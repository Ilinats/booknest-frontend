package com.example.booknest.domain.usecase.books

import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.BooksRepository

class BrowseBooksUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(
        query: String? = null,
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
        createdFrom: String? = null,
        createdTo: String? = null,
        minAvgRating: Double? = null,
        maxAvgRating: Double? = null,
        page: Int? = null,
        limit: Int? = null,
        status: String? = null,
        applicationStatus: String? = null,
        deadlineFilter: String? = null,
        sortBy: String? = null
    ): Result<List<RecommendedBookResponse>> {
        return booksRepository.browseBooks(
            search = query,
            genres = genres,
            title = title,
            authorName = authorName,
            authorId = authorId,
            seriesName = seriesName,
            seriesId = seriesId,
            ageRating = ageRating,
            distributionType = distributionType,
            publishedFrom = publishedFrom,
            publishedTo = publishedTo,
            createdFrom = createdFrom,
            createdTo = createdTo,
            minAvgRating = minAvgRating,
            maxAvgRating = maxAvgRating,
            page = page,
            limit = limit,
            status = status,
            applicationStatus = applicationStatus,
            deadlineFilter = deadlineFilter,
            sortBy = sortBy
        )
    }
}
