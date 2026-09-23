package com.infinity.drive.core.telegram

/** Where Telegram says it is sending the login code. */
enum class CodeDeliveryChannel {
    TELEGRAM_APP,
    SMS,
    SMS_WORD,
    SMS_PHRASE,
    CALL,
    FLASH_CALL,
    MISSED_CALL,
    FRAGMENT,
    FIREBASE,
    EMAIL,
    OTHER
}
