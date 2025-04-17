package com.volpis.gallery_module.domain.media.model

import android.net.Uri
import com.volpis.gallery_module.domain.cloud.CloudProviderType
import java.util.Date

data class MediaItem(
    val id: String,
    val uri: Uri,
    val name: String,
    val path: String,
    val type: MediaType,
    val albumId: String,
    val albumName: String,
    val dateCreated: Date,
    val dateModified: Date,
    val size: Long,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val isLocal: Boolean = true,

    val duration: Long? = null,
    val resolution: String? = null,

    val cloudProvider: CloudProviderType? = null,
    val cloudId: String? = null,
    val downloadUrl: String? = null,
) {
    val isVideo: Boolean
        get() = type == MediaType.VIDEO

    val isImage: Boolean
        get() = type == MediaType.IMAGE
}