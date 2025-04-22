package com.volpis.gallery_module.domain.media.repository

interface PermissionHandler {
    fun hasStoragePermissions(): Boolean
    suspend fun requestStoragePermissions(): Boolean
}