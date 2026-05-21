package com.example.booknest.di

import com.example.booknest.dataStore
import com.example.booknest.data.session.SearchHistoryManager
import com.example.booknest.data.session.SessionManager
import com.example.booknest.data.session.searchHistoryDataStore
import com.example.booknest.network.NetworkConnectivityMonitor
import com.example.booknest.port.AuthTokenAccessor
import com.example.booknest.port.DownloadNotifier
import com.example.booknest.port.SessionReader
import com.example.booknest.port.SessionWriter
import com.example.booknest.port.ToastNotifier
import com.example.booknest.ui.toast.AppToastNotifier
import com.example.booknest.viewmodel.common.UserFeedback
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreSessionModule = module {
    single { SessionManager(androidContext().dataStore) }
    single<SessionReader> { get<SessionManager>() }
    single<SessionWriter> { get<SessionManager>() }
    single<AuthTokenAccessor> { get<SessionManager>() }

    single { AppToastNotifier(get()) }
    single<ToastNotifier> { get<AppToastNotifier>() }
    single<DownloadNotifier> { get<AppToastNotifier>() }

    single { UserFeedback(get()) }

    single { NetworkConnectivityMonitor(androidContext()) }
    single { SearchHistoryManager(androidContext().searchHistoryDataStore) }
}
