package com.example.booknest.network

import com.example.booknest.viewmodel.SignupData
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.Decoder
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.DELETE
import okhttp3.MultipartBody

@Serializable
data class UserData(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val userType: String?,
    val birthDate: String? = null,
    val emailVerified: Boolean = false,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val profilePictureUrl: String? = null,
    val googleId: String? = null,
    val address: UserAddress? = null
)

@Serializable
data class UserAddress(
    val street: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val country: String?
)

data class LoginSuccessResponse(
    val success: Boolean,
    val message: String?,
    val data: LoginData
)

@Serializable
data class LoginData(
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

// Enums
enum class AgeRating(val value: String) { 
    ALL("all"), 
    AGE_13_PLUS("13+"), 
    AGE_16_PLUS("16+"), 
    AGE_18_PLUS("18+") 
}
enum class DistributionType(val value: String) { 
    PHYSICAL("physical"), 
    DIGITAL("digital"), 
    BOTH("both") 
}
enum class SelectionMethod(val value: String) { 
    AUTHOR_SELECTS("author_selects"), 
    FIRST_COME("first_come"), 
    LOTTERY("lottery") 
}
@Serializable
enum class BookStatus(val value: String) {
    DRAFT("draft"),
    ACTIVE("active"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    ARCHIVED("archived")
}

@Serializable
enum class ApplicationStatus(val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    WITHDRAWN("withdrawn")
}

@Serializable
enum class ReadingStatus(val value: String) {
    NOT_STARTED("not_started"),
    CURRENTLY_READING("currently_reading"),
    FOR_REVIEW("for_review"),
    REVIEWED("reviewed")
}

enum class ReviewType(val value: String) {
    TEXT("text"),
    LINK("link")
}

// Custom serializers for enums
object AgeRatingSerializer : KSerializer<AgeRating> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AgeRating", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: AgeRating) {
        encoder.encodeString(value.value)
    }
    
    override fun deserialize(decoder: Decoder): AgeRating {
        val string = decoder.decodeString()
        return AgeRating.values().find { it.value == string } ?: AgeRating.ALL
    }
}

object DistributionTypeSerializer : KSerializer<DistributionType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DistributionType", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: DistributionType) {
        encoder.encodeString(value.value)
    }
    
    override fun deserialize(decoder: Decoder): DistributionType {
        val string = decoder.decodeString()
        return DistributionType.values().find { it.value == string } ?: DistributionType.DIGITAL
    }
}

object SelectionMethodSerializer : KSerializer<SelectionMethod> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SelectionMethod", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: SelectionMethod) {
        encoder.encodeString(value.value)
    }
    
    override fun deserialize(decoder: Decoder): SelectionMethod {
        val string = decoder.decodeString()
        return SelectionMethod.values().find { it.value == string } ?: SelectionMethod.AUTHOR_SELECTS
    }
}

object BookStatusSerializer : KSerializer<BookStatus> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BookStatus", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: BookStatus) {
        encoder.encodeString(value.value)
    }
    
    override fun deserialize(decoder: Decoder): BookStatus {
        val string = decoder.decodeString()
        return BookStatus.values().find { it.value == string } ?: BookStatus.DRAFT
    }
}

object ApplicationStatusSerializer : KSerializer<ApplicationStatus> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ApplicationStatus", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: ApplicationStatus) {
        encoder.encodeString(value.value)
    }
    
    override fun deserialize(decoder: Decoder): ApplicationStatus {
        val string = decoder.decodeString()
        return ApplicationStatus.values().find { it.value == string } ?: ApplicationStatus.PENDING
    }
}

object ReadingStatusSerializer : KSerializer<ReadingStatus> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ReadingStatus", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: ReadingStatus) {
        encoder.encodeString(value.value)
    }
    
    override fun deserialize(decoder: Decoder): ReadingStatus {
        val string = decoder.decodeString()
        return ReadingStatus.values().find { it.value == string } ?: ReadingStatus.NOT_STARTED
    }
}

object ReviewTypeSerializer : KSerializer<ReviewType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ReviewType", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: ReviewType) {
        encoder.encodeString(value.value)
    }
    
    override fun deserialize(decoder: Decoder): ReviewType {
        val string = decoder.decodeString()
        return ReviewType.values().find { it.value == string } ?: ReviewType.TEXT
    }
}

