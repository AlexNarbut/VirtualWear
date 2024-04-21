package ru.bestteam.virtualwear.feature.permission.internal

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import ru.bestteam.virtualwear.feature.permission.api.PermissionCheckResult
import ru.bestteam.virtualwear.feature.permission.api.PermissionChecker

internal class PermissionCheckerImpl(
    private val permissionsController: PermissionsController
) : PermissionChecker {
    override suspend fun checkCamera(): PermissionCheckResult {
        return checkPermissions(listOf(Permission.CAMERA))
    }

    private suspend fun checkPermissions(permissions: List<Permission>): PermissionCheckResult {
        for (permission in permissions) {
            try {
                val state = permissionsController.getPermissionState(permission)
                if (state != PermissionState.Granted) {
                    permissionsController.providePermission(permission)
                }
            } catch (ex: DeniedAlwaysException) {
                PermissionCheckResult.DeniedAlways(permission.name)
            } catch (ex: DeniedException) {
                PermissionCheckResult.Denied(permission.name)
            }
        }
        return PermissionCheckResult.Granted
    }
}