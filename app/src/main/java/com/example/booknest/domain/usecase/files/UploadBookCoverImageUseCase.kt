package com.example.booknest.domain.usecase.files

import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.repository.BooksRepository
import okhttp3.MultipartBody

class UploadBookCoverImageUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String, file: MultipartBody.Part): Result<BookResponse> {
        return booksRepository.uploadBookCoverImage(bookId, file)
    }
}


