package com.infinity.drive.presentation.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import com.infinity.drive.resources.Res
import com.infinity.drive.resources.common_empty
import com.infinity.drive.resources.file_count
import com.infinity.drive.resources.folder_count
import com.infinity.drive.domain.model.DriveFolder

@Composable
fun DriveFolder.contentsLabel(): String {
    val parts = buildList {
        if (folderCount > 0) {
            add(pluralStringResource(Res.plurals.folder_count, folderCount, folderCount))
        }
        if (fileCount > 0) {
            add(pluralStringResource(Res.plurals.file_count, fileCount, fileCount))
        }
    }
    return if (parts.isEmpty()) stringResource(Res.string.common_empty) else parts.joinToString(" · ")
}
