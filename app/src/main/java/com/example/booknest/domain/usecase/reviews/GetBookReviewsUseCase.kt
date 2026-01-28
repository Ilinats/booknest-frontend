package com.example.booknest.domain.usecase.reviews

import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.repository.ReviewsRepository

class GetBookReviewsUseCase(
    private val reviewsRepository: ReviewsRepository
) {
    suspend operator fun invoke(bookId: String): Result<List<ReviewResponse>> {
        return reviewsRepository.getBookReviews(bookId)
    }
}


