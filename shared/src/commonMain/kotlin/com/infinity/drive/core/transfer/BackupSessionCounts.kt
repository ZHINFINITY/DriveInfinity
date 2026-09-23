package com.infinity.drive.core.transfer

import com.infinity.drive.data.local.entity.TransferEntity
import com.infinity.drive.domain.model.TransferState

data class BackupSessionCounts(
    val totalFiles: Int,
    val completedFiles: Int,
    val failedFiles: Int,
    val totalBytes: Long,
    val transferredBytes: Long,
    val settled: Boolean,
    val allPaused: Boolean
)

fun countSession(transfers: List<TransferEntity>): BackupSessionCounts {
    val remaining = transfers.filter { it.state != TransferState.CANCELLED }
    val completed = remaining.filter { it.state == TransferState.COMPLETED }
    return BackupSessionCounts(
        totalFiles = remaining.size,
        completedFiles = completed.size,
        failedFiles = remaining.count { it.state == TransferState.FAILED },
        totalBytes = remaining.sumOf { it.sizeBytes },
        transferredBytes = remaining.sumOf {
            if (it.state == TransferState.COMPLETED) {
                it.sizeBytes
            } else {
                it.transferredBytes.coerceIn(0L, it.sizeBytes)
            }
        },
        settled = remaining.none { !it.state.isTerminal },
        allPaused = remaining.any { it.state == TransferState.PAUSED } &&
                remaining.none { it.state == TransferState.QUEUED || it.state == TransferState.RUNNING }
    )
}
