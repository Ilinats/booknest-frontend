package com.example.booknest.domain.usecase.files

import com.example.booknest.domain.model.response.UploadBookFileResponse
import com.example.booknest.domain.repository.BooksRepository
import okhttp3.MultipartBody

class UploadBookFileUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(
        bookId: String,
        filePart: MultipartBody.Part
    ): Result<UploadBookFileResponse> {
        return booksRepository.uploadBookFile(bookId, filePart)
    }
}
