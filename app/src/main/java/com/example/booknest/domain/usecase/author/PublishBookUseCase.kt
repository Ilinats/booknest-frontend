package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.repository.BooksRepository

class PublishBookUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Result<BookResponse> {
        return booksRepository.publishBook(bookId)
    }
}


