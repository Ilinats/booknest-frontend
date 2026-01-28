package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.repository.AuthorFollowRepository

class GetAuthorFollowersUseCase(
    private val authorFollowRepository: AuthorFollowRepository
) {
    suspend operator fun invoke(authorId: String): Result<List<AuthorFollowResponse>> {
        return authorFollowRepository.getAuthorFollowers(authorId)
    }
}


