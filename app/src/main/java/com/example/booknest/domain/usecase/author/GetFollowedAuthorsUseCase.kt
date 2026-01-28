package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.repository.AuthorFollowRepository

class GetFollowedAuthorsUseCase(
    private val authorFollowRepository: AuthorFollowRepository
) {
    suspend operator fun invoke(): Result<List<AuthorFollowResponse>> {
        return authorFollowRepository.getFollowedAuthors()
    }
}


