package com.infinity.drive.data.mapper

import com.infinity.drive.data.local.entity.BackupSessionEntity
import com.infinity.drive.domain.model.BackupSession

fun BackupSessionEntity.toDomain(): BackupSession = BackupSession(
    id = id,
    trigger = trigger,
    status = status,
    totalFiles = totalFiles,
    completedFiles = completedFiles,
    failedFiles = failedFiles,
    skippedFiles = skippedFiles,
    totalBytes = totalBytes,
    transferredBytes = transferredBytes,
    startedAt = startedAt,
    completedAt = completedAt
)
