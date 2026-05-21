package com.example.booknest.data.service

import com.example.booknest.data.constants.PathConstants
import com.example.booknest.data.constants.QueryConstants
import com.example.booknest.data.constants.Reviews
import com.example.booknest.data.constants.Users
import com.example.booknest.domain.model.request.CreateReviewRequest
import com.example.booknest.domain.model.request.UpdateReviewRequest
import com.example.booknest.domain.model.response.PaginatedResponse
import com.example.booknest.domain.model.response.ReviewResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ReviewsService {
    @POST(Reviews.CREATE)
    suspend fun createReview(@Body review: CreateReviewRequest): Response<ReviewResponse>

    @GET(Reviews.BY_ID)
    suspend fun getReview(
        @Path(PathConstants.REVIEW_ID) reviewId: String
    ): Response<ReviewResponse>

    @PATCH(Reviews.BY_ID)
    suspend fun updateReview(
        @Path(PathConstants.REVIEW_ID) reviewId: String,
        @Body review: UpdateReviewRequest
    ): Response<ReviewResponse>

    @DELETE(Reviews.BY_ID)
    suspend fun deleteReview(
        @Path(PathConstants.REVIEW_ID) reviewId: String
    ): Response<Unit>

    @GET(Reviews.USER_REVIEWS)
    suspend fun getUserReviews(
        @Path(PathConstants.USER_ID) userId: String,
        @retrofit2.http.Query(QueryConstants.SKIP) skip: Int? = null,
        @retrofit2.http.Query(QueryConstants.TAKE) take: Int? = null,
    ): Response<PaginatedResponse<ReviewResponse>>

    @GET(Users.ME_REVIEWS_LATEST)
    suspend fun getAuthorLatestReviews(
        @retrofit2.http.Query(QueryConstants.LIMIT) limit: Int?,
    ): Response<List<ReviewResponse>>
}

