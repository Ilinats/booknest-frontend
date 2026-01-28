package com.example.booknest.ui.applications.models

data class ApplicationStats(
    val total: Int,
    val pending: Int,
    val approved: Int,
    val rejected: Int,
    val withdrawn: Int
)

enum class SortOption(val displayName: String) {
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
    RATING_DESC("Rating (High)"),
    READING_STATUS("Reading Status")
}
