package com.example.booknest.ui.applications.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun daysUntilDeadline(deadline: String): Int? {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(deadline) ?: SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault(),
        ).parse(deadline)
        date?.let {
            val now = Date()
            ((it.time - now.time) / (1000 * 60 * 60 * 24)).toInt()
        }
    } catch (_: Exception) {
        null
    }
}

/** Application deadline: no overdue label — hide status once the date has passed. */
fun getApplicationDeadlineStatus(deadline: String): String? {
    return when (val daysUntil = daysUntilDeadline(deadline)) {
        null -> null
        in Int.MIN_VALUE..-1 -> null
        0 -> "Today"
        1 -> "Tomorrow"
        else -> "$daysUntil days remaining"
    }
}

/** Review deadline: may show overdue when past due. */
fun getReviewDeadlineStatus(deadline: String): String? {
    return when (val daysUntil = daysUntilDeadline(deadline)) {
        null -> null
        in Int.MIN_VALUE..-1 -> "Overdue (${-daysUntil} days ago)"
        0 -> "Today"
        1 -> "Tomorrow"
        else -> "$daysUntil days remaining"
    }
}
