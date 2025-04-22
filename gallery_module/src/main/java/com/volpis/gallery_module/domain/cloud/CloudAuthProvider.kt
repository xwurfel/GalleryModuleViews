package com.volpis.gallery_module.domain.cloud

import android.content.Context
import android.content.Intent


interface CloudAuthProvider {
    /**
     * Returns the current authentication state
     */
    val authState: AuthState

    /**
     * Returns the cloud provider type
     */
    val providerType: CloudProviderType

    /**
     * Checks if the user is currently authenticated
     */
    fun isAuthenticated(): Boolean

    /**
     * Starts the authentication process
     * @return true if authentication was successful, false otherwise
     */
    suspend fun authenticate(): Boolean

    /**
     * Logs out the currently authenticated user
     */
    suspend fun logout()

    /**
     * Creates an intent to handle authentication
     * @param context Application context
     * @return Intent to launch for authentication, or null if not needed
     */
    fun getAuthenticationIntent(context: Context): Intent?

    /**
     * Handles the result of the authentication flow
     * @param requestCode Request code from onActivityResult
     * @param resultCode Result code from onActivityResult
     * @param data Intent data from onActivityResult
     * @return true if the result was handled, false otherwise
     */
    fun handleAuthResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean

    /**
     * Possible authentication states
     */
    enum class AuthState {
        AUTHENTICATED, AUTHENTICATING, UNAUTHENTICATED, ERROR
    }
}
