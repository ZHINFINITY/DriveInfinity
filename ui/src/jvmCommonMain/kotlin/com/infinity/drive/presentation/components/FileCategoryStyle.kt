package com.infinity.drive.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import com.infinity.drive.resources.Res
import com.infinity.drive.resources.common_category_archives
import com.infinity.drive.resources.common_category_audio
import com.infinity.drive.resources.common_category_documents
import com.infinity.drive.resources.common_category_other
import com.infinity.drive.resources.common_category_photos
import com.infinity.drive.resources.common_category_videos
import com.infinity.drive.domain.model.FileCategory
import com.infinity.drive.presentation.theme.ChartPalette
import com.infinity.drive.presentation.theme.harmonizedWithPrimary

@Composable
fun FileCategory.label(): String = stringResource(
    when (this) {
        FileCategory.IMAGE -> Res.string.common_category_photos
        FileCategory.VIDEO -> Res.string.common_category_videos
        FileCategory.AUDIO -> Res.string.common_category_audio
        FileCategory.DOCUMENT -> Res.string.common_category_documents
        FileCategory.ARCHIVE -> Res.string.common_category_archives
        FileCategory.OTHER -> Res.string.common_category_other
    }
)

/**
 * Chart color per category. Authored hues keep the categories apart; the
 * harmonization step keeps them in the theme's palette.
 */
@Composable
fun FileCategory.chartColor(): Color = when (this) {
    FileCategory.IMAGE -> ChartPalette.Blue
    FileCategory.VIDEO -> ChartPalette.Violet
    FileCategory.AUDIO -> ChartPalette.Teal
    FileCategory.DOCUMENT -> ChartPalette.Amber
    FileCategory.ARCHIVE -> ChartPalette.Rose
    FileCategory.OTHER -> ChartPalette.Slate
}.harmonizedWithPrimary()
