package com.example.booknest.data.repository

import com.example.booknest.data.datasource.ReviewsDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.request.CreateReviewRequest
import com.example.booknest.domain.model.request.UpdateReviewRequest
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.repository.ReviewsRepository

class BNReviewsRepository(private val reviewsDataSource: ReviewsDataSource) : ReviewsRepository {

    override suspend fun createReview(review: CreateReviewRequest): Result<ReviewResponse> {
        return resultBody(reviewsDataSource.createReview(review))
    }

    override suspend fun getReview(reviewId: String): Result<ReviewResponse> {
        return resultBody(reviewsDataSource.getReview(reviewId))
    }

    override suspend fun updateReview(
        reviewId: String,
        review: UpdateReviewRequest
    ): Result<ReviewResponse> {
        return resultBody(reviewsDataSource.updateReview(reviewId, review))
    }

    override suspend fun deleteReview(reviewId: String): Result<Unit> {
        return resultBody(reviewsDataSource.deleteReview(reviewId))
    }

    override suspend fun getBookReviews(bookId: String): Result<List<ReviewResponse>> {
        return resultBody(reviewsDataSource.getBookReviews(bookId))
    }

    override suspend fun getUserReviews(userId: String): Result<List<ReviewResponse>> {
        return resultBody(reviewsDataSource.getUserReviews(userId))
    }

    override suspend fun getFeaturedReviews(): Result<List<ReviewResponse>> {
        return resultBody(reviewsDataSource.getFeaturedReviews())
    }

    override suspend fun getAuthorLatestReviews(limit: Int?): Result<List<ReviewResponse>> {
        return resultBody(reviewsDataSource.getAuthorLatestReviews(limit))
    }
}

