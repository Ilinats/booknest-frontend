package com.example.booknest.domain.usecase.analytics

import com.example.booknest.domain.model.response.AuthorAnalyticsResponse
import com.example.booknest.domain.repository.BooksRepository

class GetAuthorAnalyticsUseCase(
    private val repository: BooksRepository
) {
    suspend operator fun invoke(dateRange: String? = null): Result<AuthorAnalyticsResponse> =
        repository.getAuthorAnalytics(dateRange)
}
