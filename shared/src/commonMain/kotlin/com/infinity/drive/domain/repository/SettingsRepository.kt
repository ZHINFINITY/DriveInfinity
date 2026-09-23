package com.infinity.drive.domain.repository

import com.infinity.drive.core.telegram.TelegramCredentials
import com.infinity.drive.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val preferences: Flow<UserPreferences>

    suspend fun update(transform: (UserPreferences) -> UserPreferences)

    /** Stored encrypted; returns null until onboarding configures them. */
    suspend fun getTelegramCredentials(): TelegramCredentials?

    suspend fun setTelegramCredentials(credentials: TelegramCredentials)

    /** True when credentials are stored, regardless of whether they can be decrypted. */
    suspend fun hasStoredTelegramCredentials(): Boolean

    suspend fun clearTelegramCredentials()
}
