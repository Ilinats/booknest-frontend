package com.example.booknest.domain.usecase.files

import com.example.booknest.domain.model.BookDownloadPayload
import com.example.booknest.domain.repository.BooksRepository

class GetBookDownloadUrlUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Result<BookDownloadPayload> {
        return booksRepository.getBookDownload(bookId)
    }
}
