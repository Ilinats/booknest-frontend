package com.example.booknest.data.repository

import com.example.booknest.data.datasource.GenresDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.request.CreateGenreRequest
import com.example.booknest.domain.model.request.DeleteGenrePreferenceRequest
import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenrePreferenceResponse
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.repository.GenresRepository

class BNGenresRepository(private val genresDataSource: GenresDataSource) : GenresRepository {

    override suspend fun getGenres(): Result<List<GenreResponse>> {
        return resultBody(genresDataSource.getGenres())
    }

    override suspend fun addGenre(genre: CreateGenreRequest): Result<GenreResponse> {
        return resultBody(genresDataSource.addGenre(genre))
    }

    override suspend fun getGenrePreferences(): Result<List<GenrePreferenceResponse>> {
        return resultBody(genresDataSource.getGenrePreferences())
    }

    override suspend fun saveUserGenre(preference: UpsertPreferenceRequest): Result<GenrePreferenceResponse> {
        return resultBody(genresDataSource.saveUserGenre(preference))
    }

    override suspend fun deleteGenrePreference(request: DeleteGenrePreferenceRequest): Result<Unit> {
        return resultBody(genresDataSource.deleteGenrePreference(request))
    }
}

