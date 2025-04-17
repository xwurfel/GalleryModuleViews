package com.volpis.gallery_module.domain.gallery.model

import android.graphics.Typeface
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.volpis.gallery_module.domain.cloud.CloudProviderType
import com.volpis.gallery_module.domain.media.model.MediaItem
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class GalleryConfig(
    val selectionMode: SelectionMode = SelectionMode.MULTIPLE,
    val maxSelectionCount: Int = Int.MAX_VALUE,

    val defaultViewMode: ViewMode = ViewMode.GRID,
    val defaultGridColumns: Int = 3,
    val allowViewModeToggle: Boolean = true,

    val groupByAlbum: Boolean = true,
    val showAlbumTitles: Boolean = true,
    val defaultOpenAlbum: String? = null,

    @ColorRes val backgroundColor: Int? = null,
    @ColorRes val selectedIndicatorColor: Int? = null,
    @ColorRes val albumTitleTextColor: Int? = null,
    @ColorRes val albumSubtitleTextColor: Int? = null,
    @DimenRes val thumbnailCornerRadius: Int? = null,
    @DrawableRes val placeholderImage: Int? = null,
    @DrawableRes val errorImage: Int? = null,
    @DrawableRes val selectedIndicatorDrawable: Int? = null,
    @DrawableRes val nonLocalIndicatorDrawable: Int? = null,

    val titleTypeface: @RawValue Typeface? = null,
    val subtitleTypeface: @RawValue Typeface? = null,

    @StringRes val titleText: Int? = null,
    @StringRes val emptyStateText: Int? = null,
    @StringRes val selectionCounterText: Int? = null, // Use %d for count
    @StringRes val doneButtonText: Int? = null,

    val showVideoDuration: Boolean = true,
    val autoPlayVideos: Boolean = false,
    val muteAutoPlayVideos: Boolean = true,
    val loopAutoPlayVideos: Boolean = false,

    val enableZoom: Boolean = true,
    val enableSearch: Boolean = true,
    val enableFiltering: Boolean = true,
    val enableCloudIntegration: Boolean = false,
    val cloudProviders: List<CloudProviderType> = emptyList(),

    @IgnoredOnParcel
    var onMediaSelected: ((List<MediaItem>) -> Unit)? = null,
    @IgnoredOnParcel
    var onMediaClicked: ((MediaItem) -> Unit)? = null,
    @IgnoredOnParcel
    var onBackPressed: (() -> Unit)? = null,
) : Parcelable {
    class Builder {
        private var selectionMode: SelectionMode = SelectionMode.MULTIPLE
        private var maxSelectionCount: Int = Int.MAX_VALUE
        private var defaultViewMode: ViewMode = ViewMode.GRID
        private var defaultGridColumns: Int = 3
        private var allowViewModeToggle: Boolean = true
        private var groupByAlbum: Boolean = true
        private var showAlbumTitles: Boolean = true
        private var defaultOpenAlbum: String? = null
        private var backgroundColor: Int? = null
        private var selectedIndicatorColor: Int? = null
        private var albumTitleTextColor: Int? = null
        private var albumSubtitleTextColor: Int? = null
        private var thumbnailCornerRadius: Int? = null
        private var placeholderImage: Int? = null
        private var errorImage: Int? = null
        private var selectedIndicatorDrawable: Int? = null
        private var nonLocalIndicatorDrawable: Int? = null
        private var titleTypeface: Typeface? = null
        private var subtitleTypeface: Typeface? = null
        private var titleText: Int? = null
        private var emptyStateText: Int? = null
        private var selectionCounterText: Int? = null
        private var doneButtonText: Int? = null
        private var showVideoDuration: Boolean = true
        private var autoPlayVideos: Boolean = false
        private var muteAutoPlayVideos: Boolean = true
        private var loopAutoPlayVideos: Boolean = false
        private var enableZoom: Boolean = true
        private var enableSearch: Boolean = true
        private var enableFiltering: Boolean = true
        private var enableCloudIntegration: Boolean = false
        private var cloudProviders: List<CloudProviderType> = emptyList()
        private var onMediaSelected: ((List<MediaItem>) -> Unit)? = null
        private var onMediaClicked: ((MediaItem) -> Unit)? = null
        private var onBackPressed: (() -> Unit)? = null

        fun setSelectionMode(mode: SelectionMode) = apply { this.selectionMode = mode }
        fun setMaxSelectionCount(count: Int) = apply { this.maxSelectionCount = count }
        fun setDefaultViewMode(mode: ViewMode) = apply { this.defaultViewMode = mode }
        fun setDefaultGridColumns(columns: Int) = apply { this.defaultGridColumns = columns }
        fun setAllowViewModeToggle(allow: Boolean) = apply { this.allowViewModeToggle = allow }
        fun setGroupByAlbum(group: Boolean) = apply { this.groupByAlbum = group }
        fun setShowAlbumTitles(show: Boolean) = apply { this.showAlbumTitles = show }
        fun setDefaultOpenAlbum(album: String?) = apply { this.defaultOpenAlbum = album }
        fun setBackgroundColor(@ColorRes color: Int) = apply { this.backgroundColor = color }
        fun setSelectedIndicatorColor(@ColorRes color: Int) =
            apply { this.selectedIndicatorColor = color }

        fun setAlbumTitleTextColor(@ColorRes color: Int) =
            apply { this.albumTitleTextColor = color }

        fun setAlbumSubtitleTextColor(@ColorRes color: Int) =
            apply { this.albumSubtitleTextColor = color }

        fun setThumbnailCornerRadius(@DimenRes radius: Int) =
            apply { this.thumbnailCornerRadius = radius }

        fun setPlaceholderImage(@DrawableRes image: Int) = apply { this.placeholderImage = image }
        fun setErrorImage(@DrawableRes image: Int) = apply { this.errorImage = image }
        fun setSelectedIndicatorDrawable(@DrawableRes drawable: Int) =
            apply { this.selectedIndicatorDrawable = drawable }

        fun setNonLocalIndicatorDrawable(@DrawableRes drawable: Int) =
            apply { this.nonLocalIndicatorDrawable = drawable }

        fun setTitleTypeface(typeface: Typeface) = apply { this.titleTypeface = typeface }
        fun setSubtitleTypeface(typeface: Typeface) = apply { this.subtitleTypeface = typeface }
        fun setTitleText(@StringRes text: Int) = apply { this.titleText = text }
        fun setEmptyStateText(@StringRes text: Int) = apply { this.emptyStateText = text }
        fun setSelectionCounterText(@StringRes text: Int) =
            apply { this.selectionCounterText = text }

        fun setDoneButtonText(@StringRes text: Int) = apply { this.doneButtonText = text }
        fun setShowVideoDuration(show: Boolean) = apply { this.showVideoDuration = show }
        fun setAutoPlayVideos(autoPlay: Boolean) = apply { this.autoPlayVideos = autoPlay }
        fun setMuteAutoPlayVideos(mute: Boolean) = apply { this.muteAutoPlayVideos = mute }
        fun setLoopAutoPlayVideos(loop: Boolean) = apply { this.loopAutoPlayVideos = loop }
        fun setEnableZoom(enable: Boolean) = apply { this.enableZoom = enable }
        fun setEnableSearch(enable: Boolean) = apply { this.enableSearch = enable }
        fun setEnableFiltering(enable: Boolean) = apply { this.enableFiltering = enable }
        fun setEnableCloudIntegration(enable: Boolean) =
            apply { this.enableCloudIntegration = enable }

        fun setCloudProviders(providers: List<CloudProviderType>) =
            apply { this.cloudProviders = providers }

        fun setOnMediaSelected(callback: (List<MediaItem>) -> Unit) =
            apply { this.onMediaSelected = callback }

        fun setOnMediaClicked(callback: (MediaItem) -> Unit) =
            apply { this.onMediaClicked = callback }

        fun setOnBackPressed(callback: () -> Unit) = apply { this.onBackPressed = callback }

        fun build(): GalleryConfig {
            val config = GalleryConfig(
                selectionMode = selectionMode,
                maxSelectionCount = maxSelectionCount,
                defaultViewMode = defaultViewMode,
                defaultGridColumns = defaultGridColumns,
                allowViewModeToggle = allowViewModeToggle,
                groupByAlbum = groupByAlbum,
                showAlbumTitles = showAlbumTitles,
                defaultOpenAlbum = defaultOpenAlbum,
                backgroundColor = backgroundColor,
                selectedIndicatorColor = selectedIndicatorColor,
                albumTitleTextColor = albumTitleTextColor,
                albumSubtitleTextColor = albumSubtitleTextColor,
                thumbnailCornerRadius = thumbnailCornerRadius,
                placeholderImage = placeholderImage,
                errorImage = errorImage,
                selectedIndicatorDrawable = selectedIndicatorDrawable,
                nonLocalIndicatorDrawable = nonLocalIndicatorDrawable,
                titleTypeface = titleTypeface,
                subtitleTypeface = subtitleTypeface,
                titleText = titleText,
                emptyStateText = emptyStateText,
                selectionCounterText = selectionCounterText,
                doneButtonText = doneButtonText,
                showVideoDuration = showVideoDuration,
                autoPlayVideos = autoPlayVideos,
                muteAutoPlayVideos = muteAutoPlayVideos,
                loopAutoPlayVideos = loopAutoPlayVideos,
                enableZoom = enableZoom,
                enableSearch = enableSearch,
                enableFiltering = enableFiltering,
                enableCloudIntegration = enableCloudIntegration,
                cloudProviders = cloudProviders
            )

            config.onMediaSelected = onMediaSelected
            config.onMediaClicked = onMediaClicked
            config.onBackPressed = onBackPressed

            return config
        }
    }
}
