package com.volpis.gallery_module.domain.model.media

import android.net.Uri
import java.util.Date

data class MediaAlbum(
    val id: String,
    val name: String,
    val coverUri: Uri,
    val itemCount: Int,
    val dateCreated: Date,
    val path: String? = null,
)
