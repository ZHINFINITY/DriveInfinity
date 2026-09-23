package com.infinity.drive.domain.repository

import com.infinity.drive.core.common.AppResult
import com.infinity.drive.core.proxy.ProxyProbeResult
import com.infinity.drive.domain.model.ProxyServer
import kotlinx.coroutines.flow.Flow

interface ProxyRepository {

    fun observeProxies(): Flow<List<ProxyServer>>

    suspend fun save(proxy: ProxyServer): AppResult<Unit>

    suspend fun delete(id: String): AppResult<Unit>

    /** Picks which saved proxy to route through, and applies it now. */
    suspend fun select(id: String): AppResult<Unit>

    /** Turns routing on or off without forgetting the saved list. */
    suspend fun setEnabled(enabled: Boolean): AppResult<Unit>

    /** Reapplies the current choice, for a client that has just started. */
    suspend fun applyActive()

    /**
     * Moves to the next saved proxy and reconnects through it. Returns false
     * when there is nothing else to try.
     */
    suspend fun rotate(): AppResult<Boolean>

    /** Reports how far a check through [proxy] got. */
    suspend fun test(proxy: ProxyServer): AppResult<ProxyProbeResult>
}
