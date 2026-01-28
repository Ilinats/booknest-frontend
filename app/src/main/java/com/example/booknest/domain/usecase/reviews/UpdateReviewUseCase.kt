package com.example.booknest.domain.usecase.reviews

import com.example.booknest.domain.model.request.UpdateReviewRequest
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.repository.ReviewsRepository

class UpdateReviewUseCase(
    private val reviewsRepository: ReviewsRepository
) {
    suspend operator fun invoke(reviewId: String, request: UpdateReviewRequest): Result<ReviewResponse> {
        return reviewsRepository.updateReview(reviewId, request)
    }
}


