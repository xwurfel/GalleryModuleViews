package com.volpis.gallery_module.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.volpis.gallery_module.R
import com.volpis.gallery_module.domain.model.GalleryConfig
import com.volpis.gallery_module.domain.model.media.MediaAlbum
import com.volpis.gallery_module.presentation.RoundedCornersTransformation


class AlbumAdapter(
    private val config: GalleryConfig,
    private val onAlbumClick: (MediaAlbum) -> Unit,
) : ListAdapter<MediaAlbum, AlbumAdapter.AlbumViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view, config)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = getItem(position)
        holder.bind(album, onAlbumClick)
    }

    class AlbumViewHolder(
        itemView: View,
        private val config: GalleryConfig,
    ) : RecyclerView.ViewHolder(itemView) {
        private val coverImageView: ImageView = itemView.findViewById(R.id.album_cover)
        private val titleView: TextView = itemView.findViewById(R.id.album_title)
        private val countView: TextView = itemView.findViewById(R.id.album_count)

        fun bind(
            album: MediaAlbum,
            onClick: (MediaAlbum) -> Unit,
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

            Glide.with(context).load(album.coverUri).apply(requestOptions).into(coverImageView)

            titleView.text = album.name
            countView.text = context.resources.getQuantityString(
                R.plurals.album_item_count, album.itemCount, album.itemCount
            )

            config.titleTypeface?.let { titleView.typeface = it }
            config.subtitleTypeface?.let { countView.typeface = it }

            config.albumTitleTextColor?.let { color ->
                titleView.setTextColor(context.getColor(color))
            }

            config.albumSubtitleTextColor?.let { color ->
                countView.setTextColor(context.getColor(color))
            }

            itemView.setOnClickListener { onClick(album) }
        }
    }

    private class AlbumDiffCallback : DiffUtil.ItemCallback<MediaAlbum>() {
        override fun areItemsTheSame(oldItem: MediaAlbum, newItem: MediaAlbum): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaAlbum, newItem: MediaAlbum): Boolean {
            return oldItem == newItem
        }
    }
}