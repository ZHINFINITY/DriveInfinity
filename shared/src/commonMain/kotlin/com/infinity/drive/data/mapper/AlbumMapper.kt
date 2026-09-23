package com.infinity.drive.data.mapper

import com.infinity.drive.data.local.entity.AlbumSummary
import com.infinity.drive.domain.model.MediaAlbum

fun AlbumSummary.toDomain(): MediaAlbum = MediaAlbum(
    folderId = folderId,
    name = name,
    itemCount = itemCount,
    coverFileId = coverFileId,
    latestAt = latestAt
)
