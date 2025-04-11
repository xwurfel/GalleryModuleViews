package com.volpis.gallery_module.presentation.adapters

import android.content.Context
import android.text.format.DateUtils
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.volpis.gallery_module.R
import com.volpis.gallery_module.domain.model.GalleryConfig
import com.volpis.gallery_module.domain.model.ViewMode
import com.volpis.gallery_module.domain.model.media.MediaItem
import com.volpis.gallery_module.presentation.RoundedCornersTransformation
import java.util.Locale
import java.util.concurrent.TimeUnit

class MediaAdapter(
    private val config: GalleryConfig,
    private val onItemClick: (MediaItem) -> Unit,
    private val onItemLongClick: (MediaItem) -> Unit,
) : ListAdapter<MediaItem, RecyclerView.ViewHolder>(MediaDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_GRID = 0
        private const val VIEW_TYPE_LIST = 1
    }

    private var viewMode = config.defaultViewMode
    private var selectedItems: List<MediaItem> = emptyList()

    fun setViewMode(mode: ViewMode) {
        viewMode = mode
        notifyDataSetChanged()
    }

    fun setSelectedItems(items: List<MediaItem>) {
        val oldItems = selectedItems
        selectedItems = items

        val allItems = currentList
        val itemsToUpdate = mutableListOf<Int>()

        for (i in allItems.indices) {
            val item = allItems[i]
            val wasSelected = oldItems.contains(item)
            val isSelected = items.contains(item)

            if (wasSelected != isSelected) {
                itemsToUpdate.add(i)
            }
        }

        itemsToUpdate.forEach { notifyItemChanged(it) }
    }

    override fun getItemViewType(position: Int): Int {
        return when (viewMode) {
            ViewMode.GRID -> VIEW_TYPE_GRID
            ViewMode.LIST -> VIEW_TYPE_LIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_GRID -> {
                val view = inflater.inflate(R.layout.item_media_grid, parent, false)
                GridViewHolder(view, config)
            }

            VIEW_TYPE_LIST -> {
                val view = inflater.inflate(R.layout.item_media_list, parent, false)
                ListViewHolder(view, config)
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val isSelected = selectedItems.contains(item)

        when (holder) {
            is GridViewHolder -> holder.bind(item, isSelected, onItemClick, onItemLongClick)
            is ListViewHolder -> holder.bind(item, isSelected, onItemClick, onItemLongClick)
        }
    }

    class GridViewHolder(
        itemView: View,
        private val config: GalleryConfig,
    ) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailView: ImageView = itemView.findViewById(R.id.media_thumbnail)
        private val selectedIndicator: View = itemView.findViewById(R.id.selected_indicator)
        private val durationView: TextView = itemView.findViewById(R.id.video_duration)
        private val nonLocalIndicator: ImageView = itemView.findViewById(R.id.non_local_indicator)
        private val videoIndicator: ImageView = itemView.findViewById(R.id.video_indicator)

        fun bind(
            item: MediaItem,
            isSelected: Boolean,
            onClick: (MediaItem) -> Unit,
            onLongClick: (MediaItem) -> Unit,
        ) {
            val context = itemView.context

            var requestOptions = RequestOptions()
            config.thumbnailCornerRadius?.let { radius ->
                val cornerRadius = context.resources.getDimensionPixelSize(radius)
                requestOptions =
                    requestOptions.transform(RoundedCornersTransformation(cornerRadius))
            }

            config.placeholderImage?.let { placeholder ->
                requestOptions = requestOptions.placeholder(placeholder)
            }

            config.errorImage?.let { error ->
                requestOptions = requestOptions.error(error)
            }

            Glide.with(context).load(item.uri).apply(requestOptions).into(thumbnailView)

            if (item.isVideo && config.showVideoDuration && item.duration != null) {
                durationView.visibility = View.VISIBLE
                durationView.text = formatDuration(item.duration)
            } else {
                durationView.visibility = View.GONE
            }

            videoIndicator.visibility = if (item.isVideo) View.VISIBLE else View.GONE

            if (!item.isLocal) {
                nonLocalIndicator.visibility = View.VISIBLE
                config.nonLocalIndicatorDrawable?.let { drawable ->
                    nonLocalIndicator.setImageResource(drawable)
                }
            } else {
                nonLocalIndicator.visibility = View.GONE
            }

            selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE

            if (isSelected) {
                config.selectedIndicatorDrawable?.let { drawable ->
                    selectedIndicator.background = ContextCompat.getDrawable(context, drawable)
                }

                config.selectedIndicatorColor?.let { color ->
                    selectedIndicator.backgroundTintList =
                        ContextCompat.getColorStateList(context, color)
                }
            }

            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }

        private fun formatDuration(durationMs: Long): String {
            return if (durationMs < 60 * 1000) {
                String.format(
                    Locale.getDefault(), "0:%02d", TimeUnit.MILLISECONDS.toSeconds(durationMs)
                )
            } else {
                String.format(
                    Locale.getDefault(),
                    "%d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(durationMs),
                    TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
                )
            }
        }
    }

    class ListViewHolder(
        itemView: View,
        private val config: GalleryConfig,
    ) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailView: ImageView = itemView.findViewById(R.id.media_thumbnail)
        private val selectedIndicator: View = itemView.findViewById(R.id.selected_indicator)
        private val titleView: TextView = itemView.findViewById(R.id.media_title)
        private val subtitleView: TextView = itemView.findViewById(R.id.media_subtitle)
        private val durationView: TextView = itemView.findViewById(R.id.video_duration)
        private val nonLocalIndicator: ImageView = itemView.findViewById(R.id.non_local_indicator)
        private val videoIndicator: ImageView = itemView.findViewById(R.id.video_indicator)

        fun bind(
            item: MediaItem,
            isSelected: Boolean,
            onClick: (MediaItem) -> Unit,
            onLongClick: (MediaItem) -> Unit,
        ) {
            val context = itemView.context
            var requestOptions = RequestOptions()
            config.thumbnailCornerRadius?.let { radius ->
                val cornerRadius = context.resources.getDimensionPixelSize(radius)
                requestOptions =
                    requestOptions.transform(RoundedCornersTransformation(cornerRadius))
            }
            config.placeholderImage?.let { placeholder ->
                requestOptions = requestOptions.placeholder(placeholder)
            }

            config.errorImage?.let { error ->
                requestOptions = requestOptions.error(error)
            }

            Glide.with(context).load(item.uri).apply(requestOptions).into(thumbnailView)

            titleView.text = item.name

            val formattedDate = DateUtils.formatDateTime(
                context,
                item.dateModified.time,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
            )

            val formattedSize = formatFileSize(context, item.size)
            subtitleView.text =
                context.getString(R.string.subtitle_placeholder, formattedDate, formattedSize)

            config.titleTypeface?.let { titleView.typeface = it }
            config.subtitleTypeface?.let { subtitleView.typeface = it }

            if (item.isVideo && config.showVideoDuration && item.duration != null) {
                durationView.visibility = View.VISIBLE
                durationView.text = formatDuration(item.duration)
            } else {
                durationView.visibility = View.GONE
            }

            videoIndicator.visibility = if (item.isVideo) View.VISIBLE else View.GONE

            if (!item.isLocal) {
                nonLocalIndicator.visibility = View.VISIBLE
                config.nonLocalIndicatorDrawable?.let { drawable ->
                    nonLocalIndicator.setImageResource(drawable)
                }
            } else {
                nonLocalIndicator.visibility = View.GONE
            }

            selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE

            if (isSelected) {
                config.selectedIndicatorDrawable?.let { drawable ->
                    selectedIndicator.background = ContextCompat.getDrawable(context, drawable)
                }

                config.selectedIndicatorColor?.let { color ->
                    selectedIndicator.backgroundTintList =
                        ContextCompat.getColorStateList(context, color)
                }
            }

            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }

        private fun formatDuration(durationMs: Long): String {
            return if (durationMs < 60 * 1000) {
                String.format(
                    Locale.getDefault(), "0:%02d", TimeUnit.MILLISECONDS.toSeconds(durationMs)
                )
            } else {
                String.format(
                    Locale.getDefault(),
                    "%d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(durationMs),
                    TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
                )
            }
        }

        private fun formatFileSize(context: Context, sizeInBytes: Long): String {
            return Formatter.formatFileSize(context, sizeInBytes)
        }
    }

    private class MediaDiffCallback : DiffUtil.ItemCallback<MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem == newItem
        }
    }
}