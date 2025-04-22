package com.volpis.gallery_module.domain.cloud

import android.content.Context
import android.content.Intent
import com.volpis.gallery_module.data.remote.repository.GoogleDriveRepository


internal object CloudRepositoryFactory {
    private val repositories = mutableMapOf<CloudProviderType, CloudRepository>()

    fun getRepository(context: Context, type: CloudProviderType): CloudRepository {
        return repositories.getOrPut(type) {
            when (type) {
                CloudProviderType.GOOGLE_DRIVE -> GoogleDriveRepository(context.applicationContext)
                CloudProviderType.CUSTOM -> throw NotImplementedError("Please provide implementation for $type")
            }
        }
    }

    fun handleAuthResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return repositories.values.any { it.handleAuthResult(requestCode, resultCode, data) }
    }
}