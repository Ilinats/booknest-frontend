package com.example.booknest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.booknest.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class BookNestApp : Application(), ImageLoaderFactory {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        SessionManager.getInstance(dataStore)

        applicationScope.launch {
            SessionManager.setTokens()
        }

        startKoin {
            androidContext(this@BookNestApp)
            modules(appModule)
        }

        createNotificationChannel()
    }

    override fun newImageLoader(): ImageLoader {
        return try {
            GlobalContext.get().get<ImageLoader>()
        } catch (e: Exception) {
            ImageLoader.Builder(this)
                .build()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "booknest_notifications"
            val channelName = "BookNest Notifications"
            val channelDescription = "Notifications for BookNest app"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            println("FCM: Notification channel created: $channelId")
        }
    }
}
