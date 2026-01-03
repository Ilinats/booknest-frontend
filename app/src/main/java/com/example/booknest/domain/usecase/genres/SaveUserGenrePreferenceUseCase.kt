package com.example.booknest.domain.usecase.genres

import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenrePreferenceResponse
import com.example.booknest.domain.repository.GenresRepository

class SaveUserGenrePreferenceUseCase(
    private val repository: GenresRepository
) {
    suspend operator fun invoke(request: UpsertPreferenceRequest): Result<GenrePreferenceResponse> {
        return repository.saveUserGenre(request)
    }
}
