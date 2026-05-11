package com.example.booknest.domain.usecase.analytics

import com.example.booknest.domain.model.response.BookPerformanceComparisonResponse
import com.example.booknest.domain.repository.BooksRepository

class GetBookPerformanceComparisonUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(): Result<List<BookPerformanceComparisonResponse>> {
        return booksRepository.getBookPerformanceComparison()
    }
}
