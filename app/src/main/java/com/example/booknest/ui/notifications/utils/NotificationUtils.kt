package com.example.booknest.ui.notifications.utils

import android.os.Build
import androidx.navigation.NavController
import com.example.booknest.domain.model.enums.NotificationType
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.navigation.BottomBarScreen
import com.example.booknest.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun formatNotificationTime(createdAt: String): String {
    val createdAtMillis = parseCreatedAtMillis(createdAt) ?: return createdAt
    val now = System.currentTimeMillis()
    val minutesAgo = TimeUnit.MILLISECONDS.toMinutes(now - createdAtMillis)
    val hoursAgo = TimeUnit.MILLISECONDS.toHours(now - createdAtMillis)
    val daysAgo = TimeUnit.MILLISECONDS.toDays(now - createdAtMillis)

    return when {
        minutesAgo < 1 -> "Just now"
        minutesAgo < 60 -> "$minutesAgo ${if (minutesAgo == 1L) "minute" else "minutes"} ago"
        hoursAgo < 24 -> "$hoursAgo ${if (hoursAgo == 1L) "hour" else "hours"} ago"
        daysAgo < 7 -> "$daysAgo ${if (daysAgo == 1L) "day" else "days"} ago"
        else -> displayDateFormat().format(createdAtMillis)
    }
}

private fun parseCreatedAtMillis(createdAt: String): Long? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            return java.time.Instant.parse(createdAt).toEpochMilli()
        } catch (_: Exception) {
        }
    }
    return isoInstantPatterns().firstNotNullOfOrNull { pattern ->
        try {
            pattern.parse(createdAt)?.time
        } catch (_: Exception) {
            null
        }
    }
}

private fun isoInstantPatterns(): List<SimpleDateFormat> {
    val utc = TimeZone.getTimeZone("UTC")
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
    )
    return patterns.map { pattern ->
        SimpleDateFormat(pattern, Locale.US).apply { timeZone = utc }
    }
}

private fun displayDateFormat(): SimpleDateFormat =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

fun handleNotificationNavigation(
    navController: NavController,
    notification: NotificationResponse
) {
    when (notification.type) {
        NotificationType.FRIEND_REQUEST_RECEIVED,
        NotificationType.FRIEND_REQUEST_ACCEPTED,
        NotificationType.FRIEND_REQUEST_DECLINED -> {
            notification.relatedUserId?.let { userId ->
                navController.navigate(Screen.Profile.createRoute(userId))
            } ?: run {
                navController.navigate(Screen.Friends.route)
            }
        }

        NotificationType.APPLICATION_APPROVED,
        NotificationType.APPLICATION_REJECTED -> {
            notification.applicationId?.let {
                navController.navigate(BottomBarScreen.MyApplications.route)
            } ?: notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        NotificationType.REVIEW_DEADLINE_REMINDER -> {
            notification.applicationId?.let { applicationId ->
                navController.navigate(Screen.ReviewSubmission.createRoute(applicationId))
            } ?: notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        NotificationType.AUTHOR_BOOK_PUBLISHED -> {
            notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        else -> {
        }
    }
}
