package com.example.booknest.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookResponse(
    val id: String,
    @SerialName("authorId")
    val authorId: String? = null,
    val title: String,
    val shortDescription: String? = null,
    val fullDescription: String? = null,
    val coverImageUrl: String? = null,
    val pageCount: Int? = null,
    val ageRating: String? = null,
    val distributionType: String? = null,
    val fileUrl: String? = null,
    val fileSize: String? = null,
    val fileType: String? = null,
    val totalCopies: Int? = null,
    val availableCopies: Int? = null,
    val applicationDeadline: String? = null,
    val reviewDeadline: String? = null,
    val selectionCriteria: String? = null,
    val selectionMethod: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val publishedAt: String? = null,
    val seriesId: String? = null,
    val seriesOrder: Int? = null,
    val seriesName: String? = null,
    val authorName: String? = null,
    val author: BookAuthorResponse? = null,
    val series: SeriesResponse? = null,
    val rating: Double? = null,
    val genres: List<GenreResponse>? = null,
    val bookGenres: List<BookGenreResponse>? = null
) {
    val resolvedAuthorId: String?
        get() = authorId ?: author?.id

    val resolvedGenres: List<GenreResponse>
        get() = genres ?: bookGenres?.mapNotNull { it.genre } ?: emptyList()
}

@Serializable
data class BookGenreResponse(
    val id: String,
    val bookId: String? = null,
    val genreId: Int? = null,
    val genre: GenreResponse? = null,
    val createdAt: String? = null
)

@Serializable
data class RecommendedBookResponse(
    val id: String,
    val title: String,
    val authorName: String? = null,
    val author: BookAuthorResponse? = null,
    val coverImageUrl: String? = null,
    val rating: Double? = null,
    val seriesName: String? = null,
    val seriesOrder: Int? = null,
    val publishedAt: String? = null,
    val applicationDeadline: String? = null,
    val availableCopies: Int? = null,
    val totalCopies: Int? = null,
    val genres: List<GenreResponse>? = null,
    val distributionType: String? = null
) {
    val resolvedAuthorName: String?
        get() = authorName ?: author?.displayName
}

@Serializable
data class BookAuthorResponse(
    val id: String,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
) {
    val displayName: String
        get() = when {
            firstName != null && lastName != null -> "$firstName $lastName"
            username != null -> username
            else -> "Unknown Author"
        }
}

@Serializable
data class SeriesResponse(
    val id: String,
    val authorId: String,
    val name: String,
    val description: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BookStatsResponse(
    @SerialName("totalApplications")
    val totalApplications: Int? = null,
    @SerialName("totalApplicants")
    val totalApplicants: Int? = null,
    val approvedReaders: Int,
    val reviewsSubmitted: Int? = null,
    val averageRating: Double? = null,
    val readingProgress: Map<String, Int>? = null
) {
    val effectiveTotalApplications: Int
        get() = totalApplications ?: totalApplicants ?: 0
}

@Serializable
data class BookAnalyticsResponse(
    val views: Int,
    val clicks: Int,
    val applicationConversionRate: Double,
    val reviewCompletionRate: Double,
    val timeToComplete: Double? = null
)

@Serializable
data class UploadBookFileResponse(
    val book: BookResponse,
    val file: BookFileResponse
)

@Serializable
data class BookFileResponse(
    val url: String,
    val size: Long,
    val type: String,
    val originalName: String
)

@Serializable
data class UploadBookCoverResponse(
    val book: BookResponse,
    val coverImage: BookCoverImageResponse
)

@Serializable
data class BookCoverImageResponse(
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
    val fileType: String? = null
)

@Serializable
data class TrendingBookResponse(
    val book: RecommendedBookResponse,
    val applicationCount: Int
)

