package com.infinity.drive.data.mapper

import com.infinity.drive.data.local.entity.FileEntity
import com.infinity.drive.domain.model.DriveFile

fun FileEntity.toDomain(): DriveFile = DriveFile(
    id = id,
    folderId = folderId,
    name = name,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    category = category,
    localPath = localPath,
    contentHash = contentHash,
    chatId = chatId,
    messageId = messageId,
    remoteFileId = remoteFileId,
    remoteUniqueId = remoteUniqueId,
    backupState = backupState,
    isHidden = isHidden,
    isArchived = isArchived,
    isFavorite = isFavorite,
    isEncrypted = isEncrypted,
    width = width,
    height = height,
    durationMs = durationMs,
    trashedAt = trashedAt,
    iconFileId = iconFileId,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    addedAt = addedAt
)

fun DriveFile.toEntity(preTrashFolderId: String? = null): FileEntity = FileEntity(
    id = id,
    folderId = folderId,
    name = name,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    category = category,
    localPath = localPath,
    contentHash = contentHash,
    chatId = chatId,
    messageId = messageId,
    remoteFileId = remoteFileId,
    remoteUniqueId = remoteUniqueId,
    backupState = backupState,
    isHidden = isHidden,
    isArchived = isArchived,
    isFavorite = isFavorite,
    isEncrypted = isEncrypted,
    width = width,
    height = height,
    durationMs = durationMs,
    trashedAt = trashedAt,
    preTrashFolderId = preTrashFolderId,
    iconFileId = iconFileId,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    addedAt = addedAt
)
