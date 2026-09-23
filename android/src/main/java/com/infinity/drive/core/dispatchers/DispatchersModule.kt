package com.infinity.drive.core.dispatchers

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dispatchersModule = module {
    singleOf(::DefaultDispatcherProvider) bind DispatcherProvider::class
}
