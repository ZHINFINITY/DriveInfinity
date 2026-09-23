package com.infinity.drive.core.transfer

import android.content.Context
import com.infinity.drive.R
import com.infinity.drive.core.common.AppNotifications
import com.infinity.drive.data.local.dao.BackupDao
import com.infinity.drive.data.local.dao.TransferDao
import com.infinity.drive.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class NotifyingBackupSessionTracker(
    transferDao: TransferDao,
    backupDao: BackupDao,
    private val settingsRepository: SettingsRepository,
    private val appNotifications: AppNotifications,
    private val context: Context
) : CountingBackupSessionTracker(transferDao, backupDao) {

    override suspend fun onSessionSettled(counts: BackupSessionCounts) {
        if (!settingsRepository.preferences.first().backupNotifications) return
        appNotifications.notifyBackupResult(
            title = context.getString(R.string.notification_backup_complete),
            message = context.getString(
                R.string.notification_backup_summary,
                counts.completedFiles,
                counts.totalFiles
            )
        )
    }
}
