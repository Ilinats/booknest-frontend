package com.example.booknest.ui.applications.utils

import com.example.booknest.utils.BookDateUtils

fun formatDate(dateString: String?): String =
    BookDateUtils.formatDeadlineForDisplay(dateString)

fun getApplicationDeadlineStatus(deadline: String): String? =
    BookDateUtils.getApplicationDeadlineStatus(deadline)

fun getReviewDeadlineStatus(deadline: String): String? =
    BookDateUtils.getReviewDeadlineStatus(deadline)
