package com.infinity.drive.desktop.ui

import com.infinity.drive.presentation.platform.TelegramLinkOpener
import com.sun.jna.Platform
import java.awt.Desktop
import java.net.URI

/**
 * Windows registers the tg: protocol in the registry when a Telegram app is
 * installed, so a one-time probe answers whether a login link can be
 * confirmed on this machine without launching anything.
 */
class DesktopTelegramLinkOpener : TelegramLinkOpener {

    override val canOpenTelegram: Boolean by lazy {
        when {
            Platform.isWindows() -> runCatching {
                ProcessBuilder("reg", "query", "HKEY_CLASSES_ROOT\\tg")
                    .redirectErrorStream(true)
                    .start()
                    .apply { inputStream.readBytes() }
                    .waitFor() == 0
            }.getOrDefault(false)
            Platform.isLinux() -> commandAvailable("xdg-open")
            else -> Desktop.isDesktopSupported()
        }
    }

    override fun open(link: String): Boolean = runCatching {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(link))
        } else if (Platform.isLinux()) {
            ProcessBuilder("xdg-open", link).start()
        } else {
            return false
        }
        true
    }.getOrDefault(false)

    private fun commandAvailable(command: String): Boolean = runCatching {
        ProcessBuilder("sh", "-c", "command -v $command")
            .redirectErrorStream(true)
            .start()
            .apply { inputStream.readBytes() }
            .waitFor() == 0
    }.getOrDefault(false)
}
