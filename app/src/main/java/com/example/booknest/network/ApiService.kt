package com.example.booknest.network

import com.example.booknest.viewmodel.SignupData
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE

@Serializable
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

enum class AgeRating { ALL, AGE_13_PLUS, AGE_16_PLUS, AGE_18_PLUS }
enum class DistributionType { PHYSICAL, DIGITAL, BOTH }
enum class SelectionMethod { AUTHOR_SELECTS, FIRST_COME, LOTTERY }
enum class BookStatus { DRAFT, ACTIVE, IN_PROGRESS, COMPLETED, ARCHIVED }

data class Book(
    val id: String,
    val authorId: String,
    val title: String,
    val shortDescription: String?,
    val fullDescription: String?,
    val coverImageUrl: String?,
    val pageCount: Int?,
    val ageRating: AgeRating,
    val distributionType: DistributionType,
    val fileUrl: String?,
    val fileSize: String?,
    val fileType: String?,
    val totalCopies: Int,
    val availableCopies: Int,
    val applicationDeadline: String,
    val reviewDeadlineDays: Int,
    val selectionCriteria: String?,
    val selectionMethod: SelectionMethod,
    val status: BookStatus,
    val createdAt: String,
    val updatedAt: String,
    val publishedAt: String?,
    val seriesId: String?,
    val seriesOrder: Int?
)

data class Series(
    val id: String,
    val authorId: String,
    val name: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateBookDto(
    val title: String,
    val shortDescription: String? = null,
    val fullDescription: String? = null,
    val coverImageUrl: String? = null,
    val pageCount: Int? = null,
    val ageRating: AgeRating,
    val distributionType: DistributionType,
    val fileUrl: String? = null,
    val fileSize: Int? = null,
    val fileType: String? = null,
    val totalCopies: Int = 1,
    val availableCopies: Int? = null,
    val applicationDeadline: String,
    val reviewDeadlineDays: Int = 30,
    val selectionCriteria: String? = null,
    val selectionMethod: SelectionMethod = SelectionMethod.AUTHOR_SELECTS,
    val genreIds: List<Int>? = null,
    val seriesId: String? = null,
    val seriesOrder: Int? = null
)

data class UpdateBookDto(
    val title: String? = null,
    val shortDescription: String? = null,
    val fullDescription: String? = null,
    val coverImageUrl: String? = null,
    val pageCount: Int? = null,
    val ageRating: AgeRating? = null,
    val distributionType: DistributionType? = null,
    val fileUrl: String? = null,
    val fileSize: Int? = null,
    val fileType: String? = null,
    val totalCopies: Int? = null,
    val availableCopies: Int? = null,
    val applicationDeadline: String? = null,
    val reviewDeadlineDays: Int? = null,
    val selectionCriteria: String? = null,
    val selectionMethod: SelectionMethod? = null,
    val genreIds: List<Int>? = null,
    val seriesId: String? = null,
    val seriesOrder: Int? = null
)

data class CreateSeriesDto(
    val name: String,
    val description: String? = null
)

data class UpdateSeriesDto(
    val name: String? = null,
    val description: String? = null
)

interface ApiService {
    @POST("/auth/register")
    suspend fun register(@Body data: SignupData): Response<ApiResponse>

    @POST("/auth/login")
    suspend fun login(@Body data: LoginRequest): Response<LoginSuccessResponse>

    @POST("/auth/refresh-token")
    suspend fun refreshToken(@Body data: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("/genres")
    suspend fun getGenres(): List<GenreDto>

    @POST("/me/genre-preferences")
    suspend fun saveUserGenres(@Body preference: UpsertPreferenceRequest): Response<ApiResponse>

    @POST("/genres")
    suspend fun addGenre(@Body genre: CreateGenreRequest): Response<GenreDto>

    // Book Endpoints
    @GET("/books")
    suspend fun browseBooks(
        @Query("search") query: String?,
        @Query("genreId") genreId: Int?,
        @Query("ageRating") ageRating: String?,
        @Query("distributionType") distributionType: String?,
        @Query("publishedFrom") publishedFrom: String?,
        @Query("publishedTo") publishedTo: String?,
        @Query("skip") skip: Int?,
        @Query("take") take: Int?,
        @Query("status") status: String?
    ): Response<List<Book>>

    @GET("/books/featured")
    suspend fun getFeaturedBooks(): Response<List<Book>>

    @GET("/books/search")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("skip") skip: Int?,
        @Query("take") take: Int?
    ): Response<List<Book>>

    @GET("/books/{bookId}")
    suspend fun getBookDetails(@Path("bookId") bookId: String): Response<Book>

    @POST("/books")
    suspend fun createBook(@Body book: CreateBookDto): Response<Book>

    @GET("/books/my")
    suspend fun getMyBooks(): Response<List<Book>>

    @PUT("/books/{bookId}")
    suspend fun updateBook(@Path("bookId") bookId: String, @Body book: UpdateBookDto): Response<Book>

    @DELETE("/books/{bookId}")
    suspend fun deleteBook(@Path("bookId") bookId: String): Response<Unit>

    @POST("/books/{bookId}/publish")
    suspend fun publishBook(@Path("bookId") bookId: String): Response<Book>

    @POST("/books/{bookId}/upload")
    suspend fun uploadBookFile(@Path("bookId") bookId: String): Response<Unit>

    @GET("/books/{bookId}/download")
    suspend fun downloadBook(@Path("bookId") bookId: String): Response<Unit>

    @GET("/books/{bookId}/stats")
    suspend fun getBookStats(@Path("bookId") bookId: String): Response<Unit> // Define stats model

    @GET("/books/{bookId}/analytics")
    suspend fun getBookAnalytics(@Path("bookId") bookId: String): Response<Unit> // Define analytics model

    @GET("/books/recommended")
    suspend fun getRecommendedBooks(
        @Query("take") take: Int?
    ): Response<List<Book>>

    // Series Endpoints
    @GET("/series/my")
    suspend fun getMySeries(): Response<List<Series>>

    @POST("/series")
    suspend fun createSeries(@Body series: CreateSeriesDto): Response<Series>

    @PUT("/series/{seriesId}")
    suspend fun updateSeries(@Path("seriesId") seriesId: String, @Body series: UpdateSeriesDto): Response<Series>

    @DELETE("/series/{seriesId}")
    suspend fun deleteSeries(@Path("seriesId") seriesId: String): Response<Unit>
}

data class LoginRequest(
    val identifier: String, // username or email
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
data class ApiResponse(
    val success: Boolean,
    val message: String?,
    val user: UserData? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null
)
