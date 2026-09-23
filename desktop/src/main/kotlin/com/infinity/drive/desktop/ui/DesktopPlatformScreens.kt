package com.infinity.drive.desktop.ui

import androidx.compose.runtime.Composable
import com.infinity.drive.presentation.platform.PlatformScreens

object DesktopPlatformScreens : PlatformScreens {

    @Composable
    override fun Preview(onBack: () -> Unit, onEditNote: (String, String) -> Unit) {
        DesktopPreviewScreen(onBack = onBack)
    }
}
