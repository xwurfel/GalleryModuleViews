package com.volpis.gallery_module.domain.gallery.builder

import android.content.Context
import android.graphics.Typeface
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.volpis.gallery_module.domain.cloud.CloudProviderType
import com.volpis.gallery_module.domain.gallery.model.GalleryConfig
import com.volpis.gallery_module.domain.gallery.model.SelectionMode
import com.volpis.gallery_module.domain.gallery.model.ViewMode
import com.volpis.gallery_module.domain.media.model.MediaItem
import com.volpis.gallery_module.domain.media.model.MediaType
import com.volpis.gallery_module.presentation.GalleryFragment

@Suppress("UNUSED")
class GalleryBuilder(private val context: Context) {

    private val configBuilder = GalleryConfig.Builder()

    /**
     * Sets the selection mode for the gallery
     * @param mode SelectionMode.SINGLE or SelectionMode.MULTIPLE
     */
    fun setSelectionMode(mode: SelectionMode): GalleryBuilder {
        configBuilder.setSelectionMode(mode)
        return this
    }

    /**
     * Sets the maximum number of items that can be selected (only for MULTIPLE selection mode)
     * @param count Maximum selection count
     */
    fun setMaxSelectionCount(count: Int): GalleryBuilder {
        configBuilder.setMaxSelectionCount(count)
        return this
    }

    /**
     * Sets the default view mode for the gallery
     * @param mode ViewMode.GRID or ViewMode.LIST
     */
    fun setDefaultViewMode(mode: ViewMode): GalleryBuilder {
        configBuilder.setDefaultViewMode(mode)
        return this
    }

    /**
     * Sets the default number of columns for grid view
     * @param columns Number of columns (1-5)
     */
    fun setDefaultGridColumns(columns: Int): GalleryBuilder {
        configBuilder.setDefaultGridColumns(columns)
        return this
    }

    /**
     * Sets whether the user can toggle between view modes
     * @param allow True to allow view mode toggle, false otherwise
     */
    fun setAllowViewModeToggle(allow: Boolean): GalleryBuilder {
        configBuilder.setAllowViewModeToggle(allow)
        return this
    }

    /**
     * Sets whether media items should be grouped by album
     * @param group True to group by album, false to show all media in one screen
     */
    fun setGroupByAlbum(group: Boolean): GalleryBuilder {
        configBuilder.setGroupByAlbum(group)
        return this
    }

    /**
     * Sets whether to show album titles
     * @param show True to show album titles, false otherwise
     */
    fun setShowAlbumTitles(show: Boolean): GalleryBuilder {
        configBuilder.setShowAlbumTitles(show)
        return this
    }

    /**
     * Sets the default album to open when the gallery is launched
     * @param album Album ID to open, or null to show all albums
     */
    fun setDefaultOpenAlbum(album: String?): GalleryBuilder {
        configBuilder.setDefaultOpenAlbum(album)
        return this
    }

    /**
     * Sets the background color for the gallery
     * @param colorResId Color resource ID
     */
    fun setBackgroundColor(colorResId: Int): GalleryBuilder {
        configBuilder.setBackgroundColor(colorResId)
        return this
    }

    /**
     * Sets the color for the selected indicator
     * @param colorResId Color resource ID
     */
    fun setSelectedIndicatorColor(colorResId: Int): GalleryBuilder {
        configBuilder.setSelectedIndicatorColor(colorResId)
        return this
    }

    /**
     * Sets the color for album title text
     * @param colorResId Color resource ID
     */
    fun setAlbumTitleTextColor(colorResId: Int): GalleryBuilder {
        configBuilder.setAlbumTitleTextColor(colorResId)
        return this
    }

    /**
     * Sets the color for album subtitle text
     * @param colorResId Color resource ID
     */
    fun setAlbumSubtitleTextColor(colorResId: Int): GalleryBuilder {
        configBuilder.setAlbumSubtitleTextColor(colorResId)
        return this
    }

    /**
     * Sets the corner radius for media thumbnails
     * @param radiusResId Dimension resource ID
     */
    fun setThumbnailCornerRadius(radiusResId: Int): GalleryBuilder {
        configBuilder.setThumbnailCornerRadius(radiusResId)
        return this
    }

    /**
     * Sets the placeholder image for loading thumbnails
     * @param drawableResId Drawable resource ID
     */
    fun setPlaceholderImage(drawableResId: Int): GalleryBuilder {
        configBuilder.setPlaceholderImage(drawableResId)
        return this
    }

    /**
     * Sets the error image for failed thumbnail loading
     * @param drawableResId Drawable resource ID
     */
    fun setErrorImage(drawableResId: Int): GalleryBuilder {
        configBuilder.setErrorImage(drawableResId)
        return this
    }

