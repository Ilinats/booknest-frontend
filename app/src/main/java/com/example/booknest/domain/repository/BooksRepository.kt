package com.example.booknest.domain.repository

import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.AuthorAnalyticsResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse
import com.example.booknest.domain.model.BookDownloadPayload
import com.example.booknest.domain.model.response.BookLeakFingerprintResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.TrendingBookResponse
import com.example.booknest.domain.model.response.UploadBookFileResponse
import okhttp3.MultipartBody

interface BooksRepository {
    suspend fun browseBooks(
        search: String? = null,
        genres: List<Int>? = null,
        title: String? = null,
        authorName: String? = null,
        authorId: String? = null,
        seriesName: String? = null,
        seriesId: String? = null,
        ageRating: String? = null,
        distributionType: String? = null,
        publishedFrom: String? = null,
        publishedTo: String? = null,
        createdFrom: String? = null,
        createdTo: String? = null,
        minAvgRating: Double? = null,
        maxAvgRating: Double? = null,
        page: Int? = null,
        limit: Int? = null,
        status: String? = null,
        applicationStatus: String? = null,
        deadlineFilter: String? = null,
        sortBy: String? = null
    ): Result<List<RecommendedBookResponse>>

    suspend fun searchBooks(query: String, page: Int? = null, limit: Int? = null): Result<List<RecommendedBookResponse>>
    suspend fun getRecommendedBooks(limit: Int? = null, page: Int? = 1): Result<List<RecommendedBookResponse>>
    suspend fun getTrendingBooks(limit: Int? = 10): Result<List<TrendingBookResponse>>
    suspend fun getBookDetails(bookId: String): Result<BookResponse>
    suspend fun createBook(book: CreateBookRequest, filePart: MultipartBody.Part? = null): Result<BookResponse>
    suspend fun getMyBooks(): Result<List<BookResponse>>
    suspend fun updateBook(bookId: String, book: UpdateBookRequest): Result<BookResponse>
    suspend fun deleteBook(bookId: String): Result<Unit>
    suspend fun publishBook(bookId: String): Result<BookResponse>
    suspend fun getBookStats(bookId: String): Result<BookStatsResponse>
    suspend fun getBookAnalytics(bookId: String): Result<DetailedBookAnalyticsResponse>
    suspend fun getDetailedBookAnalytics(bookId: String): Result<DetailedBookAnalyticsResponse>
    suspend fun getBookAllReviews(bookId: String, page: Int? = null, limit: Int? = null): Result<List<ReviewResponse>>
    suspend fun getAuthorAnalytics(dateRange: String? = null): Result<AuthorAnalyticsResponse>
    suspend fun getBookPerformanceComparison(): Result<List<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse>>
    suspend fun uploadBookFile(bookId: String, file: MultipartBody.Part): Result<UploadBookFileResponse>
    suspend fun uploadBookCoverImage(bookId: String, file: MultipartBody.Part): Result<BookResponse>
    suspend fun removeBookCoverImage(bookId: String): Result<BookResponse>
    suspend fun getBookDownload(bookId: String): Result<BookDownloadPayload>
    suspend fun decodeLeakFingerprint(
        bookId: String,
        file: MultipartBody.Part
    ): Result<BookLeakFingerprintResponse>
}
