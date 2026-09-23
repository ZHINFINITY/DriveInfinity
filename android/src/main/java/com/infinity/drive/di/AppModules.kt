package com.infinity.drive.di

import com.infinity.drive.core.common.commonModule
import com.infinity.drive.core.crypto.cryptoModule
import com.infinity.drive.core.dispatchers.dispatchersModule
import com.infinity.drive.core.files.filesModule
import com.infinity.drive.core.files.sharedFilesModule
import com.infinity.drive.core.media.mediaModule
import com.infinity.drive.core.network.networkModule
import com.infinity.drive.core.permissions.permissionsModule
import com.infinity.drive.core.proxy.proxyModule
import com.infinity.drive.core.publish.publishModule
import com.infinity.drive.core.security.securityModule
import com.infinity.drive.core.telegram.telegramModule
import com.infinity.drive.core.transfer.transferEngineModule
import com.infinity.drive.core.transfer.transferModule
import com.infinity.drive.core.update.updateModule
import com.infinity.drive.data.local.database.daosModule
import com.infinity.drive.data.local.database.databaseModule
import com.infinity.drive.data.local.preferences.preferencesModule
import com.infinity.drive.data.repository.localDataModule
import com.infinity.drive.data.repository.repositoryModule
import com.infinity.drive.domain.usecase.useCaseModule
import com.infinity.drive.presentation.presentationModule

val appModules = listOf(
    commonModule,
    cryptoModule,
    dispatchersModule,
    filesModule,
    mediaModule,
    networkModule,
    permissionsModule,
    proxyModule,
    publishModule,
    securityModule,
    telegramModule,
    transferModule,
    updateModule,
    databaseModule,
    daosModule,
    preferencesModule,
    repositoryModule,
    localDataModule,
    sharedFilesModule,
    transferEngineModule,
    useCaseModule,
    presentationModule
)
