package com.infinity.drive.data.repository

import com.infinity.drive.data.remote.telegram.ManifestCodec
import com.infinity.drive.domain.repository.BackupRepository
import com.infinity.drive.domain.repository.CacheRepository
import com.infinity.drive.domain.repository.ChannelRepository
import com.infinity.drive.domain.repository.ExclusionRepository
import com.infinity.drive.domain.repository.FileRepository
import com.infinity.drive.domain.repository.KeyBackupRepository
import com.infinity.drive.domain.repository.ProxyRepository
import com.infinity.drive.domain.repository.SettingsRepository
import com.infinity.drive.domain.repository.SyncRepository
import com.infinity.drive.domain.repository.TelegramAuthRepository
import com.infinity.drive.domain.repository.TransferRepository
import com.infinity.drive.domain.repository.TrashRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::ManifestCodec)
    singleOf(::ActiveChannel)
    singleOf(::ChannelOwnership)
    singleOf(::FolderOwnershipRepair)
    singleOf(::FileManifestPublisher)
    singleOf(::FolderPathResolver)
    singleOf(::FolderStateSynchronizer)
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    singleOf(::FileRepositoryImpl) bind FileRepository::class
    singleOf(::TrashRepositoryImpl) bind TrashRepository::class
    singleOf(::TransferRepositoryImpl) bind TransferRepository::class
    singleOf(::BackupRepositoryImpl) bind BackupRepository::class
    singleOf(::ExclusionRepositoryImpl) bind ExclusionRepository::class
    singleOf(::TelegramAuthRepositoryImpl) bind TelegramAuthRepository::class
    singleOf(::ProxyRepositoryImpl) bind ProxyRepository::class
    singleOf(::SyncRepositoryImpl) bind SyncRepository::class
    singleOf(::ChannelRepositoryImpl) bind ChannelRepository::class
    singleOf(::CacheRepositoryImpl) bind CacheRepository::class
    singleOf(::KeyBackupRepositoryImpl) bind KeyBackupRepository::class
}
