package com.infinity.drive.domain.model

enum class BackupState {
    NONE,
    QUEUED,
    UPLOADING,
    BACKED_UP,
    FAILED
}
