package com.infinity.drive.domain.model

enum class TransferType {
    UPLOAD,
    DOWNLOAD,
    BACKUP,
    RESTORE;

    val isIncoming: Boolean
        get() = this == DOWNLOAD || this == RESTORE
}
