package com.example.booknest.domain.usecase.analytics

import com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse
import com.example.booknest.domain.repository.BooksRepository

class GetDetailedBookAnalyticsUseCase(
    private val repository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Result<DetailedBookAnalyticsResponse> =
        repository.getDetailedBookAnalytics(bookId)
}
