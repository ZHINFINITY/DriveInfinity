package com.infinity.drive.presentation.platform

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import com.infinity.drive.BuildConfig
import com.infinity.drive.R
import com.infinity.drive.core.files.DocumentTreePaths
import com.infinity.drive.core.files.StandardBackupFolder
import com.infinity.drive.core.permissions.manifestPermission
import com.infinity.drive.core.permissions.openAllFilesAccess
import com.infinity.drive.core.permissions.openAppSettings
import com.infinity.drive.domain.model.AppTheme
import com.infinity.drive.domain.model.UserPreferences
import com.infinity.drive.domain.repository.SettingsRepository
import com.infinity.drive.presentation.applock.requireDeviceOwner
import com.infinity.drive.presentation.common.openLink
import com.infinity.drive.presentation.common.shareLocalFiles
import com.infinity.drive.presentation.components.FileSystemFolderPickerDialog
import com.infinity.drive.presentation.components.FolderListResult
import com.infinity.drive.presentation.components.LocalFolderItem
import com.infinity.drive.presentation.theme.DriveInfinityTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File

@Composable
fun ProvidePlatformActions(content: @Composable () -> Unit) {
    val capabilities = koinInject<PlatformCapabilities>()
    val settingsRepository = koinInject<SettingsRepository>()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val prefs by settingsRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    val darkTheme = when (prefs.theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK, AppTheme.AMOLED -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val urlOpener = remember(context) { UrlOpener { url -> openLink(context, url) } }
    val telegramLinkOpener = remember(context) { AndroidTelegramLinkOpener(context) }

    var activeFolderPickerCallback by remember { mutableStateOf<((PickResult) -> Unit)?>(null) }

    val folderCallback = remember { CallbackHolder<PickResult>() }
    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
        val path = uri?.let { DocumentTreePaths.treeToFilePath(context, it) }
        folderCallback.fire(
            when {
                uri == null -> PickResult.Canceled
                else -> PickResult.Picked(path ?: uri.toString())
            }
        )
    }
    val folderPicker = remember {
        FolderPicker { onPicked ->
            activeFolderPickerCallback = onPicked
        }
    }

    val fileCallback = remember { CallbackHolder<PickResult>() }
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val path = uri?.let { DocumentTreePaths.documentToFilePath(context, it) }
        fileCallback.fire(
            when {
                uri == null -> PickResult.Canceled
                path == null -> PickResult.Unreadable
                else -> PickResult.Picked(path)
            }
        )
    }
    val filePicker = remember {
        FilePicker { onPicked ->
            fileCallback.arm(onPicked)
            fileLauncher.launch(arrayOf("*/*"))
        }
    }

    val multiCallback = remember { CallbackHolder<List<String>>() }
    val multiLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val uris = buildList {
            data?.clipData?.let { clips ->
                repeat(clips.itemCount) { index -> add(clips.getItemAt(index).uri) }
            }
            data?.data?.let { single ->
                if (none { it == single }) add(single)
            }
        }
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        multiCallback.fire(uris.map { it.toString() })
    }
    val multiFilePicker = remember {
        MultiFilePicker { onPicked ->
            multiCallback.arm(onPicked)
            runCatching {
                multiLauncher.launch(
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        )
                    }
                )
            }.onFailure {
                multiCallback.fire(emptyList())
            }
        }
    }

    val fileSharer = remember(context) {
        FileSharer { paths, mimeType, chooserTitle ->
            shareLocalFiles(context, paths, mimeType, chooserTitle)
        }
    }

    val consentCallback = remember { CallbackHolder<Boolean>() }
    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        consentCallback.fire(result.resultCode == Activity.RESULT_OK)
    }
    val deleteConsentLauncher = remember {
        DeleteConsentLauncher { request, onResult ->
            consentCallback.arm(onResult)
            consentLauncher.launch(IntentSenderRequest.Builder(request).build())
        }
    }

    val permissionCallback = remember { CallbackHolder<Unit>() }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionCallback.fire(Unit) }
    val permissionRequester = remember {
        PermissionRequester { permissions, onDone ->
            val manifest = permissions.mapNotNull { it.manifestPermission }
            if (manifest.isEmpty()) {
                onDone()
            } else {
                permissionCallback.arm { onDone() }
                permissionLauncher.launch(manifest.toTypedArray())
            }
        }
    }

    val systemScreens = remember(context) {
        object : SystemScreens {
            override fun openAppSettings() = openAppSettings(context)
            override fun openAllFilesAccess() = openAllFilesAccess(context)
        }
    }

    val deviceOwnerGate = remember(activity) {
        DeviceOwnerGate { title, subtitle, onDenied, onConfirmed ->
            requireDeviceOwner(
                activity = activity as? FragmentActivity,
                title = title,
                subtitle = subtitle,
                onDenied = onDenied,
                onConfirmed = onConfirmed
            )
        }
    }

    val standardFolders = remember {
        StandardBackupFolder.entries.map { StandardFolderOption(it.labelRes, it.path) }
    }
    val aboutIcon = remember(context) {
        BitmapFactory.decodeResource(context.resources, R.drawable.about_icon)?.asImageBitmap()
    }
    val appIcon: @Composable (Modifier) -> Unit = { modifier ->
        aboutIcon?.let { bitmap ->
            Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
        }
    }

    val externalStorageRoot = remember {
        Environment.getExternalStorageDirectory().absolutePath
    }

    CompositionLocalProvider(
        LocalAppIcon provides appIcon,
        LocalStandardFolders provides standardFolders,
        LocalUrlOpener provides urlOpener,
        LocalTelegramLinkOpener provides telegramLinkOpener,
        LocalFolderPicker provides folderPicker,
        LocalFilePicker provides filePicker,
        LocalMultiFilePicker provides multiFilePicker,
        LocalFileSharer provides fileSharer,
        LocalDeleteConsentLauncher provides deleteConsentLauncher,
        LocalPermissionRequester provides permissionRequester,
        LocalSystemScreens provides systemScreens,
        LocalDeviceOwnerGate provides deviceOwnerGate,
        LocalAppVersion provides BuildConfig.VERSION_NAME,
        LocalPlatformScreens provides AndroidPlatformScreens,
        LocalPlatformCapabilities provides capabilities
    ) {
        content()

        activeFolderPickerCallback?.let { callback ->
                DriveInfinityTheme(
                    darkTheme = darkTheme,
                    dynamicColor = prefs.dynamicColor,
                    amoled = prefs.theme == AppTheme.AMOLED
                ) {
                FileSystemFolderPickerDialog(
                    rootPath = externalStorageRoot,
                    listSubfolders = { path ->
                        withContext(Dispatchers.IO) {
                            val dir = File(path)
                            val javaFiles = runCatching { dir.listFiles() }.getOrNull()
                            if (javaFiles == null) {
                                FolderListResult.Unreadable
                            } else {
                                val items = javaFiles.filter { it.isDirectory }
                                    .sortedBy { it.name.lowercase() }
                                    .map { LocalFolderItem(it.name, it.absolutePath) }
                                FolderListResult.Success(items)
                            }
                        }
                    },
                    createSubfolder = { parentPath, name ->
                        withContext(Dispatchers.IO) {
                            runCatching { File(parentPath, name).mkdir() }.getOrDefault(false)
                        }
                    },
                    onUseSaf = {
                        val cb = callback
                        activeFolderPickerCallback = null
                        folderCallback.arm(cb)
                        folderLauncher.launch(null)
                    },
                    onConfirm = { path ->
                        activeFolderPickerCallback = null
                        callback(PickResult.Picked(path))
                    },
                    onDismiss = {
                        activeFolderPickerCallback = null
                        callback(PickResult.Canceled)
                    }
                )
            }
        }
    }
}

private class CallbackHolder<T> {

    private var pending: ((T) -> Unit)? = null

    fun arm(callback: (T) -> Unit) {
        pending = callback
    }

    fun fire(value: T) {
        val callback = pending
        pending = null
        callback?.invoke(value)
    }
}
