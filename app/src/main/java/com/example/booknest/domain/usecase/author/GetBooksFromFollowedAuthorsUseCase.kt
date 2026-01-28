package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.AuthorFollowRepository

class GetBooksFromFollowedAuthorsUseCase(
    private val authorFollowRepository: AuthorFollowRepository
) {
    suspend operator fun invoke(limit: Int? = 20): Result<List<RecommendedBookResponse>> {
        return authorFollowRepository.getBooksFromFollowedAuthors(limit)
    }
}


