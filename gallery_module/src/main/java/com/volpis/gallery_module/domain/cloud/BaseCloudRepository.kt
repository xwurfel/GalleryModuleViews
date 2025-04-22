package com.volpis.gallery_module.domain.cloud

import android.util.Log

abstract class BaseCloudRepository : CloudRepository {
    companion object {
        private const val TAG = "BaseCloudRepository"
    }

    override var authState: CloudAuthProvider.AuthState = CloudAuthProvider.AuthState.UNAUTHENTICATED
        protected set

    override fun createCloudId(originalId: String): String {
        return "${providerType.name.lowercase()}:$originalId"
    }

    override fun parseCloudId(id: String): String? {
        val parts = id.split(":", limit = 2)
        if (parts.size != 2) return null

        val providerName = parts[0].uppercase()
        if (providerName != providerType.name) return null

        return parts[1]
    }

    protected suspend fun <T> apiCall(
        call: suspend () -> T,
        errorMessage: String
    ): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Log.e(TAG, "$errorMessage: ${e.message}", e)
            Result.failure(e)
        }
    }
}