package com.example.booknest.ui.author.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.TimeZone

object DeadlineDisplayUtils {

    fun parseDeadlineInstant(deadline: String): Instant? {
        if (deadline.isBlank()) return null
        return try {
            Instant.parse(deadline)
        } catch (_: Exception) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(deadline)?.toInstant()
            } catch (_: Exception) {
                null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun daysUntilDeadline(deadline: String): Long? {
        val deadlineInstant = parseDeadlineInstant(deadline) ?: return null
        return ChronoUnit.DAYS.between(Instant.now(), deadlineInstant)
    }

    fun hasDisplayableDeadline(deadline: String?): Boolean {
        val value = deadline?.takeIf { it.isNotBlank() } ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return daysUntilDeadline(value) != null
        }
        return parseDeadlineInstant(value) != null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatEndsInText(deadline: String): String? {
        val days = daysUntilDeadline(deadline) ?: return null
        return when {
            days < 0 -> null
            days == 0L -> "Ends today"
            days == 1L -> "Ends in 1 day"
            else -> "Ends in $days days"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDaysLeftText(deadline: String): String? {
        val days = daysUntilDeadline(deadline) ?: return null
        return when {
            days < 0 -> null
            days == 0L -> "Due today"
            days == 1L -> "1 day left"
            else -> "$days days left"
        }
    }
}
