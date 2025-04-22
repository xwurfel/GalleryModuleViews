package com.volpis.gallery_module.data.remote.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.volpis.gallery_module.R
import com.volpis.gallery_module.domain.cloud.BaseCloudRepository
import com.volpis.gallery_module.domain.cloud.CloudAuthProvider
import com.volpis.gallery_module.domain.cloud.CloudProviderType
import com.volpis.gallery_module.domain.media.model.*
import com.volpis.gallery_module.domain.media.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date

class GoogleDriveRepository(
    private val context: Context,
) : BaseCloudRepository(), MediaRepository {

    companion object {
        private const val TAG = "GoogleDriveRepository"
        private const val RC_SIGN_IN = 9001
    }

    private val googleSignInClient by lazy {
        val gso = Builder(DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(DriveScopes.DRIVE_READONLY),
                Scope(DriveScopes.DRIVE_PHOTOS_READONLY)
            )
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    private var driveService: Drive? = null

    override val providerType: CloudProviderType = CloudProviderType.GOOGLE_DRIVE

    init {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            setupDriveService(account)
            authState = CloudAuthProvider.AuthState.AUTHENTICATED
        }
    }

    override fun hasPermissions(): Boolean {
        return isAuthenticated()
    }

    override suspend fun requestPermissions(): Boolean {
        return authenticate()
    }

    override fun isAuthenticated(): Boolean {
        return authState == CloudAuthProvider.AuthState.AUTHENTICATED && driveService != null
    }

    override suspend fun authenticate(): Boolean {
        if (isAuthenticated()) return true

        authState = CloudAuthProvider.AuthState.AUTHENTICATING

        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null) {
            setupDriveService(account)
            authState = CloudAuthProvider.AuthState.AUTHENTICATED
            true
        } else {
            authState = CloudAuthProvider.AuthState.UNAUTHENTICATED
            false
        }
    }

    override suspend fun logout() {
        try {
            withContext(Dispatchers.IO) {
                googleSignInClient.signOut()
            }
            driveService = null
            authState = CloudAuthProvider.AuthState.UNAUTHENTICATED
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out: ${e.message}", e)
        }
    }

    override fun getAuthenticationIntent(context: Context): Intent {
        return googleSignInClient.signInIntent
    }

    override fun handleAuthResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != RC_SIGN_IN) return false

        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    setupDriveService(account)
                    authState = CloudAuthProvider.AuthState.AUTHENTICATED
                    true
                } else {
                    Log.e(TAG, "Sign-in successful but account is null")
                    authState = CloudAuthProvider.AuthState.ERROR
                    false
                }
            } catch (e: ApiException) {
                val errorMessage = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_REQUIRED ->
                        "User needs to sign in."

                    GoogleSignInStatusCodes.NETWORK_ERROR ->
                        "Network error occurred. Check connection."

                    GoogleSignInStatusCodes.INVALID_ACCOUNT ->
                        "Invalid account specified."

                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
                        "Sign-in was cancelled by the user."

                    GoogleSignInStatusCodes.DEVELOPER_ERROR ->
                        "Developer error: Your app is not properly configured. See CloudProviderSetup.getGoogleDriveSetupInstructions()."

                    else -> "Sign-in failed with code: ${e.statusCode}"
                }

                Log.e(TAG, errorMessage)
                Log.e(TAG, "Status: ${GoogleSignInStatusCodes.getStatusCodeString(e.statusCode)}")

                authState = CloudAuthProvider.AuthState.ERROR
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign-in", e)
            authState = CloudAuthProvider.AuthState.ERROR
            false
        }
    }


    override suspend fun getMediaItems(filter: MediaFilter): Flow<MediaResult> = flow {
        if (!isAuthenticated() && !authenticate()) {
            emit(MediaResult.Error("Not authenticated with Google Drive"))
            return@flow
        }

        try {
            val service =
                driveService ?: throw IllegalStateException("Drive service not initialized")

            val query = buildFileQuery(filter)

            val result = withContext(Dispatchers.IO) {
                service.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, createdTime, modifiedTime, imageMediaMetadata, videoMediaMetadata, thumbnailLink)")
                    .setPageSize(100)
                    .execute()
            }

            val items = result.files.mapNotNull { file ->
                convertDriveFileToMediaItem(file)
            }

            if (items.isEmpty()) {
                emit(MediaResult.Empty)
            } else {
                emit(MediaResult.Success(items))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching media items: ${e.message}", e)
            emit(MediaResult.Error("Failed to fetch media from Google Drive: ${e.message}"))
        }
    }

    override suspend fun getMediaAlbums(): Flow<MediaResult> = flow {
        if (!isAuthenticated() && !authenticate()) {
            emit(MediaResult.Error("Not authenticated with Google Drive"))
            return@flow
        }

        try {
            val service =
                driveService ?: throw IllegalStateException("Drive service not initialized")

            val result = withContext(Dispatchers.IO) {
                service.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false")
                    .setSpaces("drive")
                    .setFields("files(id, name, createdTime)")
                    .execute()
            }

            val albums = mutableListOf<MediaAlbum>()

            for (folder in result.files) {
                val folderContents = withContext(Dispatchers.IO) {
                    service.files().list()
                        .setQ("'${folder.id}' in parents and (mimeType contains 'image/' or mimeType contains 'video/') and trashed=false")
                        .setFields("files(id, thumbnailLink)")
                        .execute()
                }

                val itemCount = folderContents.files.size
                if (itemCount > 0) {
                    val coverFile = folderContents.files.firstOrNull()
                    val coverUri = if (coverFile?.thumbnailLink != null) {
                        coverFile.thumbnailLink.toUri()
                    } else {
                        "android.resource://${context.packageName}/drawable/ic_folder".toUri()
                    }

                    albums.add(
                        MediaAlbum(
                            id = createCloudId(folder.id),
                            name = folder.name,
                            coverUri = coverUri,
                            itemCount = itemCount,
                            dateCreated = Date(folder.createdTime.value),
                            path = null
                        )
                    )
                }
            }

            if (albums.isEmpty()) {
                emit(MediaResult.Empty)
            } else {
                emit(MediaResult.AlbumsSuccess(albums))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching albums: ${e.message}", e)
            emit(MediaResult.Error("Failed to fetch albums from Google Drive: ${e.message}"))
        }
    }

    override suspend fun getAlbumMediaItems(
        albumId: String,
        filter: MediaFilter,
    ): Flow<MediaResult> = flow {
        if (!isAuthenticated() && !authenticate()) {
            emit(MediaResult.Error("Not authenticated with Google Drive"))
            return@flow
        }

        val folderId = parseCloudId(albumId) ?: run {
            emit(MediaResult.Error("Invalid Google Drive album ID: $albumId"))
            return@flow
        }

        try {
            val service =
                driveService ?: throw IllegalStateException("Drive service not initialized")

            val query =
                "'$folderId' in parents and (mimeType contains 'image/' or mimeType contains 'video/') and trashed=false"

            val result = withContext(Dispatchers.IO) {
                service.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, mimeType, size, createdTime, modifiedTime, imageMediaMetadata, videoMediaMetadata, thumbnailLink)")
                    .execute()
            }

            val items = result.files.mapNotNull { file ->
                convertDriveFileToMediaItem(file, folderId)
            }.filter { applyFilter(it, filter) }

            if (items.isEmpty()) {
                emit(MediaResult.Empty)
            } else {
                emit(MediaResult.Success(items))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching album media items: ${e.message}", e)
            emit(MediaResult.Error("Failed to fetch media from Google Drive folder: ${e.message}"))
        }
    }

    override suspend fun getMediaItem(id: String): MediaItem? {
        if (!isAuthenticated() && !authenticate()) {
            return null
        }

        val fileId = parseCloudId(id) ?: return null

        return try {
            val service =
                driveService ?: throw IllegalStateException("Drive service not initialized")

            val file = withContext(Dispatchers.IO) {
                service.files().get(fileId)
                    .setFields("id, name, mimeType, size, createdTime, modifiedTime, imageMediaMetadata, videoMediaMetadata, thumbnailLink, parents")
                    .execute()
            }

            convertDriveFileToMediaItem(file)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching media item: ${e.message}", e)
            null
        }
    }


    private fun setupDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_READONLY, DriveScopes.DRIVE_PHOTOS_READONLY)
        )
        credential.selectedAccount = account.account

        val transport = NetHttpTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        driveService = Drive.Builder(transport, jsonFactory, credential)
            .setApplicationName(context.getString(R.string.app_name))
            .build()
    }

    private fun buildFileQuery(filter: MediaFilter): String {
        val baseQuery =
            "(mimeType contains 'image/' or mimeType contains 'video/') and trashed=false"

        val clauses = mutableListOf(baseQuery)

        if (filter.mediaTypes.size == 1) {
            val typeClause = when {
                filter.mediaTypes.contains(MediaType.IMAGE) -> "mimeType contains 'image/'"
                filter.mediaTypes.contains(MediaType.VIDEO) -> "mimeType contains 'video/'"
                else -> null
            }

            if (typeClause != null) {
                clauses.add(typeClause)
            }
        }

        if (!filter.searchQuery.isNullOrBlank()) {
            clauses.add("name contains '${filter.searchQuery}'")
        }

        return clauses.joinToString(" and ")
    }

    private fun convertDriveFileToMediaItem(
        file: com.google.api.services.drive.model.File,
        folderId: String? = null,
    ): MediaItem? {
        val mimeType = file.mimeType ?: return null
        if (!mimeType.contains("image/") && !mimeType.contains("video/")) {
            return null
        }

        val type = when {
            mimeType.contains("image/") -> MediaType.IMAGE
            mimeType.contains("video/") -> MediaType.VIDEO
            else -> return null
        }

        val albumId = folderId ?: file.parents?.firstOrNull()
        var albumName = ""

        if (albumId != null) {
            try {
                val folder = driveService?.files()?.get(albumId)
                    ?.setFields("name")
                    ?.execute()
                albumName = folder?.name ?: ""
            } catch (e: Exception) {
                Log.w(TAG, "Could not get album name for ID $albumId", e)
            }
        }

        val width = when (type) {
            MediaType.IMAGE -> file.imageMediaMetadata?.width ?: 0
            MediaType.VIDEO -> file.videoMediaMetadata?.width ?: 0
        }

        val height = when (type) {
            MediaType.IMAGE -> file.imageMediaMetadata?.height ?: 0
            MediaType.VIDEO -> file.videoMediaMetadata?.height ?: 0
        }

        val duration = if (type == MediaType.VIDEO) {
            file.videoMediaMetadata?.durationMillis
        } else {
            null
        }

        val resolution = if (type == MediaType.VIDEO && width > 0 && height > 0) {
            "${width}x${height}"
        } else {
            null
        }

        val thumbnailUri = if (file.thumbnailLink != null) {
            file.thumbnailLink.toUri()
        } else {
            val resId = if (type == MediaType.IMAGE) {
                R.drawable.ic_image_placeholder
            } else {
                R.drawable.ic_video_placeholder
            }
            "android.resource://${context.packageName}/$resId".toUri()
        }

        return MediaItem(
            id = createCloudId(file.id),
            uri = thumbnailUri,
            name = file.name,
            path = "",
            type = type,
            albumId = albumId?.let { createCloudId(it) } ?: "",
            albumName = albumName,
            dateCreated = Date(file.createdTime.value),
            dateModified = Date(file.modifiedTime.value),
            size = file.size.toLong(),
            width = width,
            height = height,
            mimeType = mimeType,
            isLocal = false,
            cloudProvider = CloudProviderType.GOOGLE_DRIVE,
            duration = duration,
            resolution = resolution
        )
    }

    private fun applyFilter(item: MediaItem, filter: MediaFilter): Boolean {
        if (!filter.mediaTypes.contains(item.type)) {
            return false
        }

        if (!filter.searchQuery.isNullOrBlank() &&
            !item.name.contains(filter.searchQuery, ignoreCase = true)
        ) {
            return false
        }

        if (filter.minSize != null && item.size < filter.minSize) {
            return false
        }

        if (filter.maxSize != null && item.size > filter.maxSize) {
            return false
        }

        if (filter.dateRange != null) {
            val startDate = filter.dateRange.first
            val endDate = filter.dateRange.second

            if (startDate != null && item.dateModified.before(startDate)) {
                return false
            }

            if (endDate != null && item.dateModified.after(endDate)) {
                return false
            }
        }

        return true
    }
}