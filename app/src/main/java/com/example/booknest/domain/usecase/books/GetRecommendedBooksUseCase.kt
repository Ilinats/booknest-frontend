package com.example.booknest.domain.usecase.books

import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.BooksRepository

class GetRecommendedBooksUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(take: Int? = 10): Result<List<RecommendedBookResponse>> {
        return booksRepository.getRecommendedBooks(take)
    }
}
