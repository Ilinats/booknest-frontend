package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.repository.BooksRepository

class UpdateBookUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String, book: UpdateBookRequest): Result<BookResponse> {
        return booksRepository.updateBook(bookId, book)
    }
}


