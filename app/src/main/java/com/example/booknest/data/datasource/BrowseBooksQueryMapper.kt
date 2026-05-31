package com.example.booknest.data.datasource

import java.time.Instant
import java.time.temporal.ChronoUnit

internal object BrowseBooksQueryMapper {

    data class ApiQuery(
        val page: Int? = null,
        val limit: Int? = null,
        val search: String? = null,
        val sortBy: String? = null,
        val filterBookGenresGenreId: String? = null,
        val filterAgeRating: String? = null,
        val filterDistributionType: String? = null,
        val filterAverageRating: String? = null,
        val filterAvailableCopies: String? = null,
        val filterApplicationDeadline: String? = null,
        val filterAuthorId: String? = null,
        val filterSeriesId: String? = null,
        val filterPublishedAt: String? = null,
        val filterStatus: String? = null,
    )

    fun toApiQuery(
        search: String? = null,
        genres: List<Int>? = null,
        title: String? = null,
        authorName: String? = null,
        authorId: String? = null,
        seriesName: String? = null,
        seriesId: String? = null,
        ageRating: String? = null,
        distributionType: String? = null,
        publishedFrom: String? = null,
        publishedTo: String? = null,
        minAvgRating: Double? = null,
        maxAvgRating: Double? = null,
        page: Int? = null,
        limit: Int? = null,
        status: String? = null,
        applicationStatus: String? = null,
        deadlineFilter: String? = null,
        sortBy: String? = null,
    ): ApiQuery {
        val resolvedSearch = search?.trim()?.takeIf { it.isNotEmpty() }
            ?: listOfNotNull(
                title?.trim()?.takeIf { it.isNotEmpty() },
                authorName?.trim()?.takeIf { it.isNotEmpty() },
                seriesName?.trim()?.takeIf { it.isNotEmpty() },
            ).joinToString(" ").takeIf { it.isNotEmpty() }

        val genreFilter = genres?.takeIf { it.isNotEmpty() }?.let { ids ->
            if (ids.size == 1) "\$eq:${ids.first()}" else "\$in:${ids.joinToString(",")}"
        }

        val ratingFilter = when {
            minAvgRating != null && maxAvgRating != null &&
                (minAvgRating > 0 || maxAvgRating < 5) ->
                "\$btw:$minAvgRating,$maxAvgRating"
            minAvgRating != null && minAvgRating > 0 -> "\$gte:$minAvgRating"
            maxAvgRating != null && maxAvgRating < 5 -> "\$lte:$maxAvgRating"
            else -> null
        }

        val publishedFilter = when {
            !publishedFrom.isNullOrBlank() && !publishedTo.isNullOrBlank() ->
                "\$btw:${normalizeFilterDate(publishedFrom)},${normalizeFilterDate(publishedTo)}"
            !publishedFrom.isNullOrBlank() -> "\$gte:${normalizeFilterDate(publishedFrom)}"
            !publishedTo.isNullOrBlank() -> "\$lte:${normalizeFilterDate(publishedTo)}"
            else -> null
        }

        var availableCopiesFilter: String? = null
        var deadlineFilterExpr: String? = mapDeadlineFilter(deadlineFilter)

        if (applicationStatus == "accepting_applications") {
            availableCopiesFilter = "\$gt:0"
            val nowIso = Instant.now().toString()
            deadlineFilterExpr = "\$gt:$nowIso"
        }

        return ApiQuery(
            page = page,
            limit = limit,
            search = resolvedSearch,
            sortBy = mapSortBy(sortBy),
            filterBookGenresGenreId = genreFilter,
            filterAgeRating = ageRating?.let { "\$eq:$it" },
            filterDistributionType = distributionType?.let { "\$eq:$it" },
            filterAverageRating = ratingFilter,
            filterAvailableCopies = availableCopiesFilter,
            filterApplicationDeadline = deadlineFilterExpr,
            filterAuthorId = authorId?.let { "\$eq:$it" },
            filterSeriesId = seriesId?.let { "\$eq:$it" },
            filterPublishedAt = publishedFilter,
            filterStatus = null,
        )
    }

    private fun mapSortBy(frontendKey: String?): String? = when (frontendKey) {
        "newest" -> "publishedAt:DESC"
        "most_popular" -> "mostPopular:DESC"
        "highest_rated" -> "averageRating:DESC"
        "deadline_soonest" -> "applicationDeadline:ASC"
        "most_available" -> "availableCopies:DESC"
        else -> null
    }

    private fun normalizeFilterDate(value: String): String {
        val trimmed = value.trim()
        if (trimmed.endsWith('Z') || trimmed.contains('+') || trimmed.contains("T") && trimmed.length > 19) {
            return trimmed
        }
        return "${trimmed}Z"
    }

    private fun mapDeadlineFilter(deadlineFilter: String?): String? {
        return when (deadlineFilter) {
            "ending_soon" -> {
                val now = Instant.now()
                val weekLater = now.plus(7, ChronoUnit.DAYS)
                "\$btw:${now},${weekLater}"
            }
            "still_time" -> {
                val afterWeek = Instant.now().plus(7, ChronoUnit.DAYS)
                "\$gt:$afterWeek"
            }
            else -> null
        }
    }
}
