package com.example.booknest.domain.usecase.genres

import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.repository.GenresRepository

class GetGenresUseCase(
    private val repository: GenresRepository
) {
    suspend operator fun invoke(): Result<List<GenreResponse>> {
        return repository.getGenres()
    }
}
