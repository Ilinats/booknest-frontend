package com.example.booknest.ui.author.components.bookedit

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import com.example.booknest.utils.BookDateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val MIN_REVIEW_DAYS_AFTER_APPLICATION = 1

const val REVIEW_DEADLINE_MIN_OFFSET_MESSAGE =
    "Review deadline must be at least one day after the application deadline"

fun validateDeadlines(
    applicationDeadline: String?,
    reviewDeadline: String?,
): Pair<String?, String?> {
    val appError = if (applicationDeadline.isNullOrBlank()) {
        "Application deadline is required"
    } else null

    val revError = if (!reviewDeadline.isNullOrBlank()) {
        validateReviewDeadlineOffset(applicationDeadline, reviewDeadline)
    } else null

    return Pair(appError, revError)
}

private fun validateReviewDeadlineOffset(
    applicationDeadline: String?,
    reviewDeadline: String
): String? {
    if (applicationDeadline.isNullOrBlank()) {
        return "Application deadline is required before setting a review deadline"
    }

    val appDateOnly = BookDateUtils.apiDeadlineToDateOnly(applicationDeadline) ?: applicationDeadline
    val revDateOnly = BookDateUtils.apiDeadlineToDateOnly(reviewDeadline) ?: reviewDeadline

    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val appCal = parseDateOnly(dateFormat, appDateOnly) ?: return "Invalid date format"
        val revCal = parseDateOnly(dateFormat, revDateOnly) ?: return "Invalid date format"
        val minReviewCal = (appCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, MIN_REVIEW_DAYS_AFTER_APPLICATION)
        }

        if (revCal.before(minReviewCal)) {
            REVIEW_DEADLINE_MIN_OFFSET_MESSAGE
        } else {
            null
        }
    } catch (_: Exception) {
        "Invalid date format"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun applicationDeadlineSelectableDates(): SelectableDates {
    return object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val dateOnly = BookDateUtils.pickerMillisToDateOnly(utcTimeMillis)
            return BookDateUtils.isDateAtLeastTomorrow(dateOnly)
        }

        override fun isSelectableYear(year: Int): Boolean = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun reviewDeadlineSelectableDates(applicationDeadline: String?): SelectableDates {
    val minReviewMillis = minReviewDeadlineMillis(applicationDeadline)
    return object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            if (minReviewMillis == null) return true
            val selected = Calendar.getInstance().apply {
                timeInMillis = utcTimeMillis
                clearTimeFields()
            }
            val minimum = Calendar.getInstance().apply {
                timeInMillis = minReviewMillis
                clearTimeFields()
            }
            return !selected.before(minimum)
        }

        override fun isSelectableYear(year: Int): Boolean = true
    }
}

internal fun minReviewDeadlineMillis(applicationDeadline: String?): Long? {
    if (applicationDeadline.isNullOrBlank()) return null
    val appDateOnly = BookDateUtils.apiDeadlineToDateOnly(applicationDeadline) ?: applicationDeadline
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val appCal = parseDateOnly(dateFormat, appDateOnly) ?: return null
        (appCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, MIN_REVIEW_DAYS_AFTER_APPLICATION)
        }.timeInMillis
    } catch (_: Exception) {
        null
    }
}

private fun parseDateOnly(dateFormat: SimpleDateFormat, value: String): Calendar? {
    val parsed = dateFormat.parse(value) ?: return null
    return Calendar.getInstance().apply {
        time = parsed
        clearTimeFields()
    }
}

private fun Calendar.clearTimeFields() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
