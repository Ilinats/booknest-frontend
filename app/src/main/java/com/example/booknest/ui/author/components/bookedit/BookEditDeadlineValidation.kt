package com.example.booknest.ui.author.components.bookedit

import java.text.SimpleDateFormat
import java.util.Locale

internal fun validateDeadlines(
    applicationDeadline: String?,
    reviewDeadline: String?,
): Pair<String?, String?> {
    val appError = if (applicationDeadline.isNullOrBlank()) {
        "Application deadline is required"
    } else null

    val revError = if (!reviewDeadline.isNullOrBlank()) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val appDate = dateFormat.parse(applicationDeadline!!)
            val revDate = dateFormat.parse(reviewDeadline)

            if (appDate != null && revDate != null && revDate.before(appDate)) {
                "Review deadline must be after application deadline"
            } else null
        } catch (_: Exception) {
            "Invalid date format"
        }
    } else null

    return Pair(appError, revError)
}
