package com.example.booknest.domain.usecase.files

import com.example.booknest.domain.model.response.DownloadBookResponse
import com.example.booknest.domain.repository.BooksRepository

class GetBookDownloadUrlUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Result<DownloadBookResponse> {
        return booksRepository.getBookDownloadUrl(bookId)
    }
}
