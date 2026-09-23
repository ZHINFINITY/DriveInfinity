package com.infinity.drive.core.transfer

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val transferEngineModule = module {
    singleOf(::ApkIconUploader)
    singleOf(::PartUploader)
    singleOf(::PartDownloader)
    singleOf(::TransferExecutor)
    singleOf(::TransferQueueDrainer)
}
