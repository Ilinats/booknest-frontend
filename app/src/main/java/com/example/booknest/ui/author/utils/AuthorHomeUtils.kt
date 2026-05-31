package com.example.booknest.ui.author.utils

import com.example.booknest.utils.BookDateUtils

object AuthorHomeUtils {

    fun calculateDaysUntilDeadline(deadline: String): Long? =
        DeadlineDisplayUtils.daysUntilDeadline(deadline)

    fun isDeadlineApproaching(deadline: String): Boolean {
        val daysUntil = DeadlineDisplayUtils.daysUntilDeadline(deadline) ?: return false
        return daysUntil in 0..7
    }

    fun formatDate(dateString: String): String =
        BookDateUtils.formatDeadlineForDisplay(dateString)
}
