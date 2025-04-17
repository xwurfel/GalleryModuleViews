package com.volpis.gallery_module.domain.repository


import com.volpis.gallery_module.domain.model.CloudProviderType
import com.volpis.gallery_module.domain.model.media.MediaFilter
import com.volpis.gallery_module.domain.model.media.MediaItem
import com.volpis.gallery_module.domain.model.media.MediaResult
import kotlinx.coroutines.flow.Flow


interface MediaRepository {
    /**
     * Get all media items based on filter
     * @param filter The filter to apply to the query
     * @return Flow of [MediaResult]
     */
    suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult>

    /**
     * Get all media albums
     * @return Flow of MediaResult
     */
    suspend fun getMediaAlbums(): Flow<MediaResult>

    /**
     * Get media items from a specific album
     * @param albumId The album ID
     * @param filter The filter to apply to the query
     * @return Flow of [MediaResult]
     */
    suspend fun getAlbumMediaItems(albumId: String, filter: MediaFilter): Flow<MediaResult>

    /**
     * Get a specific media item
     * @param id The media item ID
     * @return [MediaItem] or null if not found
     */
    suspend fun getMediaItem(id: String): MediaItem?

    /**
     * Check if the app has all necessary permissions
     * @return true if all permissions are granted, false otherwise
     */
    fun hasPermissions(): Boolean

    /**
     * Request necessary permissions
     * @return true if all permissions are granted, false otherwise
     */
    suspend fun requestPermissions(): Boolean
}


class CompositeMediaRepository(
    private val deviceRepository: MediaRepository,
    private val cloudRepositories: Map<CloudProviderType, MediaRepository>,
) : MediaRepository {
    override suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult> {
        TODO("Not yet implemented")
    }

    override suspend fun getMediaAlbums(): Flow<MediaResult> {
        TODO("Not yet implemented")
    }

    override suspend fun getAlbumMediaItems(
        albumId: String,
        filter: MediaFilter,
    ): Flow<MediaResult> {
        TODO("Not yet implemented")
    }

    override suspend fun getMediaItem(id: String): MediaItem? {
        TODO("Not yet implemented")
    }

    override fun hasPermissions(): Boolean {
        return deviceRepository.hasPermissions() && cloudRepositories.values.all { it.hasPermissions() }
    }

    override suspend fun requestPermissions(): Boolean {
        return deviceRepository.requestPermissions() && cloudRepositories.values.all { it.requestPermissions() }
    }
}


interface PermissionHandler {
    fun hasStoragePermissions(): Boolean
    suspend fun requestStoragePermissions(): Boolean
}