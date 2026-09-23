package com.infinity.drive.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.infinity.drive.resources.Res
import com.infinity.drive.resources.info_contents
import com.infinity.drive.resources.info_created
import com.infinity.drive.resources.info_favorite
import com.infinity.drive.resources.info_modified
import com.infinity.drive.resources.info_no
import com.infinity.drive.resources.info_yes
import com.infinity.drive.domain.model.DriveFolder
import com.infinity.drive.presentation.common.Formatters

/** Folder counterpart to [FileInfoSheet], with what a folder actually has. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderInfoSheet(folder: DriveFolder, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(text = folder.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            FolderInfoRow(
                label = stringResource(Res.string.info_contents),
                value = folder.contentsLabel()
            )

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            FolderInfoRow(
                label = stringResource(Res.string.info_created),
                value = Formatters.dateTime(folder.createdAt)
            )
            FolderInfoRow(
                label = stringResource(Res.string.info_modified),
                value = Formatters.dateTime(folder.modifiedAt)
            )
            FolderInfoRow(
                label = stringResource(Res.string.info_favorite),
                value = stringResource(
                    if (folder.isFavorite) Res.string.info_yes else Res.string.info_no
                )
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FolderInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.65f)
        )
    }
}
