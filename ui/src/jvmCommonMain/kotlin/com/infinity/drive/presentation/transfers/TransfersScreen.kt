package com.infinity.drive.presentation.transfers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infinity.drive.resources.Res
import com.infinity.drive.resources.common_actions
import com.infinity.drive.resources.common_back
import com.infinity.drive.resources.common_cancel
import com.infinity.drive.resources.common_clear
import com.infinity.drive.resources.common_dismiss
import com.infinity.drive.resources.common_pause
import com.infinity.drive.resources.common_resume
import com.infinity.drive.resources.common_retry
import com.infinity.drive.resources.transfer_stage_joining
import com.infinity.drive.resources.transfer_stage_sealing
import com.infinity.drive.resources.transfer_type_backup
import com.infinity.drive.resources.transfer_type_download
import com.infinity.drive.resources.transfer_type_restore
import com.infinity.drive.resources.transfer_type_upload
import com.infinity.drive.resources.transfers
import com.infinity.drive.resources.transfers_cancel
import com.infinity.drive.resources.transfers_cancel_transfers
import com.infinity.drive.resources.transfers_clear_finished
import com.infinity.drive.resources.transfers_clear_finished_transfers
import com.infinity.drive.resources.transfers_completed_canceled_entries_removed
import com.infinity.drive.resources.transfers_failed
import com.infinity.drive.resources.transfers_failed_downloads
import com.infinity.drive.resources.transfers_failed_transfers
import com.infinity.drive.resources.transfers_failed_uploads
import com.infinity.drive.resources.transfers_more_queued
import com.infinity.drive.resources.transfers_no_transfers
import com.infinity.drive.resources.transfers_pause
import com.infinity.drive.resources.transfers_queued_running_paused_failed
import com.infinity.drive.resources.transfers_resume
import com.infinity.drive.resources.transfers_section_active
import com.infinity.drive.resources.transfers_section_finished
import com.infinity.drive.resources.transfers_section_paused
import com.infinity.drive.resources.transfers_uploads_downloads_appear_here
import com.infinity.drive.domain.model.TransferStage
import com.infinity.drive.domain.model.TransferState
import com.infinity.drive.domain.model.TransferTask
import com.infinity.drive.domain.model.TransferType
import com.infinity.drive.domain.model.ProgressBarStyle
import com.infinity.drive.core.transfer.FileParts
import com.infinity.drive.presentation.common.Formatters
import com.infinity.drive.presentation.common.add
import com.infinity.drive.presentation.components.ConfirmDialog
import com.infinity.drive.presentation.components.EmptyState
import com.infinity.drive.presentation.components.liftedTopAppBarColors
import com.infinity.drive.presentation.components.rememberToolbarLift

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    onBack: () -> Unit,
    viewModel: TransfersViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmClearFinished by remember { mutableStateOf(false) }
    var confirmCancelAll by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    if (confirmClearFinished) {
        ConfirmDialog(
            title = stringResource(Res.string.transfers_clear_finished_transfers),
            message = stringResource(Res.string.transfers_completed_canceled_entries_removed),
            confirmLabel = stringResource(Res.string.common_clear),
            onConfirm = {
                confirmClearFinished = false
                viewModel.clearFinished()
            },
            onDismiss = { confirmClearFinished = false }
        )
    }

    if (confirmCancelAll) {
        ConfirmDialog(
            title = stringResource(Res.string.transfers_cancel_transfers),
            message = stringResource(Res.string.transfers_queued_running_paused_failed),
            confirmLabel = stringResource(Res.string.transfers_cancel),
            destructive = true,
            onConfirm = {
                confirmCancelAll = false
                viewModel.cancelAll()
            },
            onDismiss = { confirmCancelAll = false }
        )
    }

    val listState = rememberLazyListState()
    val lifted by rememberToolbarLift(listState)

    Scaffold(
        topBar = {
            TopAppBar(
                colors = liftedTopAppBarColors(lifted),
                title = { Text(stringResource(Res.string.transfers)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                },
                actions = {
                    if (state.active.isNotEmpty()) {
                        IconButton(onClick = viewModel::pauseAll) {
                            Icon(
                                Icons.Filled.Pause,
                                contentDescription = stringResource(Res.string.transfers_pause)
                            )
                        }
                    } else if (state.paused.isNotEmpty()) {
                        IconButton(onClick = viewModel::resumeAll) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = stringResource(Res.string.transfers_resume)
                            )
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = stringResource(Res.string.common_actions)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.transfers_cancel)) },
                            enabled = state.active.isNotEmpty() ||
                                    state.paused.isNotEmpty() ||
                                    state.failed.isNotEmpty(),
                            onClick = {
                                showMenu = false
                                confirmCancelAll = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.transfers_clear_finished)) },
                            enabled = state.completed.isNotEmpty(),
                            onClick = {
                                showMenu = false
                                confirmClearFinished = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        val isEmpty = state.active.isEmpty() && state.paused.isEmpty() &&
                state.failed.isEmpty() && state.completed.isEmpty()
        if (isEmpty && !state.loading) {
            EmptyState(
                icon = Icons.Outlined.SwapVert,
                title = stringResource(Res.string.transfers_no_transfers),
                description = stringResource(Res.string.transfers_uploads_downloads_appear_here),
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        val activeTitle = stringResource(Res.string.transfers_section_active)
        val pausedTitle = stringResource(Res.string.transfers_section_paused)
        val finishedTitle = stringResource(Res.string.transfers_section_finished)
        val failedTitle = failedSectionTitle(state.failed)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding.add(horizontal = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            section("active", activeTitle, state.active, state.activeTotal) { transfer ->
                TransferRow(
                    transfer = transfer,
                    progressBarStyle = state.progressBarStyle,
                    primaryIcon = Icons.Filled.Pause,
                    primaryLabel = stringResource(Res.string.common_pause),
                    onPrimary = { viewModel.pause(transfer.id) },
                    onCancel = { viewModel.cancel(transfer.id) }
                )
            }
            section("paused", pausedTitle, state.paused, state.pausedTotal) { transfer ->
                TransferRow(
                    transfer = transfer,
                    progressBarStyle = state.progressBarStyle,
                    primaryIcon = Icons.Filled.PlayArrow,
                    primaryLabel = stringResource(Res.string.common_resume),
                    onPrimary = { viewModel.resume(transfer.id) },
                    onCancel = { viewModel.cancel(transfer.id) }
                )
            }
            section("failed", failedTitle, state.failed, state.failedTotal) { transfer ->
                TransferRow(
                    transfer = transfer,
                    progressBarStyle = state.progressBarStyle,
                    primaryIcon = Icons.Filled.Refresh,
                    primaryLabel = stringResource(Res.string.common_retry),
                    onPrimary = { viewModel.retry(transfer.id) },
                    onCancel = { viewModel.cancel(transfer.id) }
                )
            }
            section("finished", finishedTitle, state.completed, state.completedTotal) { transfer ->
                TransferRow(transfer = transfer, progressBarStyle = state.progressBarStyle)
            }
        }
    }
}

private fun LazyListScope.section(
    sectionKey: String,
    title: String,
    transfers: List<TransferTask>,
    total: Int,
    content: @Composable (TransferTask) -> Unit
) {
    if (transfers.isEmpty()) return
    item(key = "header-$sectionKey") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
    items(transfers, key = { "$sectionKey-${it.id}" }) { transfer ->
        content(transfer)
    }
    val hidden = total - transfers.size
    if (hidden > 0) {
        item(key = "more-$sectionKey") {
            Text(
                text = pluralStringResource(Res.plurals.transfers_more_queued, hidden, hidden),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp)
            )
        }
    }
}

