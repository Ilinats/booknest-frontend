package com.example.booknest.data.repository

import com.example.booknest.data.datasource.BooksDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.AuthorAnalyticsResponse
import com.example.booknest.domain.model.response.BookLeakFingerprintResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse
import com.example.booknest.domain.model.BookDownloadPayload
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.TrendingBookResponse
import com.example.booknest.domain.model.response.UploadBookFileResponse
import com.example.booknest.domain.repository.BooksRepository
import okhttp3.MultipartBody

class BNBooksRepository(private val booksDataSource: BooksDataSource) : BooksRepository {
    override suspend fun browseBooks(
        search: String?,
        genres: List<Int>?,
        title: String?,
        authorName: String?,
        authorId: String?,
        seriesName: String?,
        seriesId: String?,
        ageRating: String?,
        distributionType: String?,
        publishedFrom: String?,
        publishedTo: String?,
        createdFrom: String?,
        createdTo: String?,
        minAvgRating: Double?,
        maxAvgRating: Double?,
        skip: Int?,
        take: Int?,
        status: String?,
        applicationStatus: String?,
        deadlineFilter: String?,
        sortBy: String?
    ): Result<List<RecommendedBookResponse>> {
        return resultBody(
            booksDataSource.browseBooks(
                search, genres, title, authorName, authorId,
                seriesName, seriesId, ageRating, distributionType,
                publishedFrom, publishedTo, createdFrom, createdTo,
                minAvgRating, maxAvgRating, skip, take, status,
                applicationStatus, deadlineFilter, sortBy
            )
        )
    }
    override suspend fun searchBooks(
        query: String,
        skip: Int?,
        take: Int?
    ): Result<List<RecommendedBookResponse>> {
        return resultBody(booksDataSource.searchBooks(query, skip, take))
    }

    override suspend fun getRecommendedBooks(take: Int?): Result<List<RecommendedBookResponse>> {
        return resultBody(booksDataSource.getRecommendedBooks(take))
    }

    override suspend fun getTrendingBooks(limit: Int?): Result<List<TrendingBookResponse>> {
        return booksDataSource.getTrendingBooks(limit)
    }

    override suspend fun getBookDetails(bookId: String): Result<BookResponse> {
        return resultBody(booksDataSource.getBookDetails(bookId))
    }

    override suspend fun createBook(
        book: CreateBookRequest,
        filePart: MultipartBody.Part?
    ): Result<BookResponse> {
        return resultBody(booksDataSource.createBook(book, filePart))
    }

    override suspend fun getMyBooks(): Result<List<BookResponse>> {
        return resultBody(booksDataSource.getMyBooks())
    }

    override suspend fun updateBook(bookId: String, book: UpdateBookRequest): Result<BookResponse> {
        return resultBody(booksDataSource.updateBook(bookId, book))
    }

    override suspend fun deleteBook(bookId: String): Result<Unit> {
        return resultBody(booksDataSource.deleteBook(bookId))
    }

    override suspend fun publishBook(bookId: String): Result<BookResponse> {
        return resultBody(booksDataSource.publishBook(bookId))
    }

    override suspend fun getBookStats(bookId: String): Result<BookStatsResponse> {
        return resultBody(booksDataSource.getBookStats(bookId))
    }

    override suspend fun getBookAnalytics(bookId: String): Result<DetailedBookAnalyticsResponse> {
        return resultBody(booksDataSource.getBookAnalytics(bookId))
    }

    override suspend fun getDetailedBookAnalytics(bookId: String): Result<DetailedBookAnalyticsResponse> {
        return resultBody(booksDataSource.getDetailedBookAnalytics(bookId))
    }

    override suspend fun getAuthorAnalytics(dateRange: String?): Result<AuthorAnalyticsResponse> {
        return resultBody(booksDataSource.getAuthorAnalytics(dateRange))
    }

    override suspend fun getBookPerformanceComparison(): Result<List<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse>> {
        return resultBody(booksDataSource.getBookPerformanceComparison())
    }

    override suspend fun uploadBookFile(
        bookId: String,
        file: MultipartBody.Part
    ): Result<UploadBookFileResponse> {
        return resultBody(booksDataSource.uploadBookFile(bookId, file))
    }

    override suspend fun uploadBookCoverImage(
        bookId: String,
        file: MultipartBody.Part
    ): Result<BookResponse> {
        return resultBody(booksDataSource.uploadBookCoverImage(bookId, file))
    }

    override suspend fun removeBookCoverImage(bookId: String): Result<BookResponse> {
        return resultBody(booksDataSource.removeBookCoverImage(bookId))
    }

    override suspend fun getBookDownload(bookId: String): Result<BookDownloadPayload> {
        return resultBody(booksDataSource.getBookDownload(bookId))
    }

    override suspend fun decodeLeakFingerprint(
        bookId: String,
        file: MultipartBody.Part
    ): Result<BookLeakFingerprintResponse> {
        return resultBody(booksDataSource.decodeLeakFingerprint(bookId, file))
    }

    override suspend fun getBookAllReviews(
        bookId: String,
        skip: Int?,
        take: Int?
    ): Result<List<ReviewResponse>> {
        return resultBody(booksDataSource.getBookAllReviews(bookId, skip, take))
    }
}

