package com.infinity.drive.core.update

import com.infinity.drive.BuildConfig
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val updateModule = module {
    single { UpdateChecker(BuildConfig.VERSION_NAME) }
    workerOf(::UpdateCheckWorker)
}
