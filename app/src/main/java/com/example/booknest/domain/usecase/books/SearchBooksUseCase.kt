package com.example.booknest.domain.usecase.books

import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.BooksRepository

class SearchBooksUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int? = null,
        limit: Int? = null
    ): Result<List<RecommendedBookResponse>> {
        return booksRepository.searchBooks(query, page, limit)
    }
}
