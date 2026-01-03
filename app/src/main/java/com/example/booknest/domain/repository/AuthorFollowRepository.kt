package com.example.booknest.domain.repository

import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.model.response.AuthorFollowWithStatsResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse

interface AuthorFollowRepository {
    suspend fun followAuthor(authorId: String): Result<AuthorFollowResponse>
    suspend fun unfollowAuthor(authorId: String): Result<Unit>
    suspend fun getFollowedAuthors(): Result<List<AuthorFollowResponse>>
    suspend fun getFollowedAuthorsWithStats(): Result<List<AuthorFollowWithStatsResponse>>
    suspend fun getAuthorFollowers(authorId: String): Result<List<AuthorFollowResponse>>
    suspend fun checkIfFollowingAuthor(authorId: String): Result<Map<String, Boolean>>
    suspend fun getBooksFromFollowedAuthors(limit: Int? = 20): Result<List<RecommendedBookResponse>>
}
