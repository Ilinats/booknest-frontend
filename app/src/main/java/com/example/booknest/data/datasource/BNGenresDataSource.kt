package com.example.booknest.data.datasource

import com.example.booknest.data.service.GenresService
import com.example.booknest.domain.model.request.CreateGenreRequest
import com.example.booknest.domain.model.request.DeleteGenrePreferenceRequest
import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenrePreferenceResponse
import com.example.booknest.domain.model.response.GenreResponse

class BNGenresDataSource(private val genresService: GenresService) : GenresDataSource {

    override suspend fun getGenres(): Result<List<GenreResponse>> {
        return requestBody(genresService.getGenres())
    }

    override suspend fun addGenre(genre: CreateGenreRequest): Result<GenreResponse> {
        return requestBody(genresService.addGenre(genre))
    }

    override suspend fun getGenrePreferences(): Result<List<GenrePreferenceResponse>> {
        return requestBody(genresService.getGenrePreferences())
    }

    override suspend fun saveUserGenre(preference: UpsertPreferenceRequest): Result<GenrePreferenceResponse> {
        return requestBody(genresService.saveUserGenre(preference))
    }

    override suspend fun deleteGenrePreference(request: DeleteGenrePreferenceRequest): Result<Unit> {
        return requestBodyUnit(genresService.deleteGenrePreference(request))
    }
}

