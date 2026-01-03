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
        println("FCM Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        println("FCM: Message received - data: ${remoteMessage.data}, notification: ${remoteMessage.notification}")

        val notificationId = remoteMessage.data["notificationId"]
        val type = remoteMessage.data["type"]
        val bookId = remoteMessage.data["bookId"]
        val applicationId = remoteMessage.data["applicationId"]
        val relatedUserId = remoteMessage.data["relatedUserId"]
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "BookNest"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: ""

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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

