package com.infinity.drive.core.proxy

import com.infinity.drive.core.common.AppResult
import com.infinity.drive.core.common.SafeLog
import com.infinity.drive.core.telegram.TelegramConnectionState
import com.infinity.drive.domain.repository.ProxyRepository
import com.infinity.drive.domain.repository.SettingsRepository
import com.infinity.drive.domain.repository.TelegramAuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A blocked proxy looks exactly like a slow one: Telegram simply never
 * connects. When the connection stays down long enough to rule out a slow
 * start, the next saved proxy is tried, and then the one after that, until
 * either one answers or the list runs out.
 */
class ProxyFailover(
    private val authRepository: TelegramAuthRepository,
    private val proxyRepository: ProxyRepository,
    private val settingsRepository: SettingsRepository
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            authRepository.connectionState
                .collectLatest { state ->
                    if (state != TelegramConnectionState.CONNECTING) return@collectLatest
                    if (!routingEnabled()) return@collectLatest
                    var tried = 0
                    while (isActive) {
                        delay(SETTLE_MS)
                        if (!routingEnabled()) return@collectLatest
                        val limit = proxyRepository.observeProxies().first().size
                        if (tried >= limit) {
                            SafeLog.w(TAG, "Every saved proxy was tried without connecting")
                            return@collectLatest
                        }
                        tried++
                        val rotated = proxyRepository.rotate()
                        if (rotated !is AppResult.Success || !rotated.value) return@collectLatest
                    }
                }
        }
    }

    private suspend fun routingEnabled(): Boolean =
        settingsRepository.preferences.first().proxyEnabled

    private companion object {
        const val TAG = "ProxyFailover"
        const val SETTLE_MS = 20_000L
    }
}
