package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.AuthorStatsResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetAuthorStatsUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(authorId: String): Result<AuthorStatsResponse> =
        repository.getAuthorStats(authorId)
}