    /**
     * Sets the selected indicator drawable
     * @param drawableResId Drawable resource ID
     */
    fun setSelectedIndicatorDrawable(drawableResId: Int): GalleryBuilder {
        configBuilder.setSelectedIndicatorDrawable(drawableResId)
        return this
    }

    /**
     * Sets the non-local indicator drawable (for cloud items)
     * @param drawableResId Drawable resource ID
     */
    fun setNonLocalIndicatorDrawable(drawableResId: Int): GalleryBuilder {
        configBuilder.setNonLocalIndicatorDrawable(drawableResId)
        return this
    }

    /**
     * Sets the typeface for titles
     * @param typeface Typeface
     */
    fun setTitleTypeface(typeface: Typeface): GalleryBuilder {
        configBuilder.setTitleTypeface(typeface)
        return this
    }

    /**
     * Sets the typeface for subtitles
     * @param typeface Typeface
     */
    fun setSubtitleTypeface(typeface: Typeface): GalleryBuilder {
        configBuilder.setSubtitleTypeface(typeface)
        return this
    }

    /**
     * Sets the gallery title text
     * @param textResId String resource ID
     */
    fun setTitleText(textResId: Int): GalleryBuilder {
        configBuilder.setTitleText(textResId)
        return this
    }

    /**
     * Sets the empty state text
     * @param textResId String resource ID
     */
    fun setEmptyStateText(textResId: Int): GalleryBuilder {
        configBuilder.setEmptyStateText(textResId)
        return this
    }

    /**
     * Sets the selection counter text format
     * @param textResId String resource ID with %d placeholder for count
     */
    fun setSelectionCounterText(textResId: Int): GalleryBuilder {
        configBuilder.setSelectionCounterText(textResId)
        return this
    }

    /**
     * Sets the done button text
     * @param textResId String resource ID
     */
    fun setDoneButtonText(textResId: Int): GalleryBuilder {
        configBuilder.setDoneButtonText(textResId)
        return this
    }

    /**
     * Sets whether to show video duration
     * @param show True to show video duration, false otherwise
     */
    fun setShowVideoDuration(show: Boolean): GalleryBuilder {
        configBuilder.setShowVideoDuration(show)
        return this
    }

    /**
     * Sets whether to auto-play videos
     * @param autoPlay True to auto-play videos, false otherwise
     */
    fun setAutoPlayVideos(autoPlay: Boolean): GalleryBuilder {
        configBuilder.setAutoPlayVideos(autoPlay)
        return this
    }

    /**
     * Sets whether to mute auto-play videos
     * @param mute True to mute auto-play videos, false otherwise
     */
    fun setMuteAutoPlayVideos(mute: Boolean): GalleryBuilder {
        configBuilder.setMuteAutoPlayVideos(mute)
        return this
    }

    /**
     * Sets whether to loop auto-play videos
     * @param loop True to loop auto-play videos, false otherwise
     */
    fun setLoopAutoPlayVideos(loop: Boolean): GalleryBuilder {
        configBuilder.setLoopAutoPlayVideos(loop)
        return this
    }

    /**
     * Sets whether to enable zoom for media items
     * @param enable True to enable zoom, false otherwise
     */
    fun setEnableZoom(enable: Boolean): GalleryBuilder {
        configBuilder.setEnableZoom(enable)
        return this
    }

    /**
     * Sets whether to enable search functionality
     * @param enable True to enable search, false otherwise
     */
    fun setEnableSearch(enable: Boolean): GalleryBuilder {
        configBuilder.setEnableSearch(enable)
        return this
    }

    /**
     * Sets whether to enable filtering functionality
     * @param enable True to enable filtering, false otherwise
     */
    fun setEnableFiltering(enable: Boolean): GalleryBuilder {
        configBuilder.setEnableFiltering(enable)
        return this
    }

    /**
     * Sets whether to enable cloud integration
     * @param enable True to enable cloud integration, false otherwise
     */
    fun setEnableCloudIntegration(enable: Boolean): GalleryBuilder {
        configBuilder.setEnableCloudIntegration(enable)
        return this
    }

    /**
     * Sets the cloud providers to integrate with
     * @param providers List of CloudProviderType
     */
    fun setCloudProviders(providers: List<CloudProviderType>): GalleryBuilder {
        configBuilder.setCloudProviders(providers)
        return this
    }

    /**
     * Sets the callback for when media items are selected
     * @param callback Callback function that receives the list of selected MediaItem
     */
    fun setOnMediaSelected(callback: (List<MediaItem>) -> Unit): GalleryBuilder {
        configBuilder.setOnMediaSelected(callback)
        return this
    }

