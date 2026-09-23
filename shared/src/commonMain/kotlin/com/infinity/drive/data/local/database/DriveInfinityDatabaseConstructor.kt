package com.infinity.drive.data.local.database

import androidx.room.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object DriveInfinityDatabaseConstructor : RoomDatabaseConstructor<DriveInfinityDatabase> {
    override fun initialize(): DriveInfinityDatabase
}
