package com.example.booknest.ui.author.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Locale

object AuthorHomeUtils {
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateDaysUntilDeadline(deadline: String): Long? =
        DeadlineDisplayUtils.daysUntilDeadline(deadline)

    @RequiresApi(Build.VERSION_CODES.O)
    fun isDeadlineApproaching(deadline: String): Boolean {
        val daysUntil = DeadlineDisplayUtils.daysUntilDeadline(deadline) ?: return false
        return daysUntil in 0..7
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
