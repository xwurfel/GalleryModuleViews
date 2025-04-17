package com.volpis.gallery_module.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.volpis.gallery_module.domain.model.media.*
import com.volpis.gallery_module.domain.repository.MediaRepository
import com.volpis.gallery_module.domain.repository.PermissionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Date

class DeviceMediaRepositoryImpl(
    context: Context,
    private val permissionHandler: PermissionHandler,
) : MediaRepository {

    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult> = flow {
        if (!hasPermissions()) {
            emit(MediaResult.Error("Storage permission not granted"))
            return@flow
        }

        val items = queryMediaItems(filter)
        if (items.isEmpty()) {
            emit(MediaResult.Empty)
        } else {
            emit(MediaResult.Success(items))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getMediaAlbums(): Flow<MediaResult> = flow {
        if (!hasPermissions()) {
            emit(MediaResult.Error("Storage permission not granted"))
            return@flow
        }

        val albums = queryAlbums()
        if (albums.isEmpty()) {
            emit(MediaResult.Empty)
        } else {
            emit(MediaResult.AlbumsSuccess(albums))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getAlbumMediaItems(
        albumId: String,
        filter: MediaFilter,
    ): Flow<MediaResult> = flow {
        if (!hasPermissions()) {
            emit(MediaResult.Error("Storage permission not granted"))
            return@flow
        }
        emit(MediaResult.Loading)
        val items = queryMediaItems(filter.copy(albumIds = listOf(albumId)))
        if (items.isEmpty()) {
            emit(MediaResult.Empty)
        } else {
            emit(MediaResult.Success(items))
        }
    }.flowOn(Dispatchers.IO).catch { exception ->
        emit(MediaResult.Error("Error loading album: ${exception.message}"))
    }

    override suspend fun getMediaItem(id: String): MediaItem? {
        if (!hasPermissions()) {
            return null
        }

        val parts = id.split(":", limit = 2)
        if (parts.size != 2) {
            return null
        }

        val type = if (parts[0] == "image") MediaType.IMAGE else MediaType.VIDEO
        val uri = parts[1].toUri()

        return when (type) {
            MediaType.IMAGE -> queryImage(uri)
            MediaType.VIDEO -> queryVideo(uri)
        }
    }

    override fun hasPermissions(): Boolean {
        return permissionHandler.hasStoragePermissions()
    }

    override suspend fun requestPermissions(): Boolean {
        return permissionHandler.requestStoragePermissions()
    }

    private fun queryMediaItems(filter: MediaFilter): List<MediaItem> {
        val items = mutableListOf<MediaItem>()

        if (filter.mediaTypes.contains(MediaType.IMAGE)) {
            items.addAll(queryImages(filter))
        }

        if (filter.mediaTypes.contains(MediaType.VIDEO)) {
            items.addAll(queryVideos(filter))
        }

        items.sortWith { a, b ->
            when (filter.sortBy) {
                MediaSortOption.NAME_ASC -> a.name.compareTo(b.name)
                MediaSortOption.NAME_DESC -> b.name.compareTo(a.name)
                MediaSortOption.DATE_CREATED_ASC -> a.dateCreated.compareTo(b.dateCreated)
                MediaSortOption.DATE_CREATED_DESC -> b.dateCreated.compareTo(a.dateCreated)
                MediaSortOption.DATE_MODIFIED_ASC -> a.dateModified.compareTo(b.dateModified)
                MediaSortOption.DATE_MODIFIED_DESC -> b.dateModified.compareTo(a.dateModified)
                MediaSortOption.SIZE_ASC -> a.size.compareTo(b.size)
                MediaSortOption.SIZE_DESC -> b.size.compareTo(a.size)
            }
        }

        return items
    }

    private fun queryImages(filter: MediaFilter): List<MediaItem> {
        val images = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val selection = buildSelection(filter, MediaType.IMAGE)
        val selectionArgs = buildSelectionArgs(filter)

        val sortOrder = buildSortOrder(filter.sortBy, MediaType.IMAGE)

        val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        contentResolver.query(
            imageUri, projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val image = cursorToImageItem(cursor)

                if (filter.minSize != null && image.size < filter.minSize) {
                    continue
                }

                if (filter.maxSize != null && image.size > filter.maxSize) {
                    continue
                }

                if (!filter.searchQuery.isNullOrBlank() && !image.name.contains(
                        filter.searchQuery, ignoreCase = true
                    )
                ) {
                    continue
                }

                images.add(image)
            }
        }

        return images
    }

    private fun queryVideos(filter: MediaFilter): List<MediaItem> {
        val videos = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.RESOLUTION
        )

        val selection = buildSelection(filter, MediaType.VIDEO)
        val selectionArgs = buildSelectionArgs(filter)

        val sortOrder = buildSortOrder(filter.sortBy, MediaType.VIDEO)

        val videoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        contentResolver.query(
            videoUri, projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val video = cursorToVideoItem(cursor)

                if (filter.minSize != null && video.size < filter.minSize) {
                    continue
                }

                if (filter.maxSize != null && video.size > filter.maxSize) {
                    continue
                }

                if (!filter.searchQuery.isNullOrBlank() && !video.name.contains(
                        filter.searchQuery, ignoreCase = true
                    )
                ) {
                    continue
                }

                videos.add(video)
            }
        }

        return videos
    }

    private fun queryAlbums(): List<MediaAlbum> {
        val albums = mutableMapOf<String, MutableList<MediaItem>>()

        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        contentResolver.query(
            imageUri, imageProjection, null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val bucketId =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID))
                val bucketName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val contentUri = ContentUris.withAppendedId(imageUri, id)
                val dateAdded =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))

                val item = MediaItem(
                    id = "image:$contentUri",
                    uri = contentUri,
                    name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)),
                    path = path,
                    type = MediaType.IMAGE,
                    albumId = bucketId,
                    albumName = bucketName,
                    dateCreated = Date(dateAdded * 1000),
                    dateModified = Date(dateAdded * 1000),
                    size = 0,
                    width = 0,
                    height = 0,
                    mimeType = "image/*",
                    isLocal = true
                )

                if (!albums.containsKey(bucketId)) {
                    albums[bucketId] = mutableListOf()
                }

                albums[bucketId]?.add(item)
            }
        }

        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        val videoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        contentResolver.query(
            videoUri, videoProjection, null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val bucketId =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID))
                val bucketName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                val contentUri = ContentUris.withAppendedId(videoUri, id)
                val dateAdded =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))

                val item = MediaItem(
                    id = "video:$contentUri",
                    uri = contentUri,
                    name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)),
                    path = path,
                    type = MediaType.VIDEO,
                    albumId = bucketId,
                    albumName = bucketName,
                    dateCreated = Date(dateAdded * 1000),
                    dateModified = Date(dateAdded * 1000),
                    size = 0,
                    width = 0,
                    height = 0,
                    mimeType = "video/*",
                    isLocal = true
                )

                if (!albums.containsKey(bucketId)) {
                    albums[bucketId] = mutableListOf()
                }

                albums[bucketId]?.add(item)
            }
        }

        return albums.map { (bucketId, items) ->
            val coverItem = items.firstOrNull()
            MediaAlbum(
                id = bucketId,
                name = coverItem?.albumName ?: "Unknown",
                coverUri = coverItem?.uri ?: Uri.EMPTY,
                itemCount = items.size,
                dateCreated = coverItem?.dateCreated ?: Date(),
                path = coverItem?.path?.substringBeforeLast("/")
            )
        }.sortedByDescending { it.itemCount }
    }

    private fun queryImage(uri: Uri): MediaItem? {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        contentResolver.query(
            uri, projection, null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorToImageItem(cursor)
            }
        }

        return null
    }

    private fun queryVideo(uri: Uri): MediaItem? {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.RESOLUTION
        )

        contentResolver.query(
            uri, projection, null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorToVideoItem(cursor)
            }
        }

        return null
    }

    private fun cursorToImageItem(cursor: Cursor): MediaItem {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
        val contentUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        val dateAdded =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
        val dateModified =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))

        return MediaItem(
            id = "image:$contentUri",
            uri = contentUri,
            name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)),
            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)),
            type = MediaType.IMAGE,
            albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)),
            albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)),
            dateCreated = Date(dateAdded * 1000),
            dateModified = Date(dateModified * 1000),
            size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)),
            width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)),
            height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)),
            mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)),
            isLocal = true
        )
    }

    private fun cursorToVideoItem(cursor: Cursor): MediaItem {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
        val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

        val dateAdded =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
        val dateModified =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))

        return MediaItem(
            id = "video:$contentUri",
            uri = contentUri,
            name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)),
            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)),
            type = MediaType.VIDEO,
            albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)),
            albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)),
            dateCreated = Date(dateAdded * 1000),
            dateModified = Date(dateModified * 1000),
            size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)),
            width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)),
            height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)),
            mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)),
            isLocal = true,
            duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)),
            resolution = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION))
        )
    }

    private fun buildSelection(filter: MediaFilter, type: MediaType): String {
        val selectionCriteria = mutableListOf<String>()

        if (filter.dateRange != null) {
            val startDate = filter.dateRange.first
            val endDate = filter.dateRange.second

            if (startDate != null && endDate != null) {
                val startTimestamp = startDate.time / 1000
                val endTimestamp = endDate.time / 1000

                val dateColumn = when (type) {
                    MediaType.IMAGE -> MediaStore.Images.Media.DATE_MODIFIED
                    MediaType.VIDEO -> MediaStore.Video.Media.DATE_MODIFIED
                }

                selectionCriteria.add("$dateColumn BETWEEN $startTimestamp AND $endTimestamp")
            }
        }

        if (!filter.albumIds.isNullOrEmpty()) {
            val bucketColumn = when (type) {
                MediaType.IMAGE -> MediaStore.Images.Media.BUCKET_ID
                MediaType.VIDEO -> MediaStore.Video.Media.BUCKET_ID
            }

            val placeholders = filter.albumIds.joinToString(",") { "?" }
            selectionCriteria.add("$bucketColumn IN ($placeholders)")
        }

        return if (selectionCriteria.isEmpty()) {
            ""
        } else {
            selectionCriteria.joinToString(" AND ")
        }
    }

    private fun buildSelectionArgs(filter: MediaFilter): Array<String>? {
        val args = mutableListOf<String>()

        if (filter.dateRange != null) {
            val startDate = filter.dateRange.first
            val endDate = filter.dateRange.second

            if (startDate != null && endDate != null) {
                args.add((startDate.time / 1000).toString())
                args.add((endDate.time / 1000).toString())
            }
        }

        if (!filter.albumIds.isNullOrEmpty()) {
            args.addAll(filter.albumIds)
        }

        return if (args.isEmpty()) null else args.toTypedArray()
    }

    private fun buildSortOrder(sortOption: MediaSortOption, type: MediaType): String {
        return when (sortOption) {
            MediaSortOption.NAME_ASC -> when (type) {
                MediaType.IMAGE -> "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
                MediaType.VIDEO -> "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
            }

            MediaSortOption.NAME_DESC -> when (type) {
                MediaType.IMAGE -> "${MediaStore.Images.Media.DISPLAY_NAME} DESC"
                MediaType.VIDEO -> "${MediaStore.Video.Media.DISPLAY_NAME} DESC"
            }

            MediaSortOption.DATE_CREATED_ASC -> when (type) {
                MediaType.IMAGE -> "${MediaStore.Images.Media.DATE_ADDED} ASC"
                MediaType.VIDEO -> "${MediaStore.Video.Media.DATE_ADDED} ASC"
            }

            MediaSortOption.DATE_CREATED_DESC -> when (type) {
                MediaType.IMAGE -> "${MediaStore.Images.Media.DATE_ADDED} DESC"
                MediaType.VIDEO -> "${MediaStore.Video.Media.DATE_ADDED} DESC"
            }

            MediaSortOption.DATE_MODIFIED_ASC -> when (type) {
                MediaType.IMAGE -> "${MediaStore.Images.Media.DATE_MODIFIED} ASC"
                MediaType.VIDEO -> "${MediaStore.Video.Media.DATE_MODIFIED} ASC"
            }

            MediaSortOption.DATE_MODIFIED_DESC -> when (type) {
                MediaType.IMAGE -> "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
                MediaType.VIDEO -> "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
            }

            MediaSortOption.SIZE_ASC -> when (type) {
                MediaType.IMAGE -> "${MediaStore.Images.Media.SIZE} ASC"
                MediaType.VIDEO -> "${MediaStore.Video.Media.SIZE} ASC"
            }

            MediaSortOption.SIZE_DESC -> when (type) {
                MediaType.IMAGE -> "${MediaStore.Images.Media.SIZE} DESC"
                MediaType.VIDEO -> "${MediaStore.Video.Media.SIZE} DESC"
            }
        }
    }
}