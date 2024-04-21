package ru.bestteam.virtualwear.feature.permission.api

import dev.icerock.moko.permissions.PermissionsController
import ru.bestteam.virtualwear.feature.permission.internal.PermissionCheckerImpl

sealed interface PermissionCheckResult {
    data object Granted : PermissionCheckResult
    data class DeniedAlways(val permissionName: String) : PermissionCheckResult
    data class Denied(val permissionName: String) : PermissionCheckResult
}

interface PermissionChecker {
    suspend fun checkCamera(): PermissionCheckResult

    companion object {
        fun create(permissionsController: PermissionsController): PermissionChecker {
            return PermissionCheckerImpl(permissionsController)
        }
    }
}