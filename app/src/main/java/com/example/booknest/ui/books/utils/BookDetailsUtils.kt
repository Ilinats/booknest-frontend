package com.example.booknest.ui.books.utils

import com.example.booknest.domain.model.response.BookResponse
import java.text.SimpleDateFormat
import java.util.*

/** No review copies left (same rule as browse list "Fully Booked" badge). */
fun BookResponse.isFullyBooked(): Boolean {
    val total = totalCopies ?: return false
    if (total <= 0) return false
    val available = availableCopies ?: return false
    return available <= 0
}

internal fun formatDateDMY(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