    /**
     * Sets the callback for when a media item is clicked (long press)
     * @param callback Callback function that receives the clicked MediaItem
     */
    fun setOnMediaClicked(callback: (MediaItem) -> Unit): GalleryBuilder {
        configBuilder.setOnMediaClicked(callback)
        return this
    }

    /**
     * Sets the callback for when the back button is pressed
     * @param callback Callback function
     */
    fun setOnBackPressed(callback: () -> Unit): GalleryBuilder {
        configBuilder.setOnBackPressed(callback)
        return this
    }

    /**
     * Builds the GalleryConfig
     * @return GalleryConfig
     */
    fun buildConfig(): GalleryConfig {
        return configBuilder.build()
    }

    /**
     * Launches the gallery in the specified FragmentActivity
     * @param activity FragmentActivity
     * @param containerId Container view ID
     * @param addToBackStack True to add to back stack, false otherwise
     */
    fun launch(activity: FragmentActivity, containerId: Int, addToBackStack: Boolean = true) {
        val config = buildConfig()
        val fragment = GalleryFragment.Companion.newInstance(config)

        activity.supportFragmentManager.beginTransaction().replace(containerId, fragment).apply {
            if (addToBackStack) {
                addToBackStack(GalleryFragment::class.java.simpleName)
            }
        }.commit()
    }

    /**
     * Launches the gallery in the specified FragmentManager
     * @param fragmentManager FragmentManager
     * @param containerId Container view ID
     * @param addToBackStack True to add to back stack, false otherwise
     */
    fun launch(fragmentManager: FragmentManager, containerId: Int, addToBackStack: Boolean = true) {
        val config = buildConfig()
        val fragment = GalleryFragment.Companion.newInstance(config)

        fragmentManager.beginTransaction().replace(containerId, fragment).apply {
            if (addToBackStack) {
                addToBackStack(GalleryFragment::class.java.simpleName)
            }
        }.commit()
    }

    companion object {
        /**
         * Creates a simple gallery builder with default configuration
         * @param context Context
         * @param onMediaSelected Callback for selected media items
         * @return GalleryBuilder
         */
        fun createSimpleGallery(
            context: Context,
            onMediaSelected: (List<MediaItem>) -> Unit,
        ): GalleryBuilder {
            return GalleryBuilder(context).setSelectionMode(SelectionMode.MULTIPLE)
                .setDefaultViewMode(ViewMode.GRID).setDefaultGridColumns(3).setGroupByAlbum(true)
                .setOnMediaSelected(onMediaSelected)
        }

        /**
         * Creates a single selection gallery builder
         * @param context Context
         * @param onMediaSelected Callback for selected media item
         * @return GalleryBuilder
         */
        fun createSingleSelectionGallery(
            context: Context,
            onMediaSelected: (MediaItem) -> Unit,
        ): GalleryBuilder {
            return GalleryBuilder(context).setSelectionMode(SelectionMode.SINGLE)
                .setDefaultViewMode(ViewMode.GRID).setDefaultGridColumns(3).setGroupByAlbum(true)
                .setOnMediaSelected { items ->
                    if (items.isNotEmpty()) {
                        onMediaSelected(items.first())
                    }
                }
        }

        /**
         * Creates a photo picker gallery builder (images only)
         * @param context Context
         * @param onMediaSelected Callback for selected media items
         * @return GalleryBuilder
         */
        fun createPhotoPicker(
            context: Context,
            onMediaSelected: (List<MediaItem>) -> Unit,
        ): GalleryBuilder {
            return GalleryBuilder(context).setSelectionMode(SelectionMode.MULTIPLE)
                .setDefaultViewMode(ViewMode.GRID).setDefaultGridColumns(3).setGroupByAlbum(true)
                .setEnableFiltering(false).setOnMediaSelected { items ->
                    val images = items.filter { it.type == MediaType.IMAGE }
                    onMediaSelected(images)
                }
        }

        /**
         * Creates a video picker gallery builder (videos only)
         * @param context Context
         * @param onMediaSelected Callback for selected media items
         * @return GalleryBuilder
         */
        fun createVideoPicker(
            context: Context,
            onMediaSelected: (List<MediaItem>) -> Unit,
        ): GalleryBuilder {
            return GalleryBuilder(context).setSelectionMode(SelectionMode.MULTIPLE)
                .setDefaultViewMode(ViewMode.GRID).setDefaultGridColumns(3).setGroupByAlbum(true)
                .setEnableFiltering(false).setOnMediaSelected { items ->
                    val videos = items.filter { it.type == MediaType.VIDEO }
                    onMediaSelected(videos)
                }
        }
    }
}