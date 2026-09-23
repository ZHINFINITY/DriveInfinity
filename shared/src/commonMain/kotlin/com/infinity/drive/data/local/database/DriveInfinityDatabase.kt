package com.infinity.drive.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.infinity.drive.data.local.dao.BackupDao
import com.infinity.drive.data.local.dao.CacheDao
import com.infinity.drive.data.local.dao.ExclusionDao
import com.infinity.drive.data.local.dao.FileDao
import com.infinity.drive.data.local.dao.FilePartDao
import com.infinity.drive.data.local.dao.ProxyDao
import com.infinity.drive.data.local.dao.FolderDao
import com.infinity.drive.data.local.dao.PendingDeleteDao
import com.infinity.drive.data.local.dao.StorageChannelDao
import com.infinity.drive.data.local.dao.ThumbnailDao
import com.infinity.drive.data.local.dao.TransferDao
import com.infinity.drive.data.local.entity.BackupRecordEntity
import com.infinity.drive.data.local.entity.BackupSessionEntity
import com.infinity.drive.data.local.entity.CacheEntryEntity
import com.infinity.drive.data.local.entity.ExclusionEntity
import com.infinity.drive.data.local.entity.FileEntity
import com.infinity.drive.data.local.entity.FilePartEntity
import com.infinity.drive.data.local.entity.ProxyEntity
import com.infinity.drive.data.local.entity.FolderEntity
import com.infinity.drive.data.local.entity.PendingDeleteEntity
import com.infinity.drive.data.local.entity.StorageChannelEntity
import com.infinity.drive.data.local.entity.ThumbnailEntity
import com.infinity.drive.data.local.entity.TransferEntity

@Database(
    entities = [
        FileEntity::class,
        FolderEntity::class,
        TransferEntity::class,
        BackupSessionEntity::class,
        BackupRecordEntity::class,
        ExclusionEntity::class,
        ThumbnailEntity::class,
        CacheEntryEntity::class,
        StorageChannelEntity::class,
        PendingDeleteEntity::class,
        FilePartEntity::class,
        ProxyEntity::class
    ],
    version = 11,
    exportSchema = true
)
@ConstructedBy(DriveInfinityDatabaseConstructor::class)
abstract class DriveInfinityDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun folderDao(): FolderDao
    abstract fun transferDao(): TransferDao
    abstract fun backupDao(): BackupDao
    abstract fun exclusionDao(): ExclusionDao
    abstract fun thumbnailDao(): ThumbnailDao
    abstract fun cacheDao(): CacheDao
    abstract fun storageChannelDao(): StorageChannelDao
    abstract fun pendingDeleteDao(): PendingDeleteDao
    abstract fun filePartDao(): FilePartDao
    abstract fun proxyDao(): ProxyDao

    companion object {
        const val NAME = "driveinfinity.db"
    }
}
