package com.infinity.drive.domain.repository

import com.infinity.drive.core.common.AppResult
import com.infinity.drive.core.telegram.TelegramAuthState
import com.infinity.drive.core.telegram.TelegramConnectionState
import com.infinity.drive.core.telegram.TelegramCredentials
import com.infinity.drive.core.telegram.TelegramUser
import com.infinity.drive.domain.model.CountryList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TelegramAuthRepository {

    val authState: StateFlow<TelegramAuthState>

    val connectionState: StateFlow<TelegramConnectionState>

    /** True once credentials are stored, regardless of session state. */
    fun hasCredentials(): Flow<Boolean>

    /** Stores credentials and starts the client. */
    suspend fun configure(credentials: TelegramCredentials): AppResult<Unit>

    /** Starts the client from stored credentials, e.g. on app launch. */
    suspend fun startFromStoredCredentials(): AppResult<Boolean>

    /** Dialling codes for the picker, with the one Telegram infers for us. */
    suspend fun countries(): AppResult<CountryList>

    suspend fun submitPhoneNumber(phoneNumber: String): AppResult<Unit>

    suspend fun requestQrCodeAuthentication(): AppResult<Unit>

    suspend fun restartAuthentication(): AppResult<Unit>

    suspend fun submitEmailAddress(email: String): AppResult<Unit>

    suspend fun submitEmailCode(code: String): AppResult<Unit>

    suspend fun submitCode(code: String): AppResult<Unit>

    suspend fun submitPassword(password: String): AppResult<Unit>

    suspend fun resendCode(): AppResult<Unit>

    suspend fun logout(): AppResult<Unit>

    suspend fun getCurrentUser(): AppResult<TelegramUser>
}
