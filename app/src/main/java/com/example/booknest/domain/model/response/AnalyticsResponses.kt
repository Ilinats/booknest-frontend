package com.example.booknest.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class DetailedBookAnalyticsResponse(
    val bookId: String,
    val summary: BookAnalyticsSummaryResponse,
    val reviewStatistics: ReviewAnalyticsResponse? = null,
    val reviewAnalytics: ReviewAnalyticsResponse? = null,
    val applicationStatistics: ApplicationAnalyticsResponse? = null,
    val applicationAnalytics: ApplicationAnalyticsResponse? = null,
    val reviewPerformance: ReviewPerformanceResponse? = null,
    val readerDemographics: ReaderDemographicsResponse? = null,
    val recentReviews: List<RecentReviewResponse>
)

@Serializable
data class BookAnalyticsSummaryResponse(
    val totalApplications: Int,
    val approvedApplications: Int,
    val pendingApplications: Int,
    val rejectedApplications: Int,
    val totalReviews: Int,
    val averageRating: Double,
    val positiveFeedback: Int
)

@Serializable
data class ReviewAnalyticsResponse(
    val totalReviews: Int,
    val averageRating: String,
    val positiveFeedback: Int,
    val ratingDistribution: RatingDistributionResponse,
    val ratingBreakdown: List<RatingBreakdownItemResponse>? = null,
    val reviewTypes: ReviewTypesResponse,
    val averageWordCount: Int
)

@Serializable
data class RatingDistributionResponse(
    val `1`: Int = 0, val `2`: Int = 0, val `3`: Int = 0, val `4`: Int = 0, val `5`: Int = 0
)

@Serializable
data class RatingBreakdownItemResponse(
    val rating: Int, val count: Int
)

@Serializable
data class ReviewTypesResponse(
    val text: Int, val link: Int
)

@Serializable
data class ApplicationAnalyticsResponse(
    val totalApplications: Int,
    val approvedApplications: Int,
    val pendingApplications: Int,
    val rejectedApplications: Int,
    val applicationsThisMonth: Int? = null,
    val approvedApplicationsThisMonth: Int? = null,
    val rejectedApplicationsThisMonth: Int? = null,
    val approvalRate: Int,
    val rejectionRate: Int,
    val averageResponseTime: Int? = null,
    val applicationConversionRate: Int? = null
)

@Serializable
data class ReviewPerformanceResponse(
    val reviewSubmissionRate: Int,
    val reviewCompletionRate: Int,
    val averageReviewTime: Double? = null,
    val averageWordCount: Int? = null
)

@Serializable
data class RecentReviewResponse(
    val id: String,
    val rating: Int,
    val reviewType: String,
    val reviewContent: String? = null,
    val wordCount: Int,
    val createdAt: String,
    val application: RecentReviewApplicationResponse
)

@Serializable
data class RecentReviewApplicationResponse(
    val reader: RecentReviewReaderResponse
)

@Serializable
data class RecentReviewReaderResponse(
    val id: String,
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null
)

@Serializable
data class AuthorAnalyticsResponse(
    val authorId: String,
    val overview: AuthorAnalyticsOverviewResponse,
    val performance: AuthorPerformanceResponse,
    val readerAnalytics: ReaderAnalyticsResponse? = null,
    val trends: AuthorTrendsResponse
)

@Serializable
data class AuthorAnalyticsOverviewResponse(
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
data class AuthorPerformanceResponse(
    val booksWithReviews: Int,
    val averageRating: Double,
    val topPerformingBooks: List<TopPerformingBookResponse>
)

@Serializable
data class TopPerformingBookResponse(
    val bookId: String, val title: String, val averageRating: Double, val reviewCount: Int
)

@Serializable
data class AuthorTrendsResponse(
    val monthlyApplications: List<MonthlyDataResponse>,
    val monthlyReviews: List<MonthlyDataResponse>,
    val approvalRateOverTime: List<MonthlyDataResponse>? = null
)

@Serializable
data class MonthlyDataResponse(
    val month: String, val count: Int? = null, val value: Int? = null, val percentage: Int? = null
) {
    // Helper to get the numeric value, preferring count, then value, then percentage
    val numericValue: Int
        get() = count ?: value ?: percentage ?: 0
}

@Serializable
data class ReaderAnalyticsResponse(
    val totalUniqueReaders: Int,
    val newReadersThisMonth: Int,
    val repeatReaders: Int,
    val averageApplicationsPerReader: Double,
    val engagementRate: Int,
    val readersWithReviews: Int,
    val readingStatusBreakdown: Map<String, Int>? = null,
    val topReaders: List<TopReaderResponse>? = null,
    val demographics: ReaderDemographicsResponse? = null
)

@Serializable
data class TopReaderResponse(
    val readerId: String,
    val username: String,
    val totalApplications: Int,
    val completedReviews: Int
)

@Serializable
data class ReaderDemographicsResponse(
    val age: AgeDemographicsResponse? = null,
    val countries: CountryDemographicsResponse? = null,
    val genrePreferences: GenreDemographicsResponse? = null,
    val appliedBookGenres: GenreDemographicsResponse? = null
)

@Serializable
data class AgeDemographicsResponse(
    val averageAge: Int? = null,
    val totalWithAge: Int? = null,
    val ageRanges: List<AgeRangeResponse> = emptyList()
)

@Serializable
data class AgeRangeResponse(
    val range: String, val count: Int, val percentage: Int
)

@Serializable
data class CountryDemographicsResponse(
    val totalWithCountry: Int, val countries: List<CountryDataResponse>
)

@Serializable
data class CountryDataResponse(
    val country: String, val count: Int, val percentage: Int
)

@Serializable
data class GenreDemographicsResponse(
    val totalWithPreferences: Int? = null, val genres: List<GenreDataResponse>
)

@Serializable
data class GenreDataResponse(
    val genre: String, val count: Int, val percentage: Int
)

@Serializable
data class BookPerformanceComparisonResponse(
    val bookId: String,
    val title: String,
    val coverImageUrl: String? = null,
    val status: String,
    val genres: List<BookPerformanceGenreResponse> = emptyList(),
    val applications: BookPerformanceApplicationsResponse,
    val reviews: BookPerformanceReviewsResponse,
    val approvalRate: Int
)

@Serializable
data class BookPerformanceGenreResponse(
    val id: Int, val name: String
)

@Serializable
data class BookPerformanceApplicationsResponse(
    val totalApplications: Int, val approvedApplications: Int
)

@Serializable
data class BookPerformanceReviewsResponse(
    val averageRating: Double, val reviewCount: Int
)

