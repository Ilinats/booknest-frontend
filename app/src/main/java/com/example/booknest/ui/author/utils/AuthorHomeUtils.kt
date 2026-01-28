package com.example.booknest.ui.author.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

object AuthorHomeUtils {
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateDaysUntilDeadline(deadline: String): Long {
        return try {
            val deadlineDate = Instant.parse(deadline)
            val now = Instant.now()
            ChronoUnit.DAYS.between(now, deadlineDate)
        } catch (e: Exception) {
            0L
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isDeadlineApproaching(deadline: String): Boolean {
        return try {
            val deadlineDate = Instant.parse(deadline)
            val now = Instant.now()
            val daysUntil = ChronoUnit.DAYS.between(now, deadlineDate)
            daysUntil in 0..7
        } catch (e: Exception) {
            false
        }
    }

    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
