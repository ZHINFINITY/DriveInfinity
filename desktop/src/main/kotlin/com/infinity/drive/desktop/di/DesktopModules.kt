package com.infinity.drive.desktop.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.infinity.drive.core.crypto.CredentialCipher
import com.infinity.drive.core.crypto.FileWrappedKeyRepository
import com.infinity.drive.core.crypto.KeyBackupCodec
import com.infinity.drive.core.crypto.PassphraseKdf
import com.infinity.drive.core.crypto.SecureFileDeleter
import com.infinity.drive.core.crypto.StreamCrypto
import com.infinity.drive.core.crypto.TdlibDatabaseKeyProviderImpl
import com.infinity.drive.core.crypto.WrappedKeyRepository
import com.infinity.drive.core.dispatchers.DefaultDispatcherProvider
import com.infinity.drive.core.dispatchers.DispatcherProvider
import com.infinity.drive.core.files.AppStoragePaths
import com.infinity.drive.core.files.DownloadWriter
import com.infinity.drive.core.files.FileImporter
import com.infinity.drive.core.files.PendingShare
import com.infinity.drive.core.files.LocalCopyDeleter
import com.infinity.drive.core.files.sharedFilesModule
import com.infinity.drive.core.media.MediaMetadataExtractor
import com.infinity.drive.core.media.ThumbnailMemoryCache
import com.infinity.drive.core.media.ThumbnailStore
import com.infinity.drive.core.network.NetworkMonitor
import com.infinity.drive.core.permissions.PermissionChecker
import com.infinity.drive.core.proxy.ProxyProbe
import com.infinity.drive.core.security.AppLockManager
import com.infinity.drive.core.publish.PublishOutboxDrainer
import com.infinity.drive.core.publish.PublishScheduler
import com.infinity.drive.core.telegram.DesktopTelegramClient
import com.infinity.drive.core.telegram.TdlibDatabaseKeyProvider
import com.infinity.drive.core.telegram.TelegramClient
import com.infinity.drive.core.telegram.TelegramPacer
import com.infinity.drive.core.transfer.BackupSessionTracker
import com.infinity.drive.core.transfer.CountingBackupSessionTracker
import com.infinity.drive.core.transfer.MaintenanceScheduler
import com.infinity.drive.core.transfer.TransferErrorMessages
import com.infinity.drive.core.transfer.TransferScheduler
import com.infinity.drive.core.transfer.transferEngineModule
import com.infinity.drive.core.update.UpdateChecker
import com.infinity.drive.data.local.database.DriveInfinityDatabase
import com.infinity.drive.data.local.database.daosModule
import com.infinity.drive.data.repository.LocalDataWiper
import com.infinity.drive.data.repository.repositoryModule
import com.infinity.drive.desktop.BuildInfo
import com.infinity.drive.desktop.DesktopPlatformCapabilities
import com.infinity.drive.desktop.crypto.DpapiCredentialCipher
import com.infinity.drive.desktop.data.DesktopLocalDataWiper
import com.infinity.drive.desktop.crypto.LocalKeyCredentialCipher
import com.infinity.drive.desktop.files.DesktopDownloadWriter
import com.infinity.drive.desktop.files.DesktopFileImporter
import com.infinity.drive.desktop.files.DesktopPendingShare
import com.infinity.drive.desktop.files.DesktopLocalCopyDeleter
import com.infinity.drive.desktop.files.DesktopStandardFolderPaths
import com.infinity.drive.desktop.files.DesktopStoragePaths
import com.infinity.drive.desktop.media.DesktopMediaMetadataExtractor
import com.infinity.drive.desktop.media.DesktopThumbnailStore
import com.infinity.drive.desktop.media.ExternalMediaPlayer
import com.infinity.drive.desktop.media.MediaStreamServer
import com.infinity.drive.desktop.media.NoopThumbnailMemoryCache
import com.infinity.drive.desktop.network.DesktopNetworkMonitor
import com.infinity.drive.desktop.permissions.DesktopPermissionChecker
import com.infinity.drive.desktop.publish.DesktopPublishScheduler
import com.infinity.drive.desktop.transfer.DesktopTransferErrorMessages
import com.infinity.drive.desktop.transfer.DesktopMaintenanceScheduler
import com.infinity.drive.desktop.transfer.DesktopTransferScheduler
import com.infinity.drive.domain.usecase.useCaseModule
import com.infinity.drive.presentation.di.sharedUiModule
import com.infinity.drive.presentation.platform.PlatformCapabilities
import com.infinity.drive.presentation.platform.StandardFolderPaths
import java.io.File
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toOkioPath
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val desktopModule = module {
    includes(daosModule, repositoryModule, useCaseModule, sharedFilesModule, transferEngineModule, sharedUiModule)

    singleOf(::DesktopStoragePaths) bind AppStoragePaths::class
    single<CredentialCipher> {
        val os = System.getProperty("os.name").orEmpty().lowercase()
        if (os.contains("win")) DpapiCredentialCipher() else LocalKeyCredentialCipher(get())
    }
    singleOf(::FileWrappedKeyRepository) bind WrappedKeyRepository::class
    singleOf(::TdlibDatabaseKeyProviderImpl) bind TdlibDatabaseKeyProvider::class
    singleOf(::DefaultDispatcherProvider) bind DispatcherProvider::class
    singleOf(::StreamCrypto)
    singleOf(::SecureFileDeleter)
    singleOf(::PassphraseKdf)
    singleOf(::KeyBackupCodec)
    singleOf(::TelegramPacer)
    singleOf(::ProxyProbe)
    singleOf(::AppLockManager)
    single { UpdateChecker(BuildInfo.VERSION) }
    singleOf(::DesktopPermissionChecker) bind PermissionChecker::class
    singleOf(::DesktopStandardFolderPaths) bind StandardFolderPaths::class
    singleOf(::DesktopPlatformCapabilities) bind PlatformCapabilities::class
    singleOf(::PublishOutboxDrainer)
    singleOf(::DesktopNetworkMonitor) bind NetworkMonitor::class
    singleOf(::DesktopTransferErrorMessages) bind TransferErrorMessages::class
    singleOf(::DesktopDownloadWriter) bind DownloadWriter::class
    singleOf(::MediaStreamServer)
    singleOf(::ExternalMediaPlayer)
    singleOf(::DesktopFileImporter) bind FileImporter::class
    singleOf(::DesktopPendingShare) bind PendingShare::class
    singleOf(::DesktopLocalCopyDeleter) bind LocalCopyDeleter::class
    singleOf(::DesktopMediaMetadataExtractor) bind MediaMetadataExtractor::class
    singleOf(::DesktopThumbnailStore) bind ThumbnailStore::class
    singleOf(::NoopThumbnailMemoryCache) bind ThumbnailMemoryCache::class
    singleOf(::CountingBackupSessionTracker) bind BackupSessionTracker::class
    singleOf(::DesktopTransferScheduler) bind TransferScheduler::class
    singleOf(::DesktopMaintenanceScheduler) bind MaintenanceScheduler::class
    singleOf(::DesktopLocalDataWiper) bind LocalDataWiper::class
    singleOf(::DesktopPublishScheduler) bind PublishScheduler::class
    single<TelegramClient> {
        DesktopTelegramClient(get(), get(), get(), BuildInfo.VERSION)
    }
    single<DataStore<Preferences>> {
        val storagePaths = get<AppStoragePaths>()
        PreferenceDataStoreFactory.createWithPath {
            File(File(storagePaths.filesDir, "datastore"), "settings.preferences_pb")
                .apply { parentFile?.mkdirs() }
                .toOkioPath()
        }
    }
    single<DriveInfinityDatabase> {
        val storagePaths = get<AppStoragePaths>()
        Room.databaseBuilder<DriveInfinityDatabase>(
            name = File(storagePaths.filesDir, "driveinfinity.db").absolutePath
        )
            .addMigrations(
                com.infinity.drive.data.local.database.MIGRATION_1_2,
                com.infinity.drive.data.local.database.MIGRATION_2_3,
                com.infinity.drive.data.local.database.MIGRATION_3_4,
                com.infinity.drive.data.local.database.MIGRATION_4_5,
                com.infinity.drive.data.local.database.MIGRATION_5_6,
                com.infinity.drive.data.local.database.MIGRATION_6_7,
                com.infinity.drive.data.local.database.MIGRATION_7_8,
                com.infinity.drive.data.local.database.MIGRATION_8_9,
                com.infinity.drive.data.local.database.MIGRATION_9_10,
                com.infinity.drive.data.local.database.MIGRATION_10_11
            )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
