package com.example.booknest.ui.author.components.books

import com.example.booknest.utils.BookDateUtils

fun calculateDaysLeft(deadline: String): Long =
    BookDateUtils.daysUntilDeadlineCalendarCompat(deadline) ?: -1L

fun formatDate(dateString: String?): String =
    BookDateUtils.formatDeadlineForDisplay(dateString)

fun parseDate(dateString: String?): java.util.Date? {
    val instant = dateString?.let { BookDateUtils.parseDeadlineInstant(it) } ?: return null
    return java.util.Date.from(instant)
}

fun formatSelectionMethod(selectionMethod: String?): String {
    if (selectionMethod.isNullOrBlank()) return "Unknown"
    return when (selectionMethod.lowercase()) {
        "author_selects" -> "Author Selects"
        "first_come" -> "First Come First Served"
        "lottery" -> "Random Selection"
        else -> formatSnakeCaseLabel(selectionMethod)
    }
}

fun formatDistributionType(distributionType: String?): String {
    if (distributionType.isNullOrBlank()) return "Unknown"
    return when (distributionType.lowercase()) {
        "digital" -> "Digital"
        "physical" -> "Physical"
        "both" -> "Both"
        else -> formatSnakeCaseLabel(distributionType)
    }
}

fun formatBookStatus(status: String?): String {
    if (status.isNullOrBlank()) return "Unknown"
    return when (status.lowercase()) {
        "draft" -> "Draft"
        "active" -> "Active"
        "in_progress" -> "In Progress"
        "completed" -> "Completed"
        "archived" -> "Archived"
        else -> formatSnakeCaseLabel(status)
    }
}

private fun formatSnakeCaseLabel(value: String): String =
    value.replace("_", " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char -> char.uppercase() }
        }
