package com.infinity.drive.data.mapper

import com.infinity.drive.data.local.entity.TransferEntity
import com.infinity.drive.domain.model.TransferTask

fun TransferEntity.toDomain(): TransferTask = TransferTask(
    id = id,
    type = type,
    fileId = fileId,
    displayName = displayName,
    sizeBytes = sizeBytes,
    transferredBytes = transferredBytes,
    state = state,
    priority = priority,
    errorMessage = errorMessage,
    speedBytesPerSecond = speedBytesPerSecond,
    stage = stage,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt
)
