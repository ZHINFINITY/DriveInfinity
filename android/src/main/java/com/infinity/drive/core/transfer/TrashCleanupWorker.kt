package com.infinity.drive.core.transfer

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.infinity.drive.core.common.AppResult
import com.infinity.drive.domain.repository.SettingsRepository
import com.infinity.drive.domain.repository.TrashRepository
import kotlinx.coroutines.flow.first

class TrashCleanupWorker(
    appContext: Context,
    params: WorkerParameters,
    private val trashRepository: TrashRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val days = settingsRepository.preferences.first().trashAutoClearDays
        if (days <= 0) return Result.success()
        return when (trashRepository.clearExpired(days)) {
            is AppResult.Success -> Result.success()
            is AppResult.Failure -> Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "trash_cleanup"
    }
}
