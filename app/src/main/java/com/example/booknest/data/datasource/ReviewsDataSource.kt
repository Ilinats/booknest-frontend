package com.example.booknest.data.datasource

import com.example.booknest.domain.model.request.CreateReviewRequest
import com.example.booknest.domain.model.request.UpdateReviewRequest
import com.example.booknest.domain.model.response.ReviewResponse

interface ReviewsDataSource {
    suspend fun createReview(review: CreateReviewRequest): Result<ReviewResponse>
    suspend fun getReview(reviewId: String): Result<ReviewResponse>
    suspend fun updateReview(reviewId: String, review: UpdateReviewRequest): Result<ReviewResponse>
    suspend fun deleteReview(reviewId: String): Result<Unit>
    suspend fun getBookReviews(bookId: String): Result<List<ReviewResponse>>
    suspend fun getUserReviews(userId: String): Result<List<ReviewResponse>>
    suspend fun getAuthorLatestReviews(limit: Int?): Result<List<ReviewResponse>>
}