@Serializable
data class Book(
    val id: String,
    val authorId: String,
    val title: String,
    val shortDescription: String?,
    val fullDescription: String?,
    val coverImageUrl: String?,
    val pageCount: Int?,
    val ageRating: String?,
    val distributionType: String?,
    val fileUrl: String?,
    val fileSize: String?,
    val fileType: String?,
    val totalCopies: Int? = null,
    val availableCopies: Int? = null,
    val applicationDeadline: String? = null,
    val reviewDeadlineDays: Int? = null,
    val selectionCriteria: String?,
    @Serializable(with = SelectionMethodSerializer::class)
    val selectionMethod: SelectionMethod?,
    @Serializable(with = BookStatusSerializer::class)
    val status: BookStatus?,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val publishedAt: String?,
    val seriesId: String?,
    val seriesOrder: Int?,
    val seriesName: String?,
    val authorName: String? = null,
    val author: BookAuthor? = null,
    val rating: Double? = null,
    val genres: List<Genre>? = null
)

@Serializable
data class BookAuthor(
    val id: String,
    val username: String,
    val name: String,
    val bio: String?,
    val profilePictureUrl: String?
)

@Serializable
data class Genre(
    val id: Int,
    val name: String,
    val description: String?
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

@Serializable
data class BookStats(
    val totalApplications: Int,
    val approvedReaders: Int,
    val reviewsSubmitted: Int,
    val averageRating: Double?,
    val readingProgress: Map<String, Int>?
)

@Serializable
data class BookAnalytics(
    val views: Int,
    val clicks: Int,
    val applicationConversionRate: Double,
    val reviewCompletionRate: Double,
    val timeToComplete: Double?
)

@Serializable
data class Application(
    val id: String,
    val status: String,
    val appliedAt: String,
    val respondedAt: String? = null,
    val applicationMessage: String? = null,
    val authorNotes: String? = null,
    val bookId: String,
    val bookTitle: String,
    val bookCoverImageUrl: String?,
    val authorName: String,
    val readingStatus: String,
    val readingStartedAt: String? = null,
    val readingCompletedAt: String? = null,
    val copySentAt: String? = null,
    val copyReceivedAt: String? = null,
    val reviewSubmittedAt: String? = null,
    val reader: ApplicationReader? = null,
    val book: Book? = null,
    val review: Review? = null
)

@Serializable
data class ApplicationReader(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val profilePictureUrl: String?
)

@Serializable
data class Review(
    val id: String,
    val applicationId: String,
    val rating: Int,
    @Serializable(with = ReviewTypeSerializer::class)
    val reviewType: ReviewType,
    val reviewContent: String? = null,
    val reviewUrls: List<String>? = null,
    val isPublic: Boolean,
    val isFeatured: Boolean,
    val wordCount: Int? = null,
    val createdAt: String,
    val updatedAt: String,
    val application: Application? = null
)

// DTOs for creating/updating applications and reviews
@Serializable
data class CreateApplicationDto(
    val bookId: String,
    val applicationMessage: String? = null
)

@Serializable
data class UpdateApplicationDto(
    val applicationMessage: String? = null
)

@Serializable
data class UpdateReadingStatusDto(
    @Serializable(with = ReadingStatusSerializer::class)
    val readingStatus: ReadingStatus
)

@Serializable
data class ApproveApplicationDto(
    val authorNotes: String? = null
)

@Serializable
data class RejectApplicationDto(
    val authorNotes: String? = null
)

@Serializable
data class BulkActionDto(
    val applicationIds: List<String>,
    val action: String, // "approve" or "reject"
    val authorNotes: String? = null
)

@Serializable
data class ApplicationCheckResponse(
    val hasApplied: Boolean,
    val application: ApplicationCheckApplication?
)

@Serializable
data class ApplicationCheckApplication(
    val id: String,
    val status: String,
    val appliedAt: String,
    val applicationMessage: String? = null,
    val authorNotes: String? = null,
    val respondedAt: String? = null,
    val book: ApplicationCheckBook? = null
)

@Serializable
data class ApplicationCheckBook(
    val id: String,
    val title: String,
    val authorId: String
)

@Serializable
data class CreateReviewDto(
    val applicationId: String,
    val rating: Int,
    @Serializable(with = ReviewTypeSerializer::class)
    val reviewType: ReviewType,
    val reviewContent: String? = null,
    val reviewUrls: List<String>? = null,
    val isPublic: Boolean = true
)

@Serializable
data class UpdateReviewDto(
    val rating: Int? = null,
    @Serializable(with = ReviewTypeSerializer::class)
    val reviewType: ReviewType? = null,
    val reviewContent: String? = null,
    val reviewUrls: List<String>? = null,
    val isPublic: Boolean? = null
)

// Token refresh data classes
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

// Profile and Stats data models
//@Serializable
//data class UserProfile(
//    val id: String,
//    val username: String,
//    val firstName: String?,
//    val lastName: String?,
//    val userType: String,
//    val bio: String?,
//    val avatarUrl: String?,
//    val isVerified: Boolean,
//    val createdAt: String,
//    val stats: UserStats?
//)

@Serializable
data class UserStats(
    val totalBooks: Int? = null,
    val publishedBooks: Int? = null,
    val draftBooks: Int? = null,
    val totalApplications: Int,
    val approvedApplications: Int,
    val pendingApplications: Int,
    val completedReads: Int? = null,
    val totalReviews: Int? = null,
    val averageRating: Double? = null,
    val userType: String
)

@Serializable
data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class UserStatsResponse(
    val user: UserData,
    val stats: UserStats
)

@Serializable
data class AuthorStatsResponse(
    val author: UserData,
    val stats: UserStats
)

// Enhanced Analytics data models
@Serializable
data class BookAnalyticsSummary(
    val totalApplications: Int,
    val approvedApplications: Int,
    val pendingApplications: Int,
    val rejectedApplications: Int,
    val totalReviews: Int,
    val averageRating: Double,
    val positiveFeedback: Int
)

@Serializable
data class RatingDistribution(
    val `1`: Int = 0,
    val `2`: Int = 0,
    val `3`: Int = 0,
    val `4`: Int = 0,
    val `5`: Int = 0
)

@Serializable
data class ReviewTypes(
    val text: Int,
    val link: Int
)

@Serializable
data class ReviewAnalytics(
    val totalReviews: Int,
    val averageRating: Double,
    val positiveFeedback: Int,
    val ratingDistribution: RatingDistribution,
    val reviewTypes: ReviewTypes,
    val averageWordCount: Int
)

@Serializable
data class ApplicationAnalytics(
    val totalApplications: Int,
    val approvedApplications: Int,
    val pendingApplications: Int,
    val rejectedApplications: Int,
    val approvalRate: Int,
    val rejectionRate: Int
)

@Serializable
data class RecentReview(
    val id: String,
    val rating: Int,
    val reviewType: String,
    val reviewContent: String?,
    val wordCount: Int,
    val createdAt: String,
    val application: RecentReviewApplication
)

@Serializable
data class RecentReviewApplication(
    val reader: RecentReviewReader
)

@Serializable
data class RecentReviewReader(
    val id: String,
    val username: String,
    val firstName: String?,
    val lastName: String?
)

@Serializable
data class DetailedBookAnalytics(
    val bookId: String,
    val summary: BookAnalyticsSummary,
    val reviewAnalytics: ReviewAnalytics,
    val applicationAnalytics: ApplicationAnalytics,
    val recentReviews: List<RecentReview>
)

@Serializable
data class AuthorAnalyticsOverview(
    val totalBooks: Int,
    val publishedBooks: Int,
    val draftBooks: Int,
    val totalApplications: Int,
    val approvedApplications: Int,
    val totalReviews: Int,
    val averageRating: Double,
    val overallApprovalRate: Int
)

@Serializable
data class TopPerformingBook(
    val bookId: String,
    val title: String,
    val averageRating: Double,
    val reviewCount: Int
)

@Serializable
data class AuthorPerformance(
    val booksWithReviews: Int,
    val averageRating: Double,
    val topPerformingBooks: List<TopPerformingBook>
)

@Serializable
data class MonthlyData(
    val month: String,
    val count: Int
)

@Serializable
data class AuthorTrends(
    val monthlyApplications: List<MonthlyData>,
    val monthlyReviews: List<MonthlyData>
)

@Serializable
data class AuthorAnalytics(
    val authorId: String,
    val overview: AuthorAnalyticsOverview,
    val performance: AuthorPerformance,
    val trends: AuthorTrends
)

// Enhanced User Stats
@Serializable
data class EnhancedUserStats(
    val totalBooks: Int? = null,
    val publishedBooks: Int? = null,
    val draftBooks: Int? = null,
    val totalApplications: Int,
    val approvedApplications: Int,
    val pendingApplications: Int,
    val approvalRate: Int? = null,
    val successRate: Int? = null,
    val completedReads: Int? = null,
    val totalReviews: Int? = null,
    val averageRating: Double? = null,
    val booksWithReviews: Int? = null,
    val totalWordCount: Int? = null,
    val userType: String
)

interface ApiService {
    @POST("/auth/register")
    suspend fun register(@Body data: SignupData): Response<ApiResponse<RegisterResponse>>

    @POST("/auth/login")
    suspend fun login(@Body data: LoginRequest): Response<LoginSuccessResponse>

    @POST("/auth/refresh-token")
    suspend fun refreshToken(@Body data: RefreshTokenRequest): Response<ApiResponse<RefreshTokenResponse>>

    @GET("/genres")
    suspend fun getGenres(): Response<ApiResponse<List<GenreDto>>>

    @POST("/me/genre-preferences")
    suspend fun saveUserGenres(@Body preference: UpsertPreferenceRequest): Response<ApiResponse<Unit>>

    @POST("/genres")
    suspend fun addGenre(@Body genre: CreateGenreRequest): Response<ApiResponse<GenreDto>>

    // Reader Book Endpoints
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
    ): Response<ApiResponse<List<Book>>>

    @GET("/books/featured")
    suspend fun getFeaturedBooks(): Response<ApiResponse<List<Book>>>

    @GET("/books/search")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("skip") skip: Int?,
        @Query("take") take: Int?
    ): Response<ApiResponse<List<Book>>>

    @GET("/books/recommended")
    suspend fun getRecommendedBooks(
        @Query("take") take: Int?
    ): Response<ApiResponse<List<Book>>>

    @GET("/books/{bookId}")
    suspend fun getBookDetails(@Path("bookId") bookId: String): Response<ApiResponse<Book>>

    @POST("/books")
    suspend fun createBook(@Body book: CreateBookDto): Response<ApiResponse<Book>>

    @GET("/books/my")
    suspend fun getMyBooks(): Response<ApiResponse<List<Book>>>

    @PUT("/books/{bookId}")
    suspend fun updateBook(@Path("bookId") bookId: String, @Body book: UpdateBookDto): Response<ApiResponse<Book>>

    @DELETE("/books/{bookId}")
    suspend fun deleteBook(@Path("bookId") bookId: String): Response<ApiResponse<Unit>>

    @POST("/books/{bookId}/publish")
    suspend fun publishBook(@Path("bookId") bookId: String): Response<ApiResponse<Book>>

    @GET("/books/{bookId}/stats")
    suspend fun getBookStats(@Path("bookId") bookId: String): Response<ApiResponse<BookStats>>

    @GET("/books/{bookId}/analytics")
    suspend fun getBookAnalytics(@Path("bookId") bookId: String): Response<ApiResponse<BookAnalytics>>

    // File Management (Placeholder)
    @POST("/books/{bookId}/upload")
    suspend fun uploadBookFile(@Path("bookId") bookId: String): Response<ApiResponse<Unit>>

    @GET("/books/{bookId}/download")
    suspend fun downloadBook(@Path("bookId") bookId: String): Response<ApiResponse<Unit>>

    // Series Management Endpoints
    @GET("/series/my")
    suspend fun getMySeries(): Response<ApiResponse<List<Series>>>

    @POST("/series")
    suspend fun createSeries(@Body series: CreateSeriesDto): Response<ApiResponse<Series>>

    @PATCH("/series/{seriesId}")
    suspend fun updateSeries(@Path("seriesId") seriesId: String, @Body series: UpdateSeriesDto): Response<ApiResponse<Series>>

    @DELETE("/series/{seriesId}")
    suspend fun deleteSeries(@Path("seriesId") seriesId: String): Response<ApiResponse<Unit>>

    // Application Endpoints
    // Reader Operations
    @POST("/applications")
    suspend fun createApplication(@Body application: CreateApplicationDto): Response<ApiResponse<Application>>

    @GET("/applications/my")
    suspend fun getMyApplications(): Response<ApiResponse<List<Application>>>

    @GET("/applications/check/{bookId}")
    suspend fun checkApplication(@Path("bookId") bookId: String): Response<ApiResponse<ApplicationCheckResponse>>

    @GET("/applications/{applicationId}")
    suspend fun getApplication(@Path("applicationId") applicationId: String): Response<ApiResponse<Application>>

    @PUT("/applications/{applicationId}")
    suspend fun updateApplication(@Path("applicationId") applicationId: String, @Body application: UpdateApplicationDto): Response<ApiResponse<Application>>

    @DELETE("/applications/{applicationId}")
    suspend fun withdrawApplication(@Path("applicationId") applicationId: String): Response<ApiResponse<Unit>>

    @PUT("/applications/{applicationId}/mark-received")
    suspend fun markCopyReceived(@Path("applicationId") applicationId: String): Response<ApiResponse<Application>>

    @PUT("/applications/{applicationId}/reading-status")
    suspend fun updateReadingStatus(@Path("applicationId") applicationId: String, @Body status: UpdateReadingStatusDto): Response<ApiResponse<Application>>

    @GET("/applications/my/reading-progress")
    suspend fun getReadingProgress(): Response<ApiResponse<List<Application>>>

    // Author Operations
    @GET("/applications/books/{bookId}")
    suspend fun getBookApplications(@Path("bookId") bookId: String): Response<ApiResponse<List<Application>>>

    @PUT("/applications/{applicationId}/approve")
    suspend fun approveApplication(@Path("applicationId") applicationId: String, @Body approval: ApproveApplicationDto): Response<ApiResponse<Application>>

    @PUT("/applications/{applicationId}/reject")
    suspend fun rejectApplication(@Path("applicationId") applicationId: String, @Body rejection: RejectApplicationDto): Response<ApiResponse<Application>>

    @POST("/applications/bulk-action")
    suspend fun bulkActionApplications(@Body action: BulkActionDto): Response<ApiResponse<List<Application>>>

    @PUT("/applications/{applicationId}/mark-sent")
    suspend fun markCopySent(@Path("applicationId") applicationId: String): Response<ApiResponse<Application>>

    // Review Endpoints
    @POST("/reviews")
    suspend fun createReview(@Body review: CreateReviewDto): Response<ApiResponse<Review>>

    @GET("/reviews/{reviewId}")
    suspend fun getReview(@Path("reviewId") reviewId: String): Response<ApiResponse<Review>>

    @PUT("/reviews/{reviewId}")
    suspend fun updateReview(@Path("reviewId") reviewId: String, @Body review: UpdateReviewDto): Response<ApiResponse<Review>>

    @DELETE("/reviews/{reviewId}")
    suspend fun deleteReview(@Path("reviewId") reviewId: String): Response<ApiResponse<Unit>>

    @GET("/reviews/books/{bookId}")
    suspend fun getBookReviews(@Path("bookId") bookId: String): Response<ApiResponse<List<Review>>>

    @GET("/reviews/users/{userId}")
    suspend fun getUserReviews(@Path("userId") userId: String): Response<ApiResponse<List<Review>>>

    @GET("/reviews/featured")
    suspend fun getFeaturedReviews(): Response<ApiResponse<List<Review>>>

    @PUT("/reviews/{reviewId}/feature")
    suspend fun featureReview(@Path("reviewId") reviewId: String): Response<ApiResponse<Review>>

    @PUT("/reviews/{reviewId}/unfeature")
    suspend fun unfeatureReview(@Path("reviewId") reviewId: String): Response<ApiResponse<Review>>

    // Profile and Stats Endpoints
    @GET("/users/profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<ApiResponse<UserProfile>>

    @GET("/users/me/stats")
    suspend fun getMyStats(): Response<ApiResponse<UserStatsResponse>>

    @GET("/users/profile/{authorId}/stats")
    suspend fun getAuthorStats(@Path("authorId") authorId: String): Response<ApiResponse<AuthorStatsResponse>>

    // Enhanced Analytics Endpoints
    @GET("/books/{bookId}/analytics/detailed")
    suspend fun getDetailedBookAnalytics(@Path("bookId") bookId: String): Response<ApiResponse<DetailedBookAnalytics>>

    @GET("/books/analytics/author")
    suspend fun getAuthorAnalytics(): Response<ApiResponse<AuthorAnalytics>>
    
    // Email Verification Endpoints (New 6-digit code system)
    @POST("/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailDto): Response<VerifyEmailResponse>
    
    @POST("/auth/request-password-reset")
    suspend fun requestPasswordReset(@Body request: RequestPasswordResetDto): Response<ApiResponse<Unit>>
    
    @POST("/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordDto): Response<ApiResponse<AuthResponse>>
    
    @POST("/auth/resend-verification")
    suspend fun resendVerification(@Body request: Map<String, String>): Response<ApiResponse<Unit>>
    
    // File Upload/Download Endpoints
    @Multipart
    @POST("/books/{bookId}/upload")
    suspend fun uploadBookFile(
        @Path("bookId") bookId: String,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<UploadBookResponse>>
    
    @GET("/books/{bookId}/download")
    suspend fun getBookDownloadUrl(
        @Path("bookId") bookId: String
    ): Response<ApiResponse<DownloadBookResponse>>

    @Multipart
    @POST("/files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<UploadFileResponse>>
    
    @GET("/files/download/{key}")
    suspend fun getFileDownloadUrl(
        @Path("key") key: String
    ): Response<ApiResponse<DownloadFileResponse>>
    
    @DELETE("/files/{key}")
    suspend fun deleteFile(
        @Path("key") key: String
    ): Response<ApiResponse<Unit>>
    
    @GET("/files/metadata/{key}")
    suspend fun getFileMetadata(
        @Path("key") key: String
    ): Response<ApiResponse<FileMetadataResponse>>
    
    // Google OAuth Endpoint
    @POST("/auth/google")
    suspend fun authenticateWithGoogle(@Body request: GoogleAuthRequest): Response<GoogleAuthResponse>
    
    // Friend Management Endpoints
    @POST("/friends/request/{username}")
    suspend fun sendFriendRequest(@Path("username") username: String): Response<ApiResponse<FriendRequest>>
    
    @POST("/friends/accept/{requesterId}")
    suspend fun acceptFriendRequest(@Path("requesterId") requesterId: String): Response<ApiResponse<FriendRequest>>
    
    @DELETE("/friends/decline/{requesterId}")
    suspend fun declineFriendRequest(@Path("requesterId") requesterId: String): Response<Unit>
    
    @DELETE("/friends/unfriend/{friendId}")
    suspend fun unfriendUser(@Path("friendId") friendId: String): Response<Unit>
    
    @POST("/friends/block/{userId}")
    suspend fun blockUser(@Path("userId") userId: String): Response<ApiResponse<FriendRequest>>
    
    @DELETE("/friends/unblock/{userId}")
    suspend fun unblockUser(@Path("userId") userId: String): Response<Unit>
    
    @GET("/friends")
    suspend fun getFriends(
        @Query("status") status: String? = "accepted",
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<List<FriendRequest>>>
    
    @GET("/friends/requests")
    suspend fun getFriendRequests(
        @Query("type") type: String? = "received",
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<List<FriendRequest>>>
    
    @GET("/friends/status/{userId}")
    suspend fun getFriendshipStatus(@Path("userId") userId: String): Response<ApiResponse<FriendshipStatus>>
    
    @GET("/friends/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<List<UserData>>>
    
    @GET("/friends/activity")
    suspend fun getFriendsActivity(
        @Query("limit") limit: Int? = 50
    ): Response<ApiResponse<List<UserActivity>>>
    
    // User Profile Endpoints
    @GET("/profiles/me")
    suspend fun getMyProfile(): Response<ApiResponse<UserProfile>>
    
    @PUT("/profiles/me")
    suspend fun updateMyProfile(@Body profile: UpdateProfileRequest): Response<ApiResponse<UserProfile>>
    
    @GET("/profiles/social-media/options")
    suspend fun getSocialMediaOptions(): Response<ApiResponse<SocialMediaOptions>>
    
    @PUT("/profiles/me/social-media")
    suspend fun updateSocialMedia(@Body request: UpdateSocialMediaRequest): Response<ApiResponse<UserProfile>>
    
    @PUT("/profiles/me/privacy")
    suspend fun updatePrivacySettings(@Body request: UpdatePrivacyRequest): Response<ApiResponse<UserProfile>>
    
    @PUT("/profiles/me/notifications")
    suspend fun updateNotificationSettings(@Body request: UpdateNotificationRequest): Response<ApiResponse<UserProfile>>
    
    @GET("/profiles/user/{username}")
    suspend fun getPublicUserProfile(@Path("username") username: String): Response<ApiResponse<PublicUserProfile>>
    
    @GET("/profiles/me/activity")
    suspend fun getMyActivity(
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<List<UserActivity>>>
    
    @GET("/profiles/me/activity/public")
    suspend fun getMyPublicActivity(
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<List<UserActivity>>>
    
    @GET("/profiles/me/activity/recent")
    suspend fun getMyRecentActivity(
        @Query("days") days: Int? = 7,
        @Query("limit") limit: Int? = 50
    ): Response<ApiResponse<List<UserActivity>>>
    
    @GET("/profiles/me/activity/stats")
    suspend fun getMyActivityStats(): Response<ApiResponse<ActivityStats>>
    
    // Author Following Endpoints
    @POST("/authors/follow/{username}")
    suspend fun followAuthor(@Path("username") username: String): Response<ApiResponse<AuthorFollow>>
    
    @DELETE("/authors/unfollow/{authorId}")
    suspend fun unfollowAuthor(@Path("authorId") authorId: String): Response<Unit>
    
    @GET("/authors/following")
    suspend fun getFollowedAuthors(): Response<ApiResponse<List<AuthorFollow>>>
    
    @GET("/authors/following/with-stats")
    suspend fun getFollowedAuthorsWithStats(): Response<ApiResponse<List<AuthorFollowWithStats>>>
    
    @GET("/authors/followers/{authorId}")
    suspend fun getAuthorFollowers(@Path("authorId") authorId: String): Response<ApiResponse<List<AuthorFollow>>>
    
    @GET("/authors/following/check/{authorId}")
    suspend fun checkIfFollowingAuthor(@Path("authorId") authorId: String): Response<ApiResponse<Map<String, Boolean>>>
    
    @GET("/authors/following/books")
    suspend fun getBooksFromFollowedAuthors(
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<List<Book>>>
    
    // Enhanced User Search
    @GET("/users/search")
    suspend fun searchUsersEnhanced(
        @Query("q") query: String,
        @Query("limit") limit: Int? = 20
    ): Response<ApiResponse<UserSearchResult>>
}

@Serializable
data class LoginRequest(
    val identifier: String, // username or email
    val password: String
)


// Email Verification data classes
@Serializable
data class EmailVerificationRequest(
    val token: String
)

@Serializable
data class EmailVerificationResponse(
    val success: Boolean,
    val message: String?,
    val user: UserData? = null
)

@Serializable
data class VerificationStatusResponse(
    val success: Boolean,
    val message: String?,
    val isVerified: Boolean,
    val emailVerified: Boolean
)

@Serializable
data class ResendVerificationRequest(
    val email: String
)

@Serializable
data class ResendVerificationResponse(
    val success: Boolean,
    val message: String?
)

// Generic API Response Wrapper
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T? = null,
    val statusCode: Int? = null,
    val timestamp: String? = null
)

// Google OAuth data classes
@Serializable
data class GoogleAuthRequest(
    val idToken: String,
    val userType: String // "reader" or "author"
)

@Serializable
data class GoogleAuthResponse(
    val success: Boolean,
    val message: String?,
    val data: GoogleAuthData?
)

@Serializable
data class GoogleAuthData(
    val user: UserData,
    val accessToken: String,
    val refreshToken: String
)

// New 6-digit verification code data classes
@Serializable
data class VerifyEmailDto(
    val code: String
)

@Serializable
data class RequestPasswordResetDto(
    val email: String
)

@Serializable
data class ResetPasswordDto(
    val code: String,
    val newPassword: String
)

@Serializable
data class AuthResponse(
    val user: UserData,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class VerifyEmailResponse(
    val success: Boolean,
    val message: String?,
    val user: UserData?
)

// File Upload/Download Data Classes
@Serializable
data class UploadBookResponse(
    val book: Book,
    val file: BookFile
)

@Serializable
data class BookFile(
    val url: String,
    val size: Long,
    val type: String,
    val originalName: String
)

@Serializable
data class DownloadBookResponse(
    val downloadUrl: String,
    val expiresIn: Int,
    val fileName: String,
    val fileSize: String,
    val fileType: String
)

@Serializable
data class ReadBookResponse(
    val fileUrl: String,
    val expiresIn: Int,
    val fileName: String,
    val fileSize: String,
    val fileType: String,
    val bookId: String,
    val pageCount: Int? = null
)

@Serializable
data class UploadFileResponse(
    val url: String,
    val key: String,
    val size: Long,
    val type: String,
    val originalName: String
)

@Serializable
data class DownloadFileResponse(
    val downloadUrl: String,
    val expiresIn: Int,
    val fileName: String,
    val fileSize: String,
    val fileType: String
)

@Serializable
data class FileMetadataResponse(
    val key: String,
    val url: String,
    val size: Long,
    val type: String,
    val originalName: String,
    val uploadedAt: String,
    val lastModified: String
)

@Serializable
data class RegisterResponse(
    val user: UserData,
    val accessToken: String,
    val refreshToken: String
)

// Friend functionality data models
@Serializable
data class FriendRequest(
    val id: String,
    val requesterId: String,
    val addresseeId: String,
    val status: String, // "pending" | "accepted" | "blocked"
    val createdAt: String,
    val updatedAt: String,
    val requester: UserData? = null,
    val addressee: UserData? = null
)

@Serializable
data class FriendshipStatus(
    val status: String?, // "pending" | "accepted" | "blocked" | null
    val isRequester: Boolean
)

@Serializable
data class UserProfile(
    val id: String,
    val userId: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val userType: String? = null,
    val birthDate: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val createdAt: String,
    val updatedAt: String? = null,
    val stats: UserStats? = null,
    val socialMedia: SocialMedia? = null,
    val activityPrivacy: String? = null,
    val profilePrivacy: String? = null,
    val readingListPrivacy: String? = null,
    val reviewsPrivacy: String? = null,
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = true
)

@Serializable
data class SocialMedia(
    val instagram: String? = null,
    val tiktok: String? = null,
    val youtube: String? = null,
    val goodreads: String? = null,
    val custom: List<CustomSocialLink>? = null
)

@Serializable
data class CustomSocialLink(
    val platform: String,
    val url: String
)

@Serializable
data class SocialMediaOption(
    val key: String,
    val name: String,
    val icon: String,
    val placeholder: String
)

@Serializable
data class SocialMediaOptions(
    val predefined: List<SocialMediaOption>,
    val custom: CustomSocialOptions
)

@Serializable
data class CustomSocialOptions(
    val enabled: Boolean,
    val maxCustomLinks: Int,
    val placeholder: String
)

@Serializable
data class UserActivity(
    val id: String,
    val userId: String,
    val activityType: String, // "book_applied" | "book_approved" | "review_posted" | etc.
    val bookId: String? = null,
    val applicationId: String? = null,
    val metadata: Map<String, String>? = null,
    val createdAt: String,
    val user: UserData? = null,
    val book: Book? = null,
    val application: Application? = null
)

@Serializable
data class AuthorFollow(
    val id: String,
    val followerId: String,
    val authorId: String,
    val createdAt: String,
    val follower: UserData? = null,
    val author: UserData? = null
)

@Serializable
data class AuthorFollowWithStats(
    val author: UserData,
    val follow: AuthorFollow,
    val stats: AuthorStats
)

@Serializable
data class AuthorStats(
    val totalBooks: Int,
    val publishedBooks: Int,
    val totalApplications: Int
)

@Serializable
data class ActivityStats(
    val totalActivities: Int,
    val activitiesByType: Map<String, Int>,
    val lastActivity: String
)

@Serializable
data class PublicUserProfile(
    val user: UserData,
    val profile: UserProfile,
    val isFriend: Boolean
)

@Serializable
data class UserSearchResult(
    val data: List<UserData>,
    val total: Int
)

// Request DTOs
@Serializable
data class UpdateSocialMediaRequest(
    val instagram: String? = null,
    val tiktok: String? = null,
    val youtube: String? = null,
    val goodreads: String? = null,
    val custom: List<CustomSocialLink>? = null
)

@Serializable
data class UpdatePrivacyRequest(
    val activityPrivacy: String? = null,
    val profilePrivacy: String? = null,
    val readingListPrivacy: String? = null,
    val reviewsPrivacy: String? = null
)

@Serializable
data class UpdateNotificationRequest(
    val notificationsEnabled: Boolean? = null,
    val emailNotifications: Boolean? = null
)