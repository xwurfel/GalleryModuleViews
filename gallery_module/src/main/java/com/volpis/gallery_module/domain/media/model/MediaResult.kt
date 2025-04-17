package com.volpis.gallery_module.domain.media.model

sealed interface MediaResult {
    data class Success(val items: List<MediaItem>) : MediaResult
    data class AlbumsSuccess(val albums: List<MediaAlbum>) : MediaResult
    data class Error(val message: String) : MediaResult
    data object Empty : MediaResult
    data object Loading : MediaResult
}