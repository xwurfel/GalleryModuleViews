package com.volpis.gallery_module.domain.media.model

import android.os.Parcelable
import com.volpis.gallery_module.domain.cloud.CloudProviderType
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class MediaFilter(
    val mediaTypes: Set<MediaType> = setOf(MediaType.IMAGE, MediaType.VIDEO),
    val dateRange: Pair<Date?, Date?>? = null,
    val albumIds: List<String>? = null,
    val searchQuery: String? = null,
    val minSize: Long? = null,
    val maxSize: Long? = null,
    val includeCloudItems: Boolean = false,
    val cloudProviders: Set<CloudProviderType>? = null,
    val sortBy: MediaSortOption = MediaSortOption.DATE_MODIFIED_DESC,
) : Parcelable