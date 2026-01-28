package com.example.booknest.data.repository

import com.example.booknest.data.datasource.AuthorsDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.model.response.AuthorFollowWithStatsResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.AuthorFollowRepository

class BNAuthorFollowRepository(private val authorsDataSource: AuthorsDataSource) :
    AuthorFollowRepository {

    override suspend fun followAuthor(authorId: String): Result<AuthorFollowResponse> {
        return resultBody(authorsDataSource.followAuthor(authorId))
    }

    override suspend fun unfollowAuthor(authorId: String): Result<Unit> {
        return resultBody(authorsDataSource.unfollowAuthor(authorId))
    }

    override suspend fun getFollowedAuthors(): Result<List<AuthorFollowResponse>> {
        return resultBody(authorsDataSource.getFollowedAuthors())
    }

    override suspend fun getAuthorFollowers(authorId: String): Result<List<AuthorFollowResponse>> {
        return resultBody(authorsDataSource.getAuthorFollowers(authorId))
    }

    override suspend fun checkIfFollowingAuthor(authorId: String): Result<Map<String, Boolean>> {
        return resultBody(authorsDataSource.checkIfFollowingAuthor(authorId))
    }

    override suspend fun getBooksFromFollowedAuthors(limit: Int?): Result<List<RecommendedBookResponse>> {
        return resultBody(authorsDataSource.getBooksFromFollowedAuthors(limit))
    }
}

