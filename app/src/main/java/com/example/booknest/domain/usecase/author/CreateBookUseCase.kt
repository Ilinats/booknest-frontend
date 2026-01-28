package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.repository.BooksRepository
import okhttp3.MultipartBody

class CreateBookUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(book: CreateBookRequest, filePart: MultipartBody.Part? = null): Result<BookResponse> {
        return booksRepository.createBook(book, filePart)
    }
}


