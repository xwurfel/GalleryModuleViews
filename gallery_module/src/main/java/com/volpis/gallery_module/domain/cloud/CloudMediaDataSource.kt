package com.volpis.gallery_module.domain.cloud

import com.volpis.gallery_module.domain.media.model.MediaFilter
import com.volpis.gallery_module.domain.media.model.MediaItem
import com.volpis.gallery_module.domain.media.model.MediaResult
import kotlinx.coroutines.flow.Flow


interface CloudMediaDataSource {
    /**
     * Gets all media items based on filter
     * @param filter The filter to apply to the query
     * @return Flow of MediaResult
     */
    suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult>

    /**
     * Gets all media albums/folders
     * @return Flow of MediaResult with albums
     */
    suspend fun getMediaAlbums(): Flow<MediaResult>

    /**
     * Gets media items from a specific album
     * @param albumId The album ID
     * @param filter The filter to apply to the query
     * @return Flow of MediaResult
     */
    suspend fun getAlbumMediaItems(albumId: String, filter: MediaFilter): Flow<MediaResult>

    /**
     * Gets a specific media item
     * @param id The media item ID
     * @return MediaItem or null if not found
     */
    suspend fun getMediaItem(id: String): MediaItem?
}