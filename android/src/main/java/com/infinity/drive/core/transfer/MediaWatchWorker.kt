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
 * Backs up whatever is new after MediaStore reports a change. The trigger and
 * its re-arming belong to MediaTriggerService; this only scans, so a run can
 * never cancel the work that woke it.
 */
class MediaWatchWorker(
    appContext: Context,
    params: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val telegramAuthRepository: TelegramAuthRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = settingsRepository.preferences.first()
        SafeLog.d(
            TAG,
            "Media change seen: auto=${prefs.autoBackupEnabled} " +
                    "instant=${prefs.instantBackupEnabled} wifiOnly=${prefs.backupWifiOnly}"
        )
        if (!prefs.autoBackupEnabled || !prefs.instantBackupEnabled) {
            return Result.success()
        }

        val started = telegramAuthRepository.startFromStoredCredentials()
        if (started is AppResult.Failure || started.getOrNull() != true) return Result.success()

        val ready = withTimeoutOrNull(AUTH_TIMEOUT_MS.milliseconds) {
            telegramAuthRepository.authState.first { it == TelegramAuthState.Ready }
        } != null
        if (!ready) {
            SafeLog.d(TAG, "Telegram not ready yet, retrying later")
            return Result.retry()
        }

        backupRepository.startBackup(BackupTrigger.AUTOMATIC)
        SafeLog.d(TAG, "Instant backup scan finished")

        return Result.success()
    }

    companion object {
        private const val TAG = "MediaWatchWorker"
        private const val AUTH_TIMEOUT_MS = 45_000L
        const val UNIQUE_NAME = "media_watch"
    }
}
