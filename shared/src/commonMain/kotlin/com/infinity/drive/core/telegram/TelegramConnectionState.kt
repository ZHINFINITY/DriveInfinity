package com.infinity.drive.core.telegram

enum class TelegramConnectionState {
    WAITING_FOR_NETWORK,
    CONNECTING,
    UPDATING,
    READY
}
