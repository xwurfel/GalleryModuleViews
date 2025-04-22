package com.volpis.gallery_module.domain.cloud

import android.app.Activity
import android.content.Context
import android.content.Intent


class CloudMediaManager {

    companion object {
        const val RC_GOOGLE_SIGN_IN = 9001
        const val RC_DROPBOX_AUTH = 9002
        const val RC_ONEDRIVE_AUTH = 9003

        @JvmStatic
        fun handleAuthResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
            return CloudRepositoryFactory.handleAuthResult(requestCode, resultCode, data)
        }

        @JvmStatic
        fun authenticate(context: Activity, providerType: CloudProviderType) {
            val repository = CloudRepositoryFactory.getRepository(context, providerType)
            val intent = repository.getAuthenticationIntent(context)

            if (intent != null) {
                val requestCode = when (providerType) {
                    CloudProviderType.GOOGLE_DRIVE -> RC_GOOGLE_SIGN_IN
                    CloudProviderType.DROPBOX -> RC_DROPBOX_AUTH
                    CloudProviderType.ONE_DRIVE -> RC_ONEDRIVE_AUTH
                    CloudProviderType.CUSTOM -> RC_GOOGLE_SIGN_IN
                }
                context.startActivityForResult(intent, requestCode)
            }
        }

        @JvmStatic
        fun isAuthenticated(context: Context, providerType: CloudProviderType): Boolean {
            val repository = CloudRepositoryFactory.getRepository(context, providerType)
            return repository.isAuthenticated()
        }


        @JvmStatic
        suspend fun logout(context: Context, providerType: CloudProviderType) {
            val repository = CloudRepositoryFactory.getRepository(context, providerType)
            repository.logout()
        }
    }
}