package com.example.booknest.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.booknest.MainActivity
import com.example.booknest.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BookNestMessagingService : FirebaseMessagingService() {
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token will be registered when user logs in via NotificationViewModel
        // Store token temporarily if needed
        println("FCM Token: $token")
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        println("FCM: Message received - data: ${remoteMessage.data}, notification: ${remoteMessage.notification}")
        
        // When app is in foreground, onMessageReceived is called for all messages
        // When app is in background/closed, onMessageReceived is ONLY called for data-only messages
        // Messages with notification payload are automatically displayed by FCM
        
        // Extract data from payload
        val notificationId = remoteMessage.data["notificationId"]
        val type = remoteMessage.data["type"]
        val bookId = remoteMessage.data["bookId"]
        val applicationId = remoteMessage.data["applicationId"]
        val relatedUserId = remoteMessage.data["relatedUserId"]
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "BookNest"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: ""
        
        // Always show notification when onMessageReceived is called
        // This handles:
        // 1. App in foreground: Show notification manually
        // 2. App in background/closed with data-only message: Show notification manually
        // 3. App in background/closed with notification payload: FCM shows it automatically (this method won't be called)
        showNotification(
            title = title,
            body = body,
            notificationId = notificationId,
            type = type,
            bookId = bookId,
            applicationId = applicationId,
            relatedUserId = relatedUserId
        )
    }
    
    private fun showNotification(
        title: String,
        body: String,
        notificationId: String?,
        type: String?,
        bookId: String?,
        applicationId: String?,
        relatedUserId: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add notification data to intent
            notificationId?.let { putExtra("notificationId", it) }
            type?.let { putExtra("notificationType", it) }
            bookId?.let { putExtra("bookId", it) }
            applicationId?.let { putExtra("applicationId", it) }
            relatedUserId?.let { putExtra("relatedUserId", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = "booknest_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // You may need to add this icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "BookNest Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for BookNest app"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notificationIdInt = notificationId?.hashCode() ?: System.currentTimeMillis().toInt()
        try {
            notificationManager.notify(notificationIdInt, notificationBuilder.build())
            println("FCM: Notification shown successfully with ID: $notificationIdInt")
        } catch (e: Exception) {
            println("FCM: Error showing notification: ${e.message}")
            e.printStackTrace()
        }
    }
}

