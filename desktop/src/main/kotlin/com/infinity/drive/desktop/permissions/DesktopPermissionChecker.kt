package com.infinity.drive.desktop.permissions

import com.infinity.drive.core.permissions.AppPermission
import com.infinity.drive.core.permissions.PermissionChecker

/** Desktop file access needs no runtime permissions, so everything is granted. */
class DesktopPermissionChecker : PermissionChecker {

    override fun isGranted(permission: AppPermission): Boolean = true

    override fun hasAllFilesAccess(): Boolean = true

    override fun statuses(): Map<AppPermission, Boolean> =
        AppPermission.entries.associateWith { true }

    override fun missingCritical(): List<AppPermission> = emptyList()

    override fun isRequestable(permission: AppPermission): Boolean = false
}
