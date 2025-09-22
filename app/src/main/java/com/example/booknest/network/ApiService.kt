package com.example.booknest.network

import com.example.booknest.viewmodel.SignupData
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Data class for the nested user object in login response
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

// Data class for the actual login success response from the backend
data class LoginSuccessResponse(
    val user: UserData,
    val accessToken: String,
    val refreshToken: String
)

interface ApiService {
    @POST("/auth/register")
    suspend fun register(@Body data: SignupData): Response<ApiResponse> // Uses existing ApiResponse

    @POST("/auth/login")
    suspend fun login(@Body data: LoginRequest): Response<LoginSuccessResponse> // Changed to LoginSuccessResponse

    @GET("genres")
    suspend fun getGenres(): List<String>

    @POST("genres")
    suspend fun saveUserGenres(@Body genres: GenreRequest): Response<ApiResponse> // Uses existing ApiResponse
}

data class LoginRequest(
    val identifier: String, // username or email
    val password: String
)

// General API response for other calls like register, saveUserGenres
data class ApiResponse(
    val success: Boolean,
    val message: String?
)

data class GenreRequest(
    val username: String,
    val genres: List<String>
)