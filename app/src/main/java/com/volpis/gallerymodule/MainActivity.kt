package com.volpis.gallerymodule

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.volpis.gallery_module.domain.gallery.model.SelectionMode
import com.volpis.gallery_module.domain.gallery.model.ViewMode
import com.volpis.gallery_module.domain.media.model.MediaItem
import com.volpis.gallery_module.domain.gallery.builder.GalleryBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_simple_gallery).setOnClickListener {
            launchSimpleGallery()
        }

        findViewById<Button>(R.id.btn_single_select).setOnClickListener {
            launchSingleSelectionGallery()
        }

        findViewById<Button>(R.id.btn_list_view).setOnClickListener {
            launchListViewGallery()
        }

        findViewById<Button>(R.id.btn_custom_gallery).setOnClickListener {
            launchCustomGallery()
        }
    }

    private fun launchSimpleGallery() {
        GalleryBuilder.createSimpleGallery(this) { selectedItems ->
            showSelectedMediaInfo(selectedItems)
        }.launch(this, R.id.fragment_container)
    }

    private fun launchSingleSelectionGallery() {
        GalleryBuilder.createSingleSelectionGallery(this) { selectedItem ->
            Toast.makeText(this, "Selected: ${selectedItem.name}", Toast.LENGTH_SHORT).show()
        }.launch(this, R.id.fragment_container)
    }

    private fun launchListViewGallery() {
        GalleryBuilder(this)
            .setDefaultViewMode(ViewMode.LIST)
            .setAllowViewModeToggle(true)
            .setGroupByAlbum(false)
            .setOnMediaSelected { selectedItems ->
                showSelectedMediaInfo(selectedItems)
            }
            .launch(this, R.id.fragment_container)
    }

    private fun launchCustomGallery() {
        GalleryBuilder(this)
            .setSelectionMode(SelectionMode.MULTIPLE)
            .setMaxSelectionCount(5)

            .setDefaultViewMode(ViewMode.GRID)
            .setDefaultGridColumns(3)
            .setAllowViewModeToggle(true)

            .setGroupByAlbum(true)
            .setShowAlbumTitles(true)

            .setBackgroundColor(android.R.color.white)
            .setSelectedIndicatorColor(R.color.md_theme_onSurface)
            .setAlbumTitleTextColor(R.color.md_theme_primary)
            .setThumbnailCornerRadius(R.dimen.thumbnail_corner_radius)

            .setTitleText(R.string.custom_gallery_title)
            .setEmptyStateText(R.string.no_media_found)
            .setSelectionCounterText(R.string.selection_counter)
            .setDoneButtonText(R.string.done)

            .setShowVideoDuration(true)
            .setAutoPlayVideos(false)

            .setEnableZoom(true)
            .setEnableSearch(true)
            .setEnableFiltering(true)

            .setOnMediaSelected { selectedItems ->
                showSelectedMediaInfo(selectedItems)
            }
            .setOnMediaClicked { item ->
                showMediaPreview(item)
            }
            .setOnBackPressed {
                supportFragmentManager.popBackStack()
            }
            .launch(this, R.id.fragment_container)
    }

    private fun showSelectedMediaInfo(items: List<MediaItem>) {
        val message = if (items.isEmpty()) {
            "No items selected"
        } else {
            val imageCount = items.count { it.isImage }
            val videoCount = items.count { it.isVideo }
            "Selected ${items.size} items: $imageCount images, $videoCount videos"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showMediaPreview(item: MediaItem) {
        val mediaType = if (item.isVideo) "Video" else "Image"
        Toast.makeText(this, "Preview $mediaType: ${item.name}", Toast.LENGTH_SHORT).show()
    }
}