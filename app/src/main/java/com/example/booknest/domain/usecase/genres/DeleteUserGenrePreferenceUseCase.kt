package com.example.booknest.domain.usecase.genres

import com.example.booknest.domain.model.request.DeleteGenrePreferenceRequest
import com.example.booknest.domain.repository.GenresRepository

class DeleteUserGenrePreferenceUseCase(
    private val repository: GenresRepository
) {
    suspend operator fun invoke(request: DeleteGenrePreferenceRequest): Result<Unit> {
        return repository.deleteGenrePreference(request)
    }
}
