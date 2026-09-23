package com.infinity.drive.core.transfer

import com.infinity.drive.core.files.AppStoragePaths
import com.infinity.drive.core.common.SafeLog
import com.infinity.drive.core.crypto.CryptoKeys
import com.infinity.drive.core.crypto.StreamCrypto
import com.infinity.drive.core.crypto.WrappedKeyRepository
import com.infinity.drive.core.dispatchers.DispatcherProvider
import com.infinity.drive.core.media.ThumbnailStore
import com.infinity.drive.core.telegram.TelegramClient
import com.infinity.drive.core.telegram.TelegramUploadEvent
import com.infinity.drive.data.local.dao.FilePartDao
import com.infinity.drive.data.local.entity.FileEntity
import com.infinity.drive.data.local.entity.FilePartEntity
import com.infinity.drive.data.remote.telegram.ManifestCodec
import com.infinity.drive.data.remote.telegram.RemoteFileManifest
import com.infinity.drive.core.files.MimeTypes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

/**
 * Uploads a file Telegram will not take whole, one part at a time.
 *
 * Each part is written to scratch space, sent, then deleted before the next is
 * built, so an encrypted upload never needs room for a second copy of the whole
 * file. Parts already recorded are skipped, which is what makes a paused or
 * interrupted upload continue from the part it stopped on rather than the start.
 */
