package com.example.booknest

import android.app.Application
import com.example.booknest.network.TokenCache
import com.example.booknest.network.TokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookNestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStorage.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            TokenStorage.getTokenFlow().collect { token ->
                TokenCache.accessToken = token
            }
        }
    }
}

