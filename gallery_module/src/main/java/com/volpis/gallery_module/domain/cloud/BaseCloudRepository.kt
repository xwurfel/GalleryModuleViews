package com.volpis.gallery_module.domain.cloud

abstract class BaseCloudRepository : CloudRepository {
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
}