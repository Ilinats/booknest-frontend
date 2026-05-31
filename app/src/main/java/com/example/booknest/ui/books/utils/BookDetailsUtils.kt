package com.example.booknest.ui.books.utils

import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.utils.BookDateUtils

/** No review copies left (same rule as browse list "Fully Booked" badge). */
fun BookResponse.isFullyBooked(): Boolean {
    val total = totalCopies ?: return false
    if (total <= 0) return false
    val available = availableCopies ?: return false
    return available <= 0
}

internal fun formatDateDMY(dateString: String): String =
    BookDateUtils.formatDeadlineForDisplay(dateString)

fun formatDate(dateString: String): String =
    BookDateUtils.formatDeadlineForDisplay(dateString)

fun isApplicationDeadlinePassed(deadline: String?): Boolean =
    BookDateUtils.isApplicationDeadlinePassed(deadline)