/** Names the direction so a failure never reads as a vague "transfer". */
@Composable
private fun failedSectionTitle(failed: List<TransferTask>): String {
    val uploads = failed.count {
        it.type == TransferType.UPLOAD || it.type == TransferType.BACKUP
    }
    return when {
        failed.isEmpty() -> stringResource(Res.string.transfers_failed)
        uploads == failed.size -> stringResource(Res.string.transfers_failed_uploads)
        uploads == 0 -> stringResource(Res.string.transfers_failed_downloads)
        else -> stringResource(Res.string.transfers_failed_transfers)
    }
}

@Composable
private fun transferRate(transfer: TransferTask): String {
    val stage = transfer.stage
    if (stage != null) {
        return stringResource(
            when (stage) {
                TransferStage.SEALING -> Res.string.transfer_stage_sealing
                TransferStage.JOINING -> Res.string.transfer_stage_joining
            }
        )
    }
    if (transfer.state != TransferState.RUNNING || transfer.speedBytesPerSecond <= 0) return ""
    val speed = Formatters.speed(transfer.speedBytesPerSecond)
    val eta = transfer.etaSeconds?.let { Formatters.eta(it) } ?: return speed
    return "$speed · $eta"
}

@Composable
private fun TransferRow(
    transfer: TransferTask,
    progressBarStyle: ProgressBarStyle,
    primaryIcon: ImageVector? = null,
    primaryLabel: String? = null,
    onPrimary: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TransferThumbnail(transfer = transfer)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transfer.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append(
                                when (transfer.type) {
                                    TransferType.UPLOAD -> stringResource(Res.string.transfer_type_upload)
                                    TransferType.DOWNLOAD -> stringResource(Res.string.transfer_type_download)
                                    TransferType.BACKUP -> stringResource(Res.string.transfer_type_backup)
                                    TransferType.RESTORE -> stringResource(Res.string.transfer_type_restore)
                                }
                            )
                            append(" · ")
                            append(Formatters.bytes(transfer.sizeBytes))
                            transfer.errorMessage?.let {
                                append(" · ")
                                append(it)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (transfer.state == TransferState.FAILED) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (transfer.state == TransferState.RUNNING ||
                transfer.state == TransferState.PAUSED
            ) {
                Spacer(Modifier.height(10.dp))
                ChunkProgressBar(
                    progress = transfer.progress,
                    style = progressBarStyle,
                    chunkCount = FileParts.countFor(transfer.sizeBytes),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Formatters.bytes(transfer.transferredBytes)} / ${Formatters.bytes(transfer.sizeBytes)} · ${Formatters.percent(transfer.progress)} · ${chunkLabel(transfer)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = transferRate(transfer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            if (primaryIcon != null && onPrimary != null || onCancel != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (primaryIcon != null && onPrimary != null) {
                        IconButton(onClick = onPrimary) {
                            Icon(primaryIcon, contentDescription = primaryLabel)
                        }
                    }
                    if (onCancel != null) {
                        IconButton(onClick = onCancel) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = if (transfer.state.isTerminal) {
                                    stringResource(Res.string.common_dismiss)
                                } else {
                                    stringResource(Res.string.common_cancel)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChunkProgressBar(
    progress: Float,
    style: ProgressBarStyle,
    chunkCount: Int,
    modifier: Modifier = Modifier
) {
    val markerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    Box(modifier = modifier.height(10.dp)) {
        if (style == ProgressBarStyle.WAVY) {
            LinearWavyProgressIndicator(
                progress = { progress },
                amplitude = { 1f },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (chunkCount > 1) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (index in 1 until chunkCount) {
                    val x = size.width * index / chunkCount
                    drawLine(
                        color = markerColor,
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}

private fun chunkLabel(transfer: TransferTask): String {
    val total = FileParts.countFor(transfer.sizeBytes)
    val completed = if (transfer.transferredBytes >= transfer.sizeBytes) {
        total
    } else {
        ((transfer.transferredBytes / FileParts.PART_SIZE) + 1).toInt().coerceIn(1, total)
    }
    return "chunk $completed/$total"
}
