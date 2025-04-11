package com.volpis.gallery_module.domain.model.media

sealed class MediaResult {
    data class Success(val items: List<MediaItem>) : MediaResult()
    data class AlbumsSuccess(val albums: List<MediaAlbum>) : MediaResult()
    data class Error(val message: String) : MediaResult()
    object Empty : MediaResult()
}