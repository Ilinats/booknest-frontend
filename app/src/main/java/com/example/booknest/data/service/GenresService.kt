package com.example.booknest.data.service

import com.example.booknest.data.constants.GenrePreferences
import com.example.booknest.data.constants.Genres
import com.example.booknest.data.constants.PathConstants
import com.example.booknest.domain.model.request.CreateGenreRequest
import com.example.booknest.domain.model.request.DeleteGenrePreferenceRequest
import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenrePreferenceResponse
import com.example.booknest.domain.model.response.GenreResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface GenresService {
    @GET(Genres.LIST)
    suspend fun getGenres(): Response<List<GenreResponse>>

    @POST(Genres.LIST)
    suspend fun addGenre(@Body genre: CreateGenreRequest): Response<GenreResponse>

    @GET(GenrePreferences.LIST)
    suspend fun getGenrePreferences(): Response<List<GenrePreferenceResponse>>

    @POST(GenrePreferences.LIST)
    suspend fun saveUserGenre(@Body preference: UpsertPreferenceRequest): Response<GenrePreferenceResponse>

    @DELETE(GenrePreferences.LIST)
    suspend fun deleteGenrePreference(@Body request: DeleteGenrePreferenceRequest): Response<Unit>
}

