package com.infinity.drive.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.infinity.drive.core.telegram.TelegramProxyType
import com.infinity.drive.data.local.database.DriveInfinityDatabase
import com.infinity.drive.data.local.entity.ProxyEntity
import com.infinity.drive.domain.model.FileQuerySpec
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopDatabaseTest {

    private fun openDatabase(file: File): DriveInfinityDatabase =
        Room.databaseBuilder<DriveInfinityDatabase>(name = file.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    @Test
    fun `proxy row survives a round trip`() = runBlocking {
        val file = File.createTempFile("driveinfinity", ".db")
        val database = openDatabase(file)
        try {
            val proxy = ProxyEntity(
                id = "p1",
                label = "Test",
                type = TelegramProxyType.SOCKS5,
                host = "127.0.0.1",
                port = 1080,
                addedAt = 42L
            )
            database.proxyDao().upsert(proxy)
            assertEquals(listOf(proxy), database.proxyDao().all())
        } finally {
            database.close()
            file.delete()
        }
    }

    @Test
    fun `raw file query runs against an empty database`() = runBlocking {
        val file = File.createTempFile("driveinfinity", ".db")
        val database = openDatabase(file)
        try {
            val ids = database.fileDao().idList(FileQueryBuilder.buildIds(FileQuerySpec()))
            assertTrue(ids.isEmpty())
        } finally {
            database.close()
            file.delete()
        }
    }
}
