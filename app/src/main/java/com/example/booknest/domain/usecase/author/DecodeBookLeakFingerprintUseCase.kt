package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.response.BookLeakFingerprintResponse
import com.example.booknest.domain.repository.BooksRepository
import okhttp3.MultipartBody

class DecodeBookLeakFingerprintUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(
        bookId: String,
        file: MultipartBody.Part
    ): Result<BookLeakFingerprintResponse> {
        return booksRepository.decodeLeakFingerprint(bookId, file)
    }
}
