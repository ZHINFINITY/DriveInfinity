package com.infinity.drive.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DriveInfinityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (amoled) {
        AmoledColorScheme
    } else {
        platformColorScheme(darkTheme, dynamicColor)
            ?: if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
    }

    PlatformThemeSideEffects(darkTheme)

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = DriveInfinityTypography,
        shapes = DriveInfinityShapes,
        content = content
    )
}
