package com.example.booknest.domain.usecase.reviews

import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.repository.ReviewsRepository

class GetAuthorLatestReviewsUseCase(
    private val reviewsRepository: ReviewsRepository
) {
    suspend operator fun invoke(limit: Int? = 3): Result<List<ReviewResponse>> {
        return reviewsRepository.getAuthorLatestReviews(limit)
    }
}


