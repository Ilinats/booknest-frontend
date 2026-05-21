package com.example.booknest.data.datasource

import com.example.booknest.data.service.BooksService
import com.example.booknest.domain.model.response.PaginatedResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import retrofit2.Response

internal suspend fun BooksService.browseBooksFromQuery(
    query: BrowseBooksQueryMapper.ApiQuery,
): Response<PaginatedResponse<RecommendedBookResponse>> =
    browseBooks(
        page = query.page,
        limit = query.limit,
        search = query.search,
        sortBy = query.sortBy,
        filterBookGenresGenreId = query.filterBookGenresGenreId,
        filterAgeRating = query.filterAgeRating,
        filterDistributionType = query.filterDistributionType,
        filterAverageRating = query.filterAverageRating,
        filterAvailableCopies = query.filterAvailableCopies,
        filterApplicationDeadline = query.filterApplicationDeadline,
        filterAuthorId = query.filterAuthorId,
        filterSeriesId = query.filterSeriesId,
        filterPublishedAt = query.filterPublishedAt,
        filterStatus = query.filterStatus,
    )
