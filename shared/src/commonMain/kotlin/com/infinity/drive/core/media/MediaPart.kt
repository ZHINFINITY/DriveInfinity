package com.infinity.drive.core.media

/** One piece of a split file, as the player needs to see it. */
data class MediaPart(
    val remoteFileId: String,
    val plainOffset: Long,
    val plainSize: Long
)
