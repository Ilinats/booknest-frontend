package com.example.booknest.domain.usecase.books

import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.BooksRepository

class GetRecommendedBooksUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(limit: Int? = 10, page: Int? = 1): Result<List<RecommendedBookResponse>> {
        return booksRepository.getRecommendedBooks(limit = limit, page = page)
    }
}
