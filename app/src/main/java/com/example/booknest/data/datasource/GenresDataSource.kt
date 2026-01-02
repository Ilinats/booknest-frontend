package com.example.booknest.data.datasource

import com.example.booknest.domain.model.request.CreateGenreRequest
import com.example.booknest.domain.model.request.DeleteGenrePreferenceRequest
import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenrePreferenceResponse
import com.example.booknest.domain.model.response.GenreResponse

interface GenresDataSource {
    suspend fun getGenres(): Result<List<GenreResponse>>
    suspend fun addGenre(genre: CreateGenreRequest): Result<GenreResponse>
    suspend fun getGenrePreferences(): Result<List<GenrePreferenceResponse>>
    suspend fun saveUserGenre(preference: UpsertPreferenceRequest): Result<GenrePreferenceResponse>
    suspend fun deleteGenrePreference(request: DeleteGenrePreferenceRequest): Result<Unit>
}

