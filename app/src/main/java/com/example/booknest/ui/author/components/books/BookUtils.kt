package com.example.booknest.ui.author.components.books

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun calculateDaysLeft(deadline: String): Long {
    return try {
        val deadlineDate = Instant.parse(deadline)
        val now = Instant.now()
        ChronoUnit.DAYS.between(now, deadlineDate)
    } catch (e: Exception) {
        -1L
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

fun parseDate(dateString: String?): java.util.Date? {
    if (dateString == null) return null
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.parse(dateString)
    } catch (e: Exception) {
        null
    }
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
