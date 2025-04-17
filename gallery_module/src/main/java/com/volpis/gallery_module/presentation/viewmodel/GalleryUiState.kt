package com.volpis.gallery_module.presentation.viewmodel

import com.volpis.gallery_module.domain.gallery.model.ViewMode
import com.volpis.gallery_module.domain.media.model.MediaAlbum
import com.volpis.gallery_module.domain.media.model.MediaItem

sealed class GalleryUiState {
    object Loading : GalleryUiState()
    data class Success(
        val albums: List<MediaAlbum>,
        val mediaItems: List<MediaItem>,
        val selectedItems: List<MediaItem>,
        val viewMode: ViewMode,
        val columnCount: Int,
        val currentAlbumId: String?,
    ) : GalleryUiState()

    data class Error(val message: String) : GalleryUiState()
    object Empty : GalleryUiState()
    object NoPermission : GalleryUiState()
}