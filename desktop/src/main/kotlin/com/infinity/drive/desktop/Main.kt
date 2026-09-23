package com.infinity.drive.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import java.awt.Dimension
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.key.Keyer
import coil3.request.Options
import coil3.request.crossfade
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import com.infinity.drive.core.common.SafeLog
import com.infinity.drive.core.files.AppStoragePaths
import com.infinity.drive.desktop.window.WindowsTitleBar
import com.infinity.drive.domain.model.AppTheme
import com.infinity.drive.core.publish.PublishScheduler
import com.infinity.drive.domain.repository.FileRepository
import com.infinity.drive.domain.repository.SettingsRepository
import com.infinity.drive.domain.repository.TransferRepository
import com.infinity.drive.domain.repository.TrashRepository
import com.infinity.drive.core.files.PendingShare
import com.infinity.drive.core.media.ThumbnailFetcher
import com.infinity.drive.core.media.ThumbnailModel
import com.infinity.drive.core.media.ThumbnailStore
import com.infinity.drive.core.media.thumbnailCacheKey
import com.infinity.drive.desktop.di.desktopModule
import com.infinity.drive.desktop.resources.Res
import com.infinity.drive.desktop.resources.Res as DesktopRes
import com.infinity.drive.desktop.resources.app_already_running
import com.infinity.drive.desktop.resources.app_icon
import com.infinity.drive.desktop.ui.FullscreenController
import com.infinity.drive.desktop.ui.LocalFullscreenController
import com.infinity.drive.desktop.ui.ProvideDesktopPlatformActions
import com.infinity.drive.presentation.DriveInfinityApp
import com.infinity.drive.resources.Res as SharedRes
import com.infinity.drive.resources.app_name
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import javax.swing.JOptionPane
import kotlin.system.exitProcess
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.map
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

fun main() {
    SafeLog.verbose = true
    startKoin {
        modules(desktopModule)
    }
    val storagePaths = getKoin().get<AppStoragePaths>()
    if (!SingleInstanceLock(storagePaths.filesDir).acquire()) {
        JOptionPane.showMessageDialog(
            null,
            runBlocking { getString(DesktopRes.string.app_already_running) },
            runBlocking { getString(SharedRes.string.app_name) },
            JOptionPane.INFORMATION_MESSAGE
        )
        exitProcess(0)
    }
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        runCatching { getKoin().get<TransferRepository>().recoverOrphanedTransfers() }
        runCatching { getKoin().get<FileRepository>().sweepImportOrphans() }
        getKoin().get<PublishScheduler>().kick()
        runCatching { getKoin().get<TrashRepository>().repairTrashTree() }
    }
    application {
        val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
        val fullscreenController = remember {
            FullscreenController(
                isFullscreen = { windowState.placement == WindowPlacement.Fullscreen },
                toggle = {
                    windowState.placement =
                        if (windowState.placement == WindowPlacement.Fullscreen) {
                            WindowPlacement.Floating
                        } else {
                            WindowPlacement.Fullscreen
                        }
                }
            )
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = stringResource(SharedRes.string.app_name),
            icon = painterResource(Res.drawable.app_icon),
            state = windowState
        ) {
            window.minimumSize = Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT)
            setSingletonImageLoaderFactory { context ->
                ImageLoader.Builder(context)
                    .components {
                        add(ThumbnailFetcher.Factory(getKoin().get<ThumbnailStore>()))
                        add(Keyer<ThumbnailModel> { data, _: Options ->
                            thumbnailCacheKey(data.fileId)
                        })
                    }
                    .crossfade(true)
                    .build()
            }
            val settingsRepository = remember { getKoin().get<SettingsRepository>() }
            val theme by remember {
                settingsRepository.preferences.map { it.theme }.distinctUntilChanged()
            }.collectAsState(initial = AppTheme.SYSTEM)
            val systemDark = isSystemInDarkTheme()
            val darkTitleBar = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK, AppTheme.AMOLED -> true
                AppTheme.SYSTEM -> systemDark
            }
            LaunchedEffect(darkTitleBar) { WindowsTitleBar.setDark(window, darkTitleBar) }
            CompositionLocalProvider(LocalFullscreenController provides fullscreenController) {
                ProvideDesktopPlatformActions {
                    DriveInfinityApp(
                        pendingShare = getKoin().get<PendingShare>(),
                        notificationDestination = null,
                        onDestinationHandled = {}
                    )
                }
            }
        }
    }
}

private const val MIN_WINDOW_WIDTH = 480
private const val MIN_WINDOW_HEIGHT = 600
