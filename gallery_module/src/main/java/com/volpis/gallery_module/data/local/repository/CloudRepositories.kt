package com.volpis.gallery_module.data.local.repository
/*
package com.volpis.gallery_module.data.local.repository


import com.volpis.gallery_module.domain.media.model.MediaFilter
import com.volpis.gallery_module.domain.media.model.MediaItem
import com.volpis.gallery_module.domain.media.model.MediaResult
import com.volpis.gallery_module.domain.media.repository.MediaRepository
import com.volpis.gallery_module.domain.media.repository.PermissionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


abstract class BaseCloudRepository() : MediaRepository {

    @Suppress("Unused")
    protected val permissionHandler: PermissionHandler = object : PermissionHandler {
        override fun hasStoragePermissions(): Boolean =
            true // No need for permission check for cloud

        override suspend fun requestStoragePermissions(): Boolean = true
    }

    override fun hasPermissions(): Boolean {
        return isAuthenticated()
    }

    override suspend fun requestPermissions(): Boolean {
        return authenticate()
    }

    abstract fun isAuthenticated(): Boolean
    abstract suspend fun authenticate(): Boolean
}


class GoogleDriveRepository() : BaseCloudRepository() {

    override suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with Google Drive"))
            return@flow
        }
        emit(MediaResult.Success(emptyList()))
    }

    override suspend fun getMediaAlbums(): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with Google Drive"))
            return@flow
        }
        emit(MediaResult.AlbumsSuccess(emptyList()))
    }

    override suspend fun getAlbumMediaItems(
        albumId: String,
        filter: MediaFilter,
    ): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with Google Drive"))
            return@flow
        }

        emit(MediaResult.Success(emptyList()))
    }

    override suspend fun getMediaItem(id: String): MediaItem? {
        return null
    }

    override fun isAuthenticated(): Boolean {
        return false
    }

    override suspend fun authenticate(): Boolean {
        return false
    }
}

class DropboxRepository() : BaseCloudRepository() {

    override suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with Dropbox"))
            return@flow
        }

        emit(MediaResult.Success(emptyList()))
    }

    override suspend fun getMediaAlbums(): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with Dropbox"))
            return@flow
        }

        emit(MediaResult.AlbumsSuccess(emptyList()))
    }

    override suspend fun getAlbumMediaItems(
        albumId: String,
        filter: MediaFilter,
    ): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with Dropbox"))
            return@flow
        }

        emit(MediaResult.Success(emptyList()))
    }

    override suspend fun getMediaItem(id: String): MediaItem? {
        return null
    }

    override fun isAuthenticated(): Boolean {
        return false
    }

    override suspend fun authenticate(): Boolean {
        return false
    }
}

class OneDriveRepository() : BaseCloudRepository() {

    override suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with OneDrive"))
            return@flow
        }

        emit(MediaResult.Success(emptyList()))
    }

    override suspend fun getMediaAlbums(): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with OneDrive"))
            return@flow
        }

        emit(MediaResult.AlbumsSuccess(emptyList()))
    }

    override suspend fun getAlbumMediaItems(
        albumId: String,
        filter: MediaFilter,
    ): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with OneDrive"))
            return@flow
        }

        emit(MediaResult.Success(emptyList()))
    }

    override suspend fun getMediaItem(id: String): MediaItem? {
        return null
    }

    override fun isAuthenticated(): Boolean {
        return false
    }

    override suspend fun authenticate(): Boolean {
        return false
    }
}

class CustomCloudRepository() : BaseCloudRepository() {

    override suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with custom provider"))
            return@flow
        }

        emit(MediaResult.Success(emptyList()))
    }

    override suspend fun getMediaAlbums(): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with custom provider"))
            return@flow
        }

        emit(MediaResult.AlbumsSuccess(emptyList()))
    }

    override suspend fun getAlbumMediaItems(
        albumId: String,
        filter: MediaFilter,
    ): Flow<MediaResult> = flow {
        if (!isAuthenticated()) {
            emit(MediaResult.Error("Not authenticated with custom provider"))
            return@flow
        }

        emit(MediaResult.Success(emptyList()))
    }

    override suspend fun getMediaItem(id: String): MediaItem? {
        return null
    }

    override fun isAuthenticated(): Boolean {
        return false
    }

    override suspend fun authenticate(): Boolean {
        return false
    }
}*/
