package com.example.booknest.domain.usecase.genres

import com.example.booknest.domain.model.response.GenrePreferenceResponse
import com.example.booknest.domain.repository.GenresRepository

class GetGenrePreferencesUseCase(
    private val repository: GenresRepository
) {
    suspend operator fun invoke(): Result<List<GenrePreferenceResponse>> {
        return repository.getGenrePreferences()
    }
}
