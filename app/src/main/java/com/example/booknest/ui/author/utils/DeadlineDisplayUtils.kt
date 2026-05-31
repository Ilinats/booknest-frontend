package com.example.booknest.ui.author.utils

import com.example.booknest.utils.BookDateUtils
import java.time.Instant

object DeadlineDisplayUtils {

    fun parseDeadlineInstant(deadline: String): Instant? =
        BookDateUtils.parseDeadlineInstant(deadline)

    fun daysUntilDeadline(deadline: String): Long? =
        BookDateUtils.daysUntilDeadlineCalendarCompat(deadline)

    fun hasDisplayableDeadline(deadline: String?): Boolean {
        val value = deadline?.takeIf { it.isNotBlank() } ?: return false
        return parseDeadlineInstant(value) != null
    }

    fun formatEndsInText(deadline: String): String? =
        BookDateUtils.formatEndsInText(deadline)

    fun formatDaysLeftText(deadline: String): String? =
        BookDateUtils.formatDaysLeftText(deadline)
}
