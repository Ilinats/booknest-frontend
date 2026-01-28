package com.example.booknest.data.datasource

import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.model.response.AuthorFollowWithStatsResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse

interface AuthorsDataSource {
    suspend fun followAuthor(authorId: String): Result<AuthorFollowResponse>
    suspend fun unfollowAuthor(authorId: String): Result<Unit>
    suspend fun getFollowedAuthors(): Result<List<AuthorFollowResponse>>
    suspend fun getAuthorFollowers(authorId: String): Result<List<AuthorFollowResponse>>
    suspend fun checkIfFollowingAuthor(authorId: String): Result<Map<String, Boolean>>
    suspend fun getBooksFromFollowedAuthors(limit: Int?): Result<List<RecommendedBookResponse>>
}

