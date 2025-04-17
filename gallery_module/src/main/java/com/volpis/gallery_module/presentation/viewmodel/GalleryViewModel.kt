package com.volpis.gallery_module.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.volpis.gallery_module.domain.model.GalleryConfig
import com.volpis.gallery_module.domain.model.SelectionMode
import com.volpis.gallery_module.domain.model.ViewMode
import com.volpis.gallery_module.domain.model.media.MediaFilter
import com.volpis.gallery_module.domain.model.media.MediaItem
import com.volpis.gallery_module.domain.model.media.MediaResult
import com.volpis.gallery_module.domain.repository.MediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModel(
    private val mediaRepository: MediaRepository,
    private val config: GalleryConfig,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState

    private val _currentFilter = MutableStateFlow(MediaFilter())
    val currentFilter: StateFlow<MediaFilter> = _currentFilter

    private val _viewMode = MutableStateFlow(config.defaultViewMode)
    private val _columnCount = MutableStateFlow(config.defaultGridColumns)
    private val _selectedItems = MutableStateFlow<List<MediaItem>>(emptyList())
    private val _currentAlbumId = MutableStateFlow<String?>(config.defaultOpenAlbum)

    init {
        checkPermissions()
    }

    private fun checkPermissions() {
        if (!mediaRepository.hasPermissions()) {
            _uiState.value = GalleryUiState.NoPermission
            viewModelScope.launch {
                val granted = mediaRepository.requestPermissions()
                if (granted) {
                    loadInitialData()
                }
            }
        } else {
            loadInitialData()
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading

            if (config.groupByAlbum) {
                loadAlbums()
            } else {
                loadAllMedia()
            }
        }
    }

    fun onPermissionDenied() {
        _uiState.value = GalleryUiState.NoPermission
    }

    private suspend fun loadAlbums() {
        mediaRepository.getMediaAlbums().collect { result ->
            when (result) {
                is MediaResult.AlbumsSuccess -> {
                    val albums = result.albums
                    if (albums.isEmpty()) {
                        _uiState.value = GalleryUiState.Empty
                    } else {
                        val currentAlbumId = _currentAlbumId.value
                        if (currentAlbumId != null) {
                            loadAlbumMedia(currentAlbumId)
                        } else {
                            _uiState.value = GalleryUiState.Success(
                                albums = albums,
                                mediaItems = emptyList(),
                                selectedItems = _selectedItems.value,
                                viewMode = _viewMode.value,
                                columnCount = _columnCount.value,
                                currentAlbumId = null
                            )
                        }
                    }
                }

                is MediaResult.Error -> {
                    _uiState.value = GalleryUiState.Error(result.message)
                }

                MediaResult.Loading -> {
                    _uiState.value = GalleryUiState.Loading
                }

                else -> {
                    _uiState.value = GalleryUiState.Empty
                }
            }
        }
    }

    private suspend fun loadAlbumMedia(albumId: String) {
        mediaRepository.getAlbumMediaItems(albumId, _currentFilter.value).collect { result ->
            if (_currentAlbumId.value == albumId) {
                when (result) {
                    is MediaResult.Success -> {
                        val items = result.items
                        if (items.isEmpty()) {
                            _uiState.value = GalleryUiState.Empty
                        } else {
                            when (val current = _uiState.value) {
                                is GalleryUiState.Success -> {
                                    _uiState.value = current.copy(
                                        mediaItems = items, currentAlbumId = albumId
                                    )
                                }

                                else -> {
                                    mediaRepository.getMediaAlbums().collect { albumsResult ->
                                        if (_currentAlbumId.value == albumId) {
                                            if (albumsResult is MediaResult.AlbumsSuccess) {
                                                _uiState.value = GalleryUiState.Success(
                                                    albums = albumsResult.albums,
                                                    mediaItems = items,
                                                    selectedItems = _selectedItems.value,
                                                    viewMode = _viewMode.value,
                                                    columnCount = _columnCount.value,
                                                    currentAlbumId = albumId
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is MediaResult.Error -> {
                        if (_currentAlbumId.value == albumId) {
                            _uiState.value = GalleryUiState.Error(result.message)
                        }
                    }

                    MediaResult.Loading -> {
                        _uiState.value = GalleryUiState.Loading
                    }

                    else -> {
                        if (_currentAlbumId.value == albumId) {
                            _uiState.value = GalleryUiState.Empty
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadAllMedia() {
        _currentFilter.flatMapLatest { filter ->
            mediaRepository.getMediaItems(filter)
        }.collect { result ->
            when (result) {
                is MediaResult.Success -> {
                    val items = result.items
                    if (items.isEmpty()) {
                        _uiState.value = GalleryUiState.Empty
                    } else {
                        _uiState.value = GalleryUiState.Success(
                            albums = emptyList(),
                            mediaItems = items,
                            selectedItems = _selectedItems.value,
                            viewMode = _viewMode.value,
                            columnCount = _columnCount.value,
                            currentAlbumId = null
                        )
                    }
                }

                is MediaResult.Error -> {
                    _uiState.value = GalleryUiState.Error(result.message)
                }

                else -> {
                    _uiState.value = GalleryUiState.Empty
                }
            }
        }
    }

    fun toggleItemSelection(item: MediaItem) {
        val current = _selectedItems.value.toMutableList()

        if (config.selectionMode == SelectionMode.SINGLE) {
            if (current.contains(item)) {
                current.clear()
            } else {
                current.clear()
                current.add(item)
            }
        } else {
            if (current.contains(item)) {
                current.remove(item)
            } else {
                if (current.size < config.maxSelectionCount) {
                    current.add(item)
                }
            }
        }

        _selectedItems.value = current
        updateUiState()

        config.onMediaSelected?.invoke(current)
    }

    fun toggleViewMode() {
        if (config.allowViewModeToggle) {
            _viewMode.value = when (_viewMode.value) {
                ViewMode.GRID -> ViewMode.LIST
                ViewMode.LIST -> ViewMode.GRID
            }
            updateUiState()
        }
    }

    fun setGridColumnCount(count: Int) {
        if (count in 1..5) {
            _columnCount.value = count
            updateUiState()
        }
    }

    fun openAlbum(albumId: String?) {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            _selectedItems.value = emptyList()

            val currentFilter = _currentFilter.value
            val updatedFilter = currentFilter.copy(albumIds = null)
            _currentFilter.value = updatedFilter

            _currentAlbumId.value = albumId
            if (albumId != null) {
                loadAlbumMedia(albumId)
            } else {
                loadInitialData()
            }
        }
    }

    fun updateFilter(filter: MediaFilter) {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            val currentAlbumId = _currentAlbumId.value

            val updatedFilter = if (currentAlbumId != null) {
                filter.copy(albumIds = null)
            } else {
                filter
            }
            _currentFilter.value = updatedFilter

            when {
                currentAlbumId != null -> loadAlbumMedia(currentAlbumId)
                config.groupByAlbum -> loadAlbums()
                else -> loadAllMedia()
            }
        }
    }

    fun confirmSelection() {
        config.onMediaSelected?.invoke(_selectedItems.value)
    }

    fun onMediaClicked(item: MediaItem) {
        config.onMediaClicked?.invoke(item)
    }

    fun onBackPressed() {
        when {
            _currentAlbumId.value != null && config.groupByAlbum -> {
                _currentAlbumId.value = null
                loadInitialData()
            }

            else -> {
                config.onBackPressed?.invoke()
            }
        }
    }

    private fun updateUiState() {
        val currentState = _uiState.value
        if (currentState is GalleryUiState.Success) {
            _uiState.value = currentState.copy(
                selectedItems = _selectedItems.value,
                viewMode = _viewMode.value,
                columnCount = _columnCount.value
            )
        }
    }

    class Factory(
        private val mediaRepository: MediaRepository,
        private val config: GalleryConfig,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return GalleryViewModel(mediaRepository, config) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}