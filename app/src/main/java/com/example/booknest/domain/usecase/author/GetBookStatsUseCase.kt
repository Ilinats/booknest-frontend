package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.repository.BooksRepository

class GetBookStatsUseCase(
    private val repository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Result<BookStatsResponse> =
        repository.getBookStats(bookId)
}
