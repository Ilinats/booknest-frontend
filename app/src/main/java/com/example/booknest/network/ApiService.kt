package com.example.booknest.network

import com.example.booknest.viewmodel.SignupData
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class UserData(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val userType: String?,
    val birthDate: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val emailVerified: Boolean = false,
    val createdAt: String?,
    val updatedAt: String?,
    val lastLogin: String? = null,
    val isActive: Boolean = true
)

data class LoginSuccessResponse(
    val user: UserData,
    val accessToken: String,
    val refreshToken: String
)
data class CreateGenreRequest(
    val name: String,
    val description: String? = null,
    val colorCode: String? = null,
    val icon: String? = null,
    val isActive: Boolean? = null
)

data class GenreDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val colorCode: String? = null,
    val icon: String? = null,
    val isActive: Boolean = true,
    val createdAt: String?
)

data class UpsertPreferenceRequest(
    val genreId: Int,
    val preferenceLevel: Int
)

interface ApiService {
    @POST("/auth/register")
    suspend fun register(@Body data: SignupData): Response<ApiResponse>

    @POST("/auth/login")
    suspend fun login(@Body data: LoginRequest): Response<LoginSuccessResponse>

    @GET("/genres")
    suspend fun getGenres(): List<GenreDto>

    @POST("/me/genre-preferences")
    suspend fun saveUserGenres(@Body preference: UpsertPreferenceRequest): Response<ApiResponse>

    @POST("/genres")
    suspend fun addGenre(@Body genre: CreateGenreRequest): Response<ApiResponse>
}

data class LoginRequest(
    val identifier: String, // username or email
    val password: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String?,
    val user: UserData? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null
)
