package com.example.booknest.domain.usecase.books

import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.repository.BooksRepository

class GetBookDetailsUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Result<BookResponse> {
        return booksRepository.getBookDetails(bookId)
    }
}
