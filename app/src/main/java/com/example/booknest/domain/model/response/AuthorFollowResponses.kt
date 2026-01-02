package com.example.booknest.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthorFollowResponse(
    val id: String,
    val followerId: String,
    val authorId: String,
    val createdAt: String,
    val follower: UserResponse? = null,
    val author: UserResponse? = null
)

@Serializable
data class AuthorFollowWithStatsResponse(
    val author: UserResponse,
    val follow: AuthorFollowResponse,
    val stats: AuthorFollowStatsResponse
)

@Serializable
data class AuthorFollowStatsResponse(
    val totalBooks: Int,
    val publishedBooks: Int,
    val totalApplications: Int
)

