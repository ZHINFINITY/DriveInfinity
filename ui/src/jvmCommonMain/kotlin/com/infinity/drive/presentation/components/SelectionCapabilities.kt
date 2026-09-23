package com.infinity.drive.presentation.components

import com.infinity.drive.domain.model.DriveFile

data class SelectionCapabilities(
    val canUpload: Boolean = false,
    val canDownload: Boolean = false,
    val canFreeUpSpace: Boolean = false,
    val soleLocalPath: String? = null
) {
    companion object {
        fun of(files: List<DriveFile>): SelectionCapabilities = SelectionCapabilities(
            canUpload = files.any { !it.hasRemoteCopy },
            canDownload = files.any { it.hasRemoteCopy && !it.hasLocalCopy },
            canFreeUpSpace = files.any { it.hasRemoteCopy && it.hasLocalCopy },
            soleLocalPath = files.singleOrNull()?.localPath
        )
    }
}
