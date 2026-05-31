package com.example.booknest.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Book deadline/date handling aligned with the backend (ISO instants, date-only picker values).
 *
 * - Authors pick a calendar day in the date picker; we send end-of-that-day UTC to the API.
 * - Relative labels (Today / Tomorrow) use calendar days in the device timezone vs the deadline's UTC date.
 * - Display formatting uses the deadline's UTC calendar date so it matches what the author selected.
 */
object BookDateUtils {

    private val UTC: ZoneId = ZoneOffset.UTC
    private val ISO_WITH_MS =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    private val DATE_ONLY = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
    }

    fun parseDeadlineInstant(deadline: String): Instant? {
        if (deadline.isBlank()) return null
        return try {
            Instant.parse(deadline)
        } catch (_: Exception) {
            try {
                ISO_WITH_MS.parse(deadline)?.toInstant()
            } catch (_: Exception) {
                try {
                    val date = DATE_ONLY.parse(deadline) ?: return null
                    date.toInstant()
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    /** yyyy-MM-dd from Material DatePicker UTC millis (avoids local-TZ shifting the day). */
    fun pickerMillisToDateOnly(utcTimeMillis: Long): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = utcTimeMillis
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    /** End of the selected calendar day in UTC — applications stay open through that day. */
    fun dateOnlyToApiDeadlineEndOfDay(dateOnly: String): String {
        if (dateOnly.contains('T')) return dateOnly
        val parts = dateOnly.split("-")
        if (parts.size != 3) return dateOnly
        return String.format(
            Locale.US,
            "%04d-%02d-%02dT23:59:59.999Z",
            parts[0].toInt(),
            parts[1].toInt(),
            parts[2].toInt(),
        )
    }

    fun apiDeadlineToDateOnly(deadline: String): String? {
        if (deadline.isBlank()) return null
        if (!deadline.contains('T')) return deadline
        val instant = parseDeadlineInstant(deadline) ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val date = instant.atZone(UTC).toLocalDate()
            String.format(
                Locale.US,
                "%04d-%02d-%02d",
                date.year,
                date.monthValue,
                date.dayOfMonth,
            )
        } else {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = instant.toEpochMilli()
            String.format(
                Locale.US,
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
            )
        }
    }

    fun normalizeDeadlineForApi(value: String): String =
        if (value.contains('T')) value else dateOnlyToApiDeadlineEndOfDay(value)

    fun formatDeadlineForDisplay(deadline: String?): String {
        if (deadline.isNullOrBlank()) return ""
        if (!deadline.contains('T')) {
            return formatDateOnlyForDisplay(deadline)
        }
        val instant = parseDeadlineInstant(deadline) ?: return deadline
        return formatUtcCalendarDate(instant.toEpochMilli())
    }

    fun formatDateOnlyForDisplay(dateOnly: String): String {
        return try {
            val parsed = DATE_ONLY.parse(dateOnly) ?: return dateOnly
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(parsed)
        } catch (_: Exception) {
            dateOnly
        }
    }

    fun isDateAtLeastTomorrow(dateOnly: String): Boolean {
        val deadlineDate = parseDateOnlyToLocalDate(dateOnly) ?: return false
        val tomorrow = LocalDate.now().plusDays(1)
        return !deadlineDate.isBefore(tomorrow)
    }

    /**
     * Calendar-day difference (device timezone today vs deadline UTC date).
     * 0 = today, 1 = tomorrow, negative = past.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun daysUntilDeadlineCalendar(deadline: String): Long? {
        val instant = parseDeadlineInstant(deadline) ?: return null
        val deadlineDate = instant.atZone(UTC).toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())
        return ChronoUnit.DAYS.between(today, deadlineDate)
    }

    fun daysUntilDeadlineCalendarCompat(deadline: String): Long? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return daysUntilDeadlineCalendar(deadline)
        }
        val deadlineDateOnly = apiDeadlineToDateOnly(deadline) ?: deadline.take(10)
        val deadlineCal = parseDateOnlyToCalendar(deadlineDateOnly) ?: return null
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = deadlineCal.timeInMillis - todayCal.timeInMillis
        return diffMs / (24 * 60 * 60 * 1000)
    }

    fun isApplicationDeadlinePassed(deadline: String?): Boolean {
        if (deadline.isNullOrBlank()) return true
        val instant = parseDeadlineInstant(deadline) ?: return true
        return Instant.now().isAfter(instant)
    }

    fun formatEndsInText(deadline: String): String? {
        val days = daysUntilDeadlineCalendarCompat(deadline) ?: return null
        return when {
            days < 0 -> null
            days == 0L -> "Ends today"
            days == 1L -> "Ends in 1 day"
            else -> "Ends in $days days"
        }
    }

    fun formatDaysLeftText(deadline: String): String? {
        val days = daysUntilDeadlineCalendarCompat(deadline) ?: return null
        return when {
            days < 0 -> null
            days == 0L -> "Due today"
            days == 1L -> "1 day left"
            else -> "$days days left"
        }
    }

    fun getApplicationDeadlineStatus(deadline: String): String? {
        val days = daysUntilDeadlineCalendarCompat(deadline) ?: return null
        return when {
            days < 0 -> null
            days == 0L -> "Today"
            days == 1L -> "Tomorrow"
            else -> "$days days remaining"
        }
    }

    fun getReviewDeadlineStatus(deadline: String): String? {
        val days = daysUntilDeadlineCalendarCompat(deadline) ?: return null
        return when {
            days < 0 -> "Overdue (${-days} days ago)"
            days == 0L -> "Today"
            days == 1L -> "Tomorrow"
            else -> "$days days remaining"
        }
    }

    private fun parseDateOnlyToLocalDate(dateOnly: String): LocalDate? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return try {
            val parts = dateOnly.split("-")
            if (parts.size != 3) return null
            LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } catch (_: Exception) {
            null
        }
    }

    private fun formatUtcCalendarDate(epochMillis: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val date = Instant.ofEpochMilli(epochMillis).atZone(UTC).toLocalDate()
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()).format(date)
        } else {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = epochMillis
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(calendar.time)
        }
    }

    private fun parseDateOnlyToCalendar(dateOnly: String): Calendar? {
        return try {
            val parsed = DATE_ONLY.parse(dateOnly) ?: return null
            Calendar.getInstance().apply {
                time = parsed
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (_: Exception) {
            null
        }
    }
}
