package com.example.booknest.navigation

import android.content.Intent

data class NotificationLaunchExtras(
    val notificationId: String?,
    val notificationType: String?,
) {
    val hasNotificationDeepLink: Boolean
        get() = notificationId != null || notificationType != null
}

fun readNotificationLaunchExtras(intent: Intent): NotificationLaunchExtras =
    NotificationLaunchExtras(
        notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID),
        notificationType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE),
    )

fun Intent.consumeNotificationLaunchExtras() {
    removeExtra(EXTRA_NOTIFICATION_ID)
    removeExtra(EXTRA_NOTIFICATION_TYPE)
    removeExtra(EXTRA_BOOK_ID)
    removeExtra(EXTRA_APPLICATION_ID)
    removeExtra(EXTRA_RELATED_USER_ID)
}

const val EXTRA_NOTIFICATION_ID: String = "notificationId"
const val EXTRA_NOTIFICATION_TYPE: String = "notificationType"
const val EXTRA_BOOK_ID: String = "bookId"
const val EXTRA_APPLICATION_ID: String = "applicationId"
const val EXTRA_RELATED_USER_ID: String = "relatedUserId"
