package com.example.booknest.data.datasource

import com.example.booknest.data.service.AuthorsService
import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse

class BNAuthorsDataSource(private val authorsService: AuthorsService) : AuthorsDataSource {

    override suspend fun followAuthor(authorId: String): Result<AuthorFollowResponse> {
        return runSuspendRequest { authorsService.followAuthor(authorId) }
    }

    override suspend fun unfollowAuthor(authorId: String): Result<Unit> {
        return runSuspendRequestUnit { authorsService.unfollowAuthor(authorId) }
    }

    override suspend fun getFollowedAuthors(): Result<List<AuthorFollowResponse>> {
        return runSuspendRequest { authorsService.getFollowedAuthors() }
    }

    override suspend fun getAuthorFollowers(authorId: String): Result<List<AuthorFollowResponse>> {
        return runSuspendRequest { authorsService.getAuthorFollowers(authorId) }
    }

    override suspend fun checkIfFollowingAuthor(authorId: String): Result<Map<String, Boolean>> {
        return runSuspendRequest { authorsService.checkIfFollowingAuthor(authorId) }
    }

    override suspend fun getBooksFromFollowedAuthors(limit: Int?): Result<List<RecommendedBookResponse>> {
        return runSuspendRequest { authorsService.getBooksFromFollowedAuthors(limit) }
    }
}
