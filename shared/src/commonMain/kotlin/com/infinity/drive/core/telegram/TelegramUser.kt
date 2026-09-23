package com.infinity.drive.core.telegram

data class TelegramUser(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val username: String?,
    val phoneNumber: String,
    val isPremium: Boolean
)
