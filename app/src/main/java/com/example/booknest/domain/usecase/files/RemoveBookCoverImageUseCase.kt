package com.example.booknest.domain.usecase.files

import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.repository.BooksRepository

class RemoveBookCoverImageUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Result<BookResponse> {
        return booksRepository.removeBookCoverImage(bookId)
    }
}


