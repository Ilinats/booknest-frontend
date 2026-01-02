package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.repository.BooksRepository

class GetMyBooksUseCase(
    private val repository: BooksRepository
) {
    suspend operator fun invoke(): Result<List<BookResponse>> =
        repository.getMyBooks()
}
