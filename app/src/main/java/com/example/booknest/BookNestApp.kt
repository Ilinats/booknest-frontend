package com.example.booknest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class BookNestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Create notification channel early
        createNotificationChannel()
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

