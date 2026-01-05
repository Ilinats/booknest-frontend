package com.example.booknest.domain.usecase.books

import com.example.booknest.domain.model.response.TrendingBookResponse
import com.example.booknest.domain.repository.BooksRepository

class GetTrendingBooksUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(limit: Int? = null): Result<List<TrendingBookResponse>> {
        return booksRepository.getTrendingBooks(limit)
    }
}

