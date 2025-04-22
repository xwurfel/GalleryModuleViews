package com.volpis.gallery_module.domain.cloud

import android.content.Context
import android.content.pm.PackageManager


class CloudProviderSetup {
    companion object {
        /**
         * Checks if Google Drive is properly configured
         * @return True if configured correctly, false otherwise
         */
        @JvmStatic
        fun isGoogleDriveConfigured(context: Context): Boolean {
            try {
                val metaData = context.packageManager
                    .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                    .metaData

                return metaData?.containsKey("com.google.android.gms.drive.API_KEY") == true
            } catch (e: Exception) {
                return false
            }
        }

        @JvmStatic
        fun getGoogleDriveSetupInstructions(): String {
            return """
                To use Google Drive with this library:
                
                1. Go to Google Cloud Console (https://console.cloud.google.com)
                2. Create a new project or select an existing one
                3. Enable the Google Drive API for your project
                4. Create OAuth credentials:
                   - Go to Credentials > Create Credentials > OAuth client ID
                   - Select Android as the application type
                   - Enter your app's package name
                   - Add your app's SHA-1 certificate fingerprint
                   - Download the google-services.json file
                5. Add the google-services.json file to your app module
                6. Add the Google Services plugin to your project:
                   - Add the plugin to your app's build.gradle
                   - Apply the plugin in your app module
                
                For detailed instructions, see our documentation.
            """.trimIndent()
        }
    }
}