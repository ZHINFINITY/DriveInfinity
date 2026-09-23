package com.infinity.drive.data.local.database

import org.koin.dsl.module

val daosModule = module {
    single { get<DriveInfinityDatabase>().storageChannelDao() }
    single { get<DriveInfinityDatabase>().fileDao() }
    single { get<DriveInfinityDatabase>().folderDao() }
    single { get<DriveInfinityDatabase>().transferDao() }
    single { get<DriveInfinityDatabase>().backupDao() }
    single { get<DriveInfinityDatabase>().exclusionDao() }
    single { get<DriveInfinityDatabase>().thumbnailDao() }
    single { get<DriveInfinityDatabase>().cacheDao() }
    single { get<DriveInfinityDatabase>().pendingDeleteDao() }
    single { get<DriveInfinityDatabase>().filePartDao() }
    single { get<DriveInfinityDatabase>().proxyDao() }
}
