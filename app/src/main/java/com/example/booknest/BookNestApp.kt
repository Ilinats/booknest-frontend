package com.example.booknest

import android.app.Application

class BookNestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // AuthManager will be initialized when needed in MainActivity
    }
}

