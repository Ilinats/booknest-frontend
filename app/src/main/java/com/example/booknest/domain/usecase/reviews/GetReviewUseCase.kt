package com.example.booknest.domain.usecase.reviews

import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.repository.ReviewsRepository

class GetReviewUseCase(
    private val reviewsRepository: ReviewsRepository
) {
    suspend operator fun invoke(reviewId: String): Result<ReviewResponse> {
        return reviewsRepository.getReview(reviewId)
    }
}