class PartUploader(
    private val storagePaths: AppStoragePaths,
    private val telegramClient: TelegramClient,
    private val filePartDao: FilePartDao,
    private val manifestCodec: ManifestCodec,
    private val streamCrypto: StreamCrypto,
    private val wrappedKeyRepository: WrappedKeyRepository,
    private val thumbnailStore: ThumbnailStore,
    private val dispatchers: DispatcherProvider,
    private val apkIconUploader: ApkIconUploader
) {

    sealed interface Event {
        data class Progress(val transferredBytes: Long) : Event
        data class PartDone(val partIndex: Int, val partCount: Int) : Event
        data class Sealing(val partIndex: Int) : Event
        data class Completed(val parts: List<FilePartEntity>) : Event
    }

    fun upload(
        entity: FileEntity,
        source: File,
        chatId: Long,
        manifest: RemoteFileManifest,
        encrypt: Boolean
    ): Flow<Event> = flow {
        val totalSize = source.length()
        val partCount = FileParts.countFor(totalSize)
        val done = filePartDao.partsOf(entity.id)
            .filter { it.messageId != null }
            .associateBy { it.partIndex }
            .toMutableMap()

        var uploadedBefore = done.values.sumOf { it.plainSize }
        emit(Event.Progress(uploadedBefore))

        for (index in 0 until partCount) {
            if (done.containsKey(index)) continue

            val plainOffset = FileParts.offsetOf(index)
            val plainSize = FileParts.sizeOf(index, totalSize)
            val scratch = File(scratchDir(), "${entity.id}.${index}.part")

            try {
                if (encrypt) emit(Event.Sealing(index))
                withContext(dispatchers.io) {
                    writePart(source, plainOffset, plainSize, scratch, encrypt)
                }
                if (encrypt) emit(Event.PartDone(index, partCount))

                val iconFileId = if (index == 0) {
                    apkIconUploader.uploadIconIfApk(entity, chatId, encrypt)
                } else null

                val partManifest = manifest.copy(
                    version = RemoteFileManifest.PART_VERSION,
                    partCount = partCount,
                    partIndex = index,
                    partOffset = plainOffset,
                    partSize = plainSize,
                    iconFileId = if (index == 0) iconFileId ?: manifest.iconFileId else null
                )
                val partName = if (encrypt) {
                    FileParts.nameFor(entity.id, index)
                } else {
                    FileParts.nameFor(entity.name, index)
                }

                var stored: FilePartEntity? = null
                val alreadySent = uploadedBefore
                telegramClient.uploadDocument(
                    chatId = chatId,
                    localPath = scratch.absolutePath,
                    fileName = partName,
                    mimeType = if (encrypt) OCTET_STREAM else entity.mimeType,
                    caption = manifestCodec.encode(partManifest, encrypt),
                    thumbnailPath = null
                ).collect { event ->
                    when (event) {
                        is TelegramUploadEvent.Started -> Unit
                        is TelegramUploadEvent.Progress -> {
                            val within = event.transferredBytes
                                .coerceAtMost(plainSize)
                            emit(Event.Progress(alreadySent + within))
                        }

                        is TelegramUploadEvent.Completed -> {
                            val document = event.document
                            stored = FilePartEntity(
                                fileId = entity.id,
                                partIndex = index,
                                chatId = document.chatId,
                                messageId = document.messageId,
                                remoteFileId = document.remoteFileId,
                                remoteUniqueId = document.uniqueFileId,
                                plainOffset = plainOffset,
                                plainSize = plainSize,
                                storedSize = scratch.length(),
                                uploadedAt = System.currentTimeMillis()
                            )
                        }
                    }
                }

                val part = stored ?: error("Part ${index + 1} did not finish")
                filePartDao.upsert(part)
                done[index] = part
                uploadedBefore += plainSize
                emit(Event.Progress(uploadedBefore))
                emit(Event.PartDone(index, partCount))
            } finally {
                withContext(NonCancellable + dispatchers.io) { scratch.delete() }
            }
        }

        emit(Event.Completed(done.values.sortedBy { it.partIndex }))
    }

    /** Removes whatever reached Telegram, for a canceled or deleted upload. */
    suspend fun discardParts(fileId: String) {
        val parts = filePartDao.partsOf(fileId)
        for ((chatId, group) in parts.groupBy { it.chatId }) {
            if (chatId == null) continue
            val messageIds = group.mapNotNull { it.messageId }
            if (messageIds.isEmpty()) continue
            runCatching { telegramClient.deleteMessages(chatId, messageIds) }
                .onFailure { SafeLog.w(TAG, "Could not drop ${messageIds.size} orphan parts", it) }
        }
        filePartDao.deleteFor(listOf(fileId))
    }

    private fun writePart(
        source: File,
        plainOffset: Long,
        plainSize: Long,
        target: File,
        encrypt: Boolean
    ) {
        target.parentFile?.mkdirs()
        FileChannel.open(source.toPath(), StandardOpenOption.READ).use { channel ->
            channel.position(plainOffset)
            Channels.newInputStream(channel).use { input ->
                val ranged = RangeInputStream(input, plainSize)
                target.outputStream().buffered().use { output ->
                    if (encrypt) {
                        val key = wrappedKeyRepository.getOrCreate(CryptoKeys.CONTENT)
                        streamCrypto.encryptStream(key, ranged, output)
                    } else {
                        ranged.copyTo(output)
                    }
                    check(ranged.bytesRead == plainSize) {
                        "Source changed or ended while reading part: expected $plainSize bytes, read ${ranged.bytesRead}"
                    }
                }
            }
        }
    }

    private fun scratchDir(): File = File(storagePaths.cacheDir, SCRATCH_DIR).apply { mkdirs() }

    private companion object {
        const val TAG = "PartUploader"
        const val SCRATCH_DIR = "parts"
        const val OCTET_STREAM = "application/octet-stream"
    }
}

/** Reads at most [limit] bytes, so one part cannot run into the next. */
private class RangeInputStream(
    private val delegate: InputStream,
    private val limit: Long
) : InputStream() {

    private var read = 0L
    val bytesRead: Long get() = read

    override fun read(): Int {
        if (read >= limit) return -1
        val value = delegate.read()
        if (value != -1) read++
        return value
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (read >= limit) return -1
        val allowed = minOf(length.toLong(), limit - read).toInt()
        val count = delegate.read(buffer, offset, allowed)
        if (count > 0) read += count
        return count
    }

    override fun available(): Int = minOf(delegate.available().toLong(), limit - read).toInt()
}
