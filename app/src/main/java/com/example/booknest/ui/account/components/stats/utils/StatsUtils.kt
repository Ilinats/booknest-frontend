package com.example.booknest.ui.account.components.stats.utils

internal fun formatNumber(value: Int): String {
    return when {
        value >= 1_000_000 -> "${value / 1_000_000.0}M"
        value >= 1_000 -> "${value / 1_000.0}K"
        else -> value.toString()
    }
}

internal fun formatReadingTime(hours: Double?): String {
    if (hours == null || hours == 0.0) return "N/A"
    val totalMinutes = (hours * 60).toInt()
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        m > 0 -> "${m}m"
        else -> "N/A"
    }
}

