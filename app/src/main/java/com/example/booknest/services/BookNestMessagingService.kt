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
import com.example.booknest.navigation.EXTRA_APPLICATION_ID
import com.example.booknest.navigation.EXTRA_BOOK_ID
import com.example.booknest.navigation.EXTRA_NOTIFICATION_ID
import com.example.booknest.navigation.EXTRA_NOTIFICATION_TYPE
import com.example.booknest.navigation.EXTRA_RELATED_USER_ID
import com.example.booknest.utils.DebugLog
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BookNestMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        DebugLog.d("FCM", "onNewToken (length=${token.length})")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        DebugLog.d(
            "FCM",
            "onMessageReceived keys=${remoteMessage.data.keys} hasNotification=${remoteMessage.notification != null}"
        )

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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            notificationId?.let { putExtra(EXTRA_NOTIFICATION_ID, it) }
            type?.let { putExtra(EXTRA_NOTIFICATION_TYPE, it) }
            bookId?.let { putExtra(EXTRA_BOOK_ID, it) }
            applicationId?.let { putExtra(EXTRA_APPLICATION_ID, it) }
            relatedUserId?.let { putExtra(EXTRA_RELATED_USER_ID, it) }
        }

        val pendingIntentRequestCode = notificationId?.hashCode()
            ?: (type?.hashCode() ?: System.currentTimeMillis().toInt())
        val pendingIntent = PendingIntent.getActivity(
            this,
            pendingIntentRequestCode,
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
            DebugLog.d("FCM", "Notification shown id=$notificationIdInt")
        } catch (e: Exception) {
            DebugLog.w("FCM", "Error showing notification: ${e.message}", e)
        }
    }
}

