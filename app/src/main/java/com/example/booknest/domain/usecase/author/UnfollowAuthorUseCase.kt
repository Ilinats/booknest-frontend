package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.repository.AuthorFollowRepository

class UnfollowAuthorUseCase(
    private val authorFollowRepository: AuthorFollowRepository
) {
    suspend operator fun invoke(authorId: String): Result<Unit> {
        return authorFollowRepository.unfollowAuthor(authorId)
    }
}


