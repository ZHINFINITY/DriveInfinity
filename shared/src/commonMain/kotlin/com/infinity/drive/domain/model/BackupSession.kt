package com.infinity.drive.domain.model

data class BackupSession(
    val id: String,
    val trigger: BackupTrigger,
    val status: BackupSessionStatus,
    val totalFiles: Int,
    val completedFiles: Int,
    val failedFiles: Int,
    val skippedFiles: Int,
    val totalBytes: Long,
    val transferredBytes: Long,
    val startedAt: Long,
    val completedAt: Long?
) {
    val progress: Float
        get() = when {
            status == BackupSessionStatus.COMPLETED && totalFiles > 0 -> 1f
            totalBytes > 0 -> (transferredBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
            totalFiles > 0 -> ((completedFiles + failedFiles).toFloat() / totalFiles)
                .coerceIn(0f, 1f)
            else -> 0f
        }
}
