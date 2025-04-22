package com.volpis.gallery_module.domain.cloud

interface CloudRepository : CloudAuthProvider, CloudMediaDataSource {
    /**
     * Creates a cloud-specific media ID
     * @param originalId The original ID from the cloud provider
     * @return A properly formatted cloud ID
     */
    fun createCloudId(originalId: String): String

    /**
     * Parses a cloud ID to get the original ID
     * @param id The cloud ID
     * @return The original ID, or null if invalid
     */
    fun parseCloudId(id: String): String?
}