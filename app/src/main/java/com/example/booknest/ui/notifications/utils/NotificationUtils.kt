package com.example.booknest.ui.notifications.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.navigation.Screen
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
fun formatNotificationTime(createdAt: String): String {
    return try {
        val instant = Instant.parse(createdAt)
        val now = Instant.now()
        val minutesAgo = ChronoUnit.MINUTES.between(instant, now)
        val hoursAgo = ChronoUnit.HOURS.between(instant, now)
        val daysAgo = ChronoUnit.DAYS.between(instant, now)

        when {
            minutesAgo < 1 -> "Just now"
            minutesAgo < 60 -> "$minutesAgo ${if (minutesAgo == 1L) "minute" else "minutes"} ago"
            hoursAgo < 24 -> "$hoursAgo ${if (hoursAgo == 1L) "hour" else "hours"} ago"
            daysAgo < 7 -> "$daysAgo ${if (daysAgo == 1L) "day" else "days"} ago"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                    .format(formatter)
            }
        }
    } catch (e: Exception) {
        createdAt
    }
}

fun handleNotificationNavigation(
    navController: NavController,
    notification: NotificationResponse
) {
    when (notification.type) {
        "friend_request_received", "friend_request_accepted" -> {
            notification.relatedUserId?.let { userId ->
                navController.navigate(Screen.Profile.createRoute(userId))
            } ?: run {
                navController.navigate(Screen.Friends.route)
            }
        }

        "application_approved", "application_rejected" -> {
            notification.applicationId?.let { applicationId ->
                navController.navigate("my_applications")
            } ?: notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        "review_deadline_reminder" -> {
            notification.applicationId?.let { applicationId ->
                navController.navigate("review_submission/$applicationId")
            } ?: notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        "author_book_published" -> {
            notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        "book_copy_sent" -> {
            notification.applicationId?.let { applicationId ->
                navController.navigate("my_applications")
            } ?: notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        "new_review_on_book", "application_received" -> {
            notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }
    }
}

