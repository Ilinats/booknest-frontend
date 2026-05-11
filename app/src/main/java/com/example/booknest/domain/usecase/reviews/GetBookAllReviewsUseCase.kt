package com.example.booknest.domain.usecase.reviews

import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.repository.BooksRepository

class GetBookAllReviewsUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(bookId: String): Result<List<ReviewResponse>> {
        return booksRepository.getBookAllReviews(bookId)
    }
}
