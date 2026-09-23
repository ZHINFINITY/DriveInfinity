package com.infinity.drive.core.transfer

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.infinity.drive.core.common.AppResult
import com.infinity.drive.core.common.SafeLog
import com.infinity.drive.core.telegram.TelegramAuthState
import com.infinity.drive.domain.model.BackupTrigger
import com.infinity.drive.domain.repository.BackupRepository
import com.infinity.drive.domain.repository.SettingsRepository
import com.infinity.drive.domain.repository.TelegramAuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Hourly safety net for automatic backup. The media trigger is the fast path,
 * but the system batches those jobs and can drop a replaced one, so this sweep
 * makes sure nothing sits unbacked for long, and re-arms the trigger if it has
 * gone missing.
 */
class MediaSweepWorker(
    appContext: Context,
    params: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val telegramAuthRepository: TelegramAuthRepository,
    private val settingsRepository: SettingsRepository,
    private val mediaTriggerScheduler: MediaTriggerScheduler
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = settingsRepository.preferences.first()
        if (!prefs.autoBackupEnabled) return Result.success()

        if (prefs.instantBackupEnabled) mediaTriggerScheduler.schedule()

        val started = telegramAuthRepository.startFromStoredCredentials()
        if (started is AppResult.Failure || started.getOrNull() != true) return Result.success()

        val ready = withTimeoutOrNull(AUTH_TIMEOUT_MS.milliseconds) {
            telegramAuthRepository.authState.first { it == TelegramAuthState.Ready }
        } != null
        if (!ready) return Result.retry()

        backupRepository.startBackup(BackupTrigger.AUTOMATIC)
        SafeLog.d(TAG, "Hourly backup sweep finished")
        return Result.success()
    }

    companion object {
        private const val TAG = "MediaSweepWorker"
        private const val AUTH_TIMEOUT_MS = 45_000L
        const val UNIQUE_NAME = "media_sweep"
    }
}
