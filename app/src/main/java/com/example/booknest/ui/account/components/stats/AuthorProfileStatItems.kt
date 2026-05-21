package com.example.booknest.ui.account.components.stats

import com.example.booknest.domain.model.response.AuthorAnalyticsOverviewResponse
import com.example.booknest.domain.model.response.UserStatsDataResponse

internal fun authorProfileStatItems(stats: UserStatsDataResponse): List<Pair<String, Any>> {
    val approvalRate = if (stats.totalApplications > 0) {
        (stats.approvedApplications.toDouble() / stats.totalApplications * 100).toInt()
    } else {
        0
    }

    return listOf(
        "Total Books" to (stats.totalBooks ?: 0),
        "Active Books" to (stats.publishedBooks ?: 0),
        "Draft Books" to (stats.draftBooks ?: 0),
        "In Progress" to (stats.inProgressBooks ?: 0),
        "Completed" to (stats.completedBooks ?: 0),
        "Total Applications" to stats.totalApplications,
        "Approval Rate" to approvalRate,
        "Average Rating" to (stats.averageRating ?: 0.0),
        "Total Reviews" to (stats.totalReviews ?: 0)
    )
}

internal fun authorAnalyticsOverviewStatItems(
    overview: AuthorAnalyticsOverviewResponse
): List<Pair<String, Any>> = listOf(
    "Total Books" to overview.totalBooks,
    "Active Books" to overview.publishedBooks,
    "Draft Books" to overview.draftBooks,
    "In Progress" to overview.inProgressBooks,
    "Completed" to overview.completedBooks,
    "Total Applications" to overview.totalApplications,
    "Approval Rate" to overview.overallApprovalRate,
    "Average Rating" to overview.averageRating,
    "Total Reviews" to overview.totalReviews
)

internal fun authorProfileStatItemsFromMap(stats: Map<String, Any?>): List<Pair<String, Any>> {
    fun intValue(key: String): Int = (stats[key] as? Number)?.toInt() ?: 0
    fun doubleValue(key: String): Double = (stats[key] as? Number)?.toDouble() ?: 0.0

    val totalApplications = intValue("totalApplications")
    val approvedApplications = intValue("approvedApplications")
    val approvalRate = if (totalApplications > 0) {
        ((approvedApplications.toDouble() / totalApplications) * 100).toInt()
    } else {
        0
    }

    return listOf(
        "Total Books" to intValue("totalBooks"),
        "Active Books" to intValue("publishedBooks"),
        "Draft Books" to intValue("draftBooks"),
        "In Progress" to intValue("inProgressBooks"),
        "Completed" to intValue("completedBooks"),
        "Total Applications" to totalApplications,
        "Approval Rate" to approvalRate,
        "Average Rating" to doubleValue("averageRating"),
        "Total Reviews" to intValue("totalReviews")
    )
}

internal fun formatAuthorStatValue(title: String, value: Any): String = when {
    title == "Average Rating" -> String.format("%.1f", (value as? Number)?.toDouble() ?: 0.0)
    title == "Approval Rate" -> "${(value as? Number)?.toInt() ?: 0}%"
    else -> value.toString()
}
