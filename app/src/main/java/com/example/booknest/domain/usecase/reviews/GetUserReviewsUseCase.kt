package com.example.booknest.domain.usecase.reviews

import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.repository.ReviewsRepository

class GetUserReviewsUseCase(
    private val reviewsRepository: ReviewsRepository
) {
    suspend operator fun invoke(userId: String): Result<List<ReviewResponse>> {
        return reviewsRepository.getUserReviews(userId)
    }
}


