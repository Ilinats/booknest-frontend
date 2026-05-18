package com.example.booknest.di

import org.koin.core.module.Module

/**
 * Application Koin graph, split by layer. Stateless use cases are [factory] definitions
 * in [domainModule]; infrastructure stays [single].
 */
val koinModules: List<Module> = listOf(
    coreSessionModule,
    networkModule,
    dataModule,
    domainModule,
    viewModelModule,
)
