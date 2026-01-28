package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.repository.AuthorFollowRepository

class CheckIfFollowingAuthorUseCase(
    private val authorFollowRepository: AuthorFollowRepository
) {
    suspend operator fun invoke(authorId: String): Result<Map<String, Boolean>> {
        return authorFollowRepository.checkIfFollowingAuthor(authorId)
    }
}


