package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.repository.BooksRepository

class DeleteBookUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Result<Unit> {
        return booksRepository.deleteBook(bookId)
    }
}


