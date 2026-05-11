package com.example.booknest.data.datasource

import com.example.booknest.data.service.ReviewsService
import com.example.booknest.domain.model.request.CreateReviewRequest
import com.example.booknest.domain.model.request.UpdateReviewRequest
import com.example.booknest.domain.model.response.ReviewResponse

class BNReviewsDataSource(private val reviewsService: ReviewsService) : ReviewsDataSource {

    override suspend fun createReview(review: CreateReviewRequest): Result<ReviewResponse> {
        return runSuspendRequest { reviewsService.createReview(review) }
    }

    override suspend fun getReview(reviewId: String): Result<ReviewResponse> {
        return runSuspendRequest { reviewsService.getReview(reviewId) }
    }

    override suspend fun updateReview(
        reviewId: String,
        review: UpdateReviewRequest
    ): Result<ReviewResponse> {
        return runSuspendRequest { reviewsService.updateReview(reviewId, review) }
    }

    override suspend fun deleteReview(reviewId: String): Result<Unit> {
        return runSuspendRequestUnit { reviewsService.deleteReview(reviewId) }
    }

    override suspend fun getBookReviews(bookId: String): Result<List<ReviewResponse>> {
        return runSuspendRequestPaginated { reviewsService.getBookReviews(bookId) }
    }

    override suspend fun getUserReviews(userId: String): Result<List<ReviewResponse>> {
        return runSuspendRequestPaginated { reviewsService.getUserReviews(userId) }
    }

    override suspend fun getAuthorLatestReviews(limit: Int?): Result<List<ReviewResponse>> {
        return runSuspendRequest { reviewsService.getAuthorLatestReviews(limit) }
    }
}
