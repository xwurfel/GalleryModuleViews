package com.volpis.gallery_module.presentation.utils

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.volpis.gallery_module.domain.media.model.MediaItem
import com.volpis.gallery_module.presentation.adapters.MediaAdapter
import kotlin.collections.take


object GlideUtils {

    /**
     * Preload images for a list of MediaItems
     * This helps reduce loading time when scrolling through a list
     *
     * @param context Context to use for loading
     * @param items List of MediaItems to preload
     * @param width Target width for preloaded images
     * @param height Target height for preloaded images
     */
    fun preloadImages(
        context: Context,
        items: List<MediaItem>,
        width: Int = 100,
        height: Int = 100
    ) {
        val itemsToPreload = items.take(20)

        for (item in itemsToPreload) {
            Glide.with(context)
                .load(item.uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload(width, height)
        }
    }

    /**
     * Clear all memory cache
     * This can be called when switching between albums or when the app goes to background
     *
     * @param context Context to use for clearing cache
     */
    fun clearMemoryCache(context: Context) {
        Glide.get(context).clearMemory()
    }
}

fun MediaAdapter.preloadItemImages(context: Context) {
    val items = currentList
    if (items.isNotEmpty()) {
        GlideUtils.preloadImages(context, items)
    }
}