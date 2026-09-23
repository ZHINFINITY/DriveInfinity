package com.infinity.drive.presentation.navigation

import org.jetbrains.compose.resources.StringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.infinity.drive.resources.Res
import com.infinity.drive.resources.nav_files
import com.infinity.drive.resources.nav_gallery
import com.infinity.drive.resources.nav_home
import com.infinity.drive.resources.nav_settings

enum class TopLevelDestination(
    val route: Route,
    val labelRes: StringResource,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME(Route.Home, Res.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home),
    FILES(
        Route.Files(),
        Res.string.nav_files,
        Icons.AutoMirrored.Outlined.InsertDriveFile,
        Icons.AutoMirrored.Filled.InsertDriveFile
    ),
    GALLERY(Route.Gallery, Res.string.nav_gallery, Icons.Outlined.Photo, Icons.Filled.Photo),
    SETTINGS(Route.Settings, Res.string.nav_settings, Icons.Outlined.Settings, Icons.Filled.Settings)
}
