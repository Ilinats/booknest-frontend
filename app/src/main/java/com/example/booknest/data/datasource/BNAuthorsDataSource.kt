package com.example.booknest.data.datasource

import com.example.booknest.data.service.AuthorsService
import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.model.response.AuthorFollowWithStatsResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse

class BNAuthorsDataSource(private val authorsService: AuthorsService) : AuthorsDataSource {

    override suspend fun followAuthor(authorId: String): Result<AuthorFollowResponse> {
        return requestBody(authorsService.followAuthor(authorId))
    }

    override suspend fun unfollowAuthor(authorId: String): Result<Unit> {
        return requestBodyUnit(authorsService.unfollowAuthor(authorId))
    }

    override suspend fun getFollowedAuthors(): Result<List<AuthorFollowResponse>> {
        return requestBody(authorsService.getFollowedAuthors())
    }

    override suspend fun getFollowedAuthorsWithStats(): Result<List<AuthorFollowWithStatsResponse>> {
        return requestBody(authorsService.getFollowedAuthorsWithStats())
    }

    override suspend fun getAuthorFollowers(authorId: String): Result<List<AuthorFollowResponse>> {
        return requestBody(authorsService.getAuthorFollowers(authorId))
    }

    override suspend fun checkIfFollowingAuthor(authorId: String): Result<Map<String, Boolean>> {
        return requestBody(authorsService.checkIfFollowingAuthor(authorId))
    }

    override suspend fun getBooksFromFollowedAuthors(limit: Int?): Result<List<RecommendedBookResponse>> {
        return requestBody(authorsService.getBooksFromFollowedAuthors(limit))
    }
}

