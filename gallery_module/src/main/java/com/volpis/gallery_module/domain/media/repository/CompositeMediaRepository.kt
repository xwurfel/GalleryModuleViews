package com.volpis.gallery_module.domain.media.repository

import android.util.Log
import com.volpis.gallery_module.domain.cloud.CloudProviderType
import com.volpis.gallery_module.domain.media.model.MediaAlbum
import com.volpis.gallery_module.domain.media.model.MediaFilter
import com.volpis.gallery_module.domain.media.model.MediaItem
import com.volpis.gallery_module.domain.media.model.MediaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale

class CompositeMediaRepository(
    private val deviceRepository: MediaRepository,
    private val cloudRepositories: Map<CloudProviderType, MediaRepository>,
) : MediaRepository {

    override suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult> = flow {
        val localItemsFlow = deviceRepository.getMediaItems(filter)

        val allItems = mutableListOf<MediaItem>()
        var hasError = false
        var errorMessage = ""

        localItemsFlow.collect { result ->
            when (result) {
                is MediaResult.Success -> allItems.addAll(result.items)
                is MediaResult.Error -> {
                    hasError = true
                    errorMessage = result.message
                }

                else -> {}
            }
        }

        for ((providerType, repository) in cloudRepositories) {
            if (repository.hasPermissions()) {
                repository.getMediaItems(filter).collect { result ->
                    when (result) {
                        is MediaResult.Success -> {
                            val cloudItems = result.items.map { item ->
                                item.copy(
                                    isLocal = false,
                                    cloudProvider = providerType,
                                    id = "${providerType.name.lowercase()}:${item.id}"
                                )
                            }
                            allItems.addAll(cloudItems)
                        }

                        is MediaResult.Error -> {
                            Log.e(
                                "CompositeMediaRepo", "Error from $providerType: ${result.message}"
                            )
                        }

                        else -> {}
                    }
                }
            }
        }

        when {
            hasError && allItems.isEmpty() -> emit(MediaResult.Error(errorMessage))
            allItems.isEmpty() -> emit(MediaResult.Empty)
            else -> {
                val sortedItems = allItems.sortedByDescending { it.dateModified }
                emit(MediaResult.Success(sortedItems))
            }
        }
    }

    override suspend fun getMediaAlbums(): Flow<MediaResult> = flow {
        val allAlbums = mutableMapOf<String, MediaAlbum>()
        var hasError = false
        var errorMessage = ""

        deviceRepository.getMediaAlbums().collect { result ->
            when (result) {
                is MediaResult.AlbumsSuccess -> {
                    result.albums.forEach { album ->
                        allAlbums[album.id] = album
                    }
                }

                is MediaResult.Error -> {
                    hasError = true
                    errorMessage = result.message
                }

                else -> {}
            }
        }

        for ((providerType, repository) in cloudRepositories) {
            if (repository.hasPermissions()) {
                repository.getMediaAlbums().collect { result ->
                    when (result) {
                        is MediaResult.AlbumsSuccess -> {
                            result.albums.forEach { album ->
                                val cloudAlbumId = "${providerType.name.lowercase()}:${album.id}"
                                val cloudAlbum =
                                    album.copy(
                                        id = cloudAlbumId, name = "${album.name} (${
                                            providerType.name.replaceFirstChar {
                                                it.titlecase(
                                                    Locale.ROOT
                                                )
                                            }
                                        })")
                                allAlbums[cloudAlbumId] = cloudAlbum
                            }
                        }

                        is MediaResult.Error -> {
                            Log.e(
                                "CompositeMediaRepo", "Error from $providerType: ${result.message}"
                            )
                        }

                        else -> {}
                    }
                }
            }
        }

        when {
            hasError && allAlbums.isEmpty() -> emit(MediaResult.Error(errorMessage))
            allAlbums.isEmpty() -> emit(MediaResult.Empty)
            else -> {
                val sortedAlbums = allAlbums.values.sortedByDescending { it.itemCount }
                emit(MediaResult.AlbumsSuccess(sortedAlbums))
            }
        }
    }

    override suspend fun getAlbumMediaItems(
        albumId: String,
        filter: MediaFilter,
    ): Flow<MediaResult> = flow {
        val parts = albumId.split(":", limit = 2)

        if (parts.size == 2) {
            try {
                val providerName = parts[0].uppercase()
                val cloudAlbumId = parts[1]
                val providerType = CloudProviderType.valueOf(providerName)

                val repository = cloudRepositories[providerType]
                if (repository != null && repository.hasPermissions()) {
                    repository.getAlbumMediaItems(cloudAlbumId, filter).collect { result ->
                        when (result) {
                            is MediaResult.Success -> {
                                val cloudItems = result.items.map { item ->
                                    item.copy(
                                        isLocal = false,
                                        cloudProvider = providerType,
                                        id = "${providerType.name.lowercase()}:${item.id}"
                                    )
                                }
                                emit(MediaResult.Success(cloudItems))
                            }

                            else -> emit(result)
                        }
                    }
                } else {
                    emit(MediaResult.Error("Cloud provider not available or not authenticated"))
                }
            } catch (_: Exception) {
                emit(MediaResult.Error("Invalid cloud album ID: $albumId"))
            }
        } else {
            deviceRepository.getAlbumMediaItems(albumId, filter).collect { result ->
                emit(result)
            }
        }
    }

    override suspend fun getMediaItem(id: String): MediaItem? {
        val parts = id.split(":", limit = 2)

        if (parts.size >= 2) {
            val providerPrefix = parts[0].uppercase()

            if (providerPrefix == "IMAGE" || providerPrefix == "VIDEO") {
                return deviceRepository.getMediaItem(id)
            }

            try {
                val providerType = CloudProviderType.valueOf(providerPrefix)
                val cloudItemId = parts.subList(1, parts.size).joinToString(":")

                val repository = cloudRepositories[providerType]
                if (repository != null && repository.hasPermissions()) {
                    val item = repository.getMediaItem(cloudItemId)
                    return item?.copy(
                        isLocal = false, cloudProvider = providerType, id = id
                    )
                }
            } catch (e: Exception) {
                Log.e("CompositeMediaRepo", "Error parsing item ID: $id", e)
            }

            return null
        } else {
            return deviceRepository.getMediaItem(id)
        }
    }

    override fun hasPermissions(): Boolean {
        return deviceRepository.hasPermissions() && (cloudRepositories.isEmpty() || cloudRepositories.any { it.value.hasPermissions() })
    }

    override suspend fun requestPermissions(): Boolean {
        val devicePermission = deviceRepository.requestPermissions()

        val cloudPermissions = cloudRepositories.map { (_, repository) ->
            repository.requestPermissions()
        }

        return devicePermission && (cloudRepositories.isEmpty() || cloudPermissions.any { it })
    }
}