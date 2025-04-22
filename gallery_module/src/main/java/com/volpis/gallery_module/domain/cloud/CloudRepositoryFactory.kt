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
                CloudProviderType.DROPBOX -> throw NotImplementedError("Dropbox not yet implemented")
                CloudProviderType.ONE_DRIVE -> throw NotImplementedError("OneDrive not yet implemented")
                CloudProviderType.CUSTOM -> throw NotImplementedError("Custom provider not yet implemented")
            }
        }
    }

    fun handleAuthResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return repositories.values.any { it.handleAuthResult(requestCode, resultCode, data) }
    }
}