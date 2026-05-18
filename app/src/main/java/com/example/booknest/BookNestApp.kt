package com.example.booknest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.booknest.di.koinModules
import com.example.booknest.utils.DebugLog
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class BookNestApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BookNestApp)
            modules(*koinModules.toTypedArray())
        }

        createNotificationChannel()
    }

    override fun newImageLoader(): ImageLoader {
        return runCatching { GlobalContext.get().get<ImageLoader>() }
            .getOrElse {
                ImageLoader.Builder(this).build()
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
            DebugLog.d("FCM", "Notification channel created: $channelId")
        }
    }
}
