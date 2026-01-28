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

fun getDeadlineStatus(deadline: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(deadline) ?: SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).parse(deadline)
        date?.let {
            val now = Date()
            val daysUntil = ((it.time - now.time) / (1000 * 60 * 60 * 24)).toInt()
            when {
                daysUntil < 0 -> "Overdue (${-daysUntil} days ago)"
                daysUntil == 0 -> "Today"
                daysUntil == 1 -> "Tomorrow"
                else -> "$daysUntil days remaining"
            }
        } ?: deadline
    } catch (e: Exception) {
        deadline
    }
}
