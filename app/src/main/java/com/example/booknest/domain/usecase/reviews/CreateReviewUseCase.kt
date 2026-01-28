package com.example.booknest.domain.usecase.reviews

import com.example.booknest.domain.model.request.CreateReviewRequest
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.repository.ReviewsRepository

class CreateReviewUseCase(
    private val reviewsRepository: ReviewsRepository
) {
    suspend operator fun invoke(request: CreateReviewRequest): Result<ReviewResponse> {
        return reviewsRepository.createReview(request)
    }
}


