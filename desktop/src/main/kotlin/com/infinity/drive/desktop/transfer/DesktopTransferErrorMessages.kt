package com.infinity.drive.desktop.transfer

import com.infinity.drive.core.transfer.TransferErrorMessages
import com.infinity.drive.desktop.resources.Res
import com.infinity.drive.desktop.resources.transfer_error_decryption_failed
import com.infinity.drive.desktop.resources.transfer_error_download_ended
import com.infinity.drive.desktop.resources.transfer_error_download_stalled
import com.infinity.drive.desktop.resources.transfer_error_downloaded_missing
import com.infinity.drive.desktop.resources.transfer_error_file_record_missing
import com.infinity.drive.desktop.resources.transfer_error_key_missing
import com.infinity.drive.desktop.resources.transfer_error_local_file_gone
import com.infinity.drive.desktop.resources.transfer_error_no_file_reference
import com.infinity.drive.desktop.resources.transfer_error_no_local_copy
import com.infinity.drive.desktop.resources.transfer_error_no_remote_copy
import com.infinity.drive.desktop.resources.transfer_error_save_failed
import com.infinity.drive.desktop.resources.transfer_error_upload_ended
import com.infinity.drive.desktop.resources.transfer_error_upload_stalled
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class DesktopTransferErrorMessages : TransferErrorMessages {

    override val keyMissing by resource(Res.string.transfer_error_key_missing)

    override val noFileReference by resource(Res.string.transfer_error_no_file_reference)

    override val fileRecordMissing by resource(Res.string.transfer_error_file_record_missing)

    override val noLocalCopy by resource(Res.string.transfer_error_no_local_copy)

    override val localFileGone by resource(Res.string.transfer_error_local_file_gone)

    override val uploadEnded by resource(Res.string.transfer_error_upload_ended)

    override val uploadStalled by resource(Res.string.transfer_error_upload_stalled)

    override val downloadEnded by resource(Res.string.transfer_error_download_ended)

    override val downloadStalled by resource(Res.string.transfer_error_download_stalled)

    override val downloadedMissing by resource(Res.string.transfer_error_downloaded_missing)

    override val noRemoteCopy by resource(Res.string.transfer_error_no_remote_copy)

    override val decryptionFailed by resource(Res.string.transfer_error_decryption_failed)

    override val saveFailed by resource(Res.string.transfer_error_save_failed)

    private fun resource(resource: StringResource) = lazy {
        runBlocking { getString(resource) }
    }
}
