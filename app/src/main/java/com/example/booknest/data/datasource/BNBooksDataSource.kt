package com.example.booknest.data.datasource

import com.example.booknest.data.service.BooksService
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.AuthorAnalyticsResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse
import com.example.booknest.domain.model.response.DownloadBookResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.UploadBookFileResponse
import com.example.booknest.domain.model.response.UploadBookCoverResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class BNBooksDataSource(private val booksService: BooksService) : BooksDataSource {

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
        return requestPaginatedBody(
            booksService.browseBooks(
                search, genres, title, authorName, authorId,
                seriesName, seriesId, ageRating, distributionType,
                publishedFrom, publishedTo, createdFrom, createdTo,
                minAvgRating, maxAvgRating, skip, take, status,
                applicationStatus, deadlineFilter, sortBy
            )
        )
    }

    override suspend fun getFeaturedBooks(): Result<List<BookResponse>> {
        return requestBody(booksService.getFeaturedBooks())
    }

    override suspend fun searchBooks(
        query: String,
        skip: Int?,
        take: Int?
    ): Result<List<RecommendedBookResponse>> {
        return requestPaginatedBody(booksService.searchBooks(query, skip, take))
    }

    override suspend fun getRecommendedBooks(take: Int?): Result<List<RecommendedBookResponse>> {
        return requestPaginatedBody(booksService.getRecommendedBooks(take))
    }

    override suspend fun getBookDetails(bookId: String): Result<BookResponse> {
        return requestBody(booksService.getBookDetails(bookId))
    }

    override suspend fun createBook(
        book: CreateBookRequest,
        filePart: MultipartBody.Part?
    ): Result<BookResponse> {
        val title = book.title.toRequestBody("text/plain".toMediaType())
        val shortDescription = book.shortDescription?.toRequestBody("text/plain".toMediaType())
        val fullDescription = book.fullDescription?.toRequestBody("text/plain".toMediaType())
        val pageCount = book.pageCount?.toString()?.toRequestBody("text/plain".toMediaType())
        val ageRating = book.ageRating.toRequestBody("text/plain".toMediaType())
        val distributionType = book.distributionType.toRequestBody("text/plain".toMediaType())
        val totalCopies = book.totalCopies.toString().toRequestBody("text/plain".toMediaType())
        val applicationDeadline = book.applicationDeadline.toRequestBody("text/plain".toMediaType())
        val reviewDeadline = book.reviewDeadline?.toRequestBody("text/plain".toMediaType())
        val selectionCriteria = book.selectionCriteria?.toRequestBody("text/plain".toMediaType())
        val selectionMethod = book.selectionMethod.toRequestBody("text/plain".toMediaType())
        val genres = book.genreIds?.let { ids ->
            val jsonArray = ids.joinToString(",", "[", "]")
            jsonArray.toRequestBody("text/plain".toMediaType())
        }
        val seriesId = book.seriesId?.toRequestBody("text/plain".toMediaType())
        val seriesOrder = book.seriesOrder?.toString()?.toRequestBody("text/plain".toMediaType())

        return requestBody(
            booksService.createBook(
                title = title,
                shortDescription = shortDescription,
                fullDescription = fullDescription,
                pageCount = pageCount,
                ageRating = ageRating,
                distributionType = distributionType,
                totalCopies = totalCopies,
                applicationDeadline = applicationDeadline,
                reviewDeadline = reviewDeadline,
                selectionCriteria = selectionCriteria,
                selectionMethod = selectionMethod,
                genres = genres,
                seriesId = seriesId,
                seriesOrder = seriesOrder,
                file = filePart
            )
        )
    }

    override suspend fun getMyBooks(): Result<List<BookResponse>> {
        return requestBody(booksService.getMyBooks())
    }

    override suspend fun updateBook(bookId: String, book: UpdateBookRequest): Result<BookResponse> {
        return requestBody(booksService.updateBook(bookId, book))
    }

    override suspend fun deleteBook(bookId: String): Result<Unit> {
        return requestBodyUnit(booksService.deleteBook(bookId))
    }

    override suspend fun publishBook(bookId: String): Result<BookResponse> {
        return requestBody(booksService.publishBook(bookId))
    }

    override suspend fun getBookStats(bookId: String): Result<BookStatsResponse> {
        return requestBody(booksService.getBookStats(bookId))
    }

    override suspend fun getBookAnalytics(bookId: String): Result<DetailedBookAnalyticsResponse> {
        return requestBody(booksService.getBookAnalytics(bookId))
    }

    override suspend fun getDetailedBookAnalytics(bookId: String): Result<DetailedBookAnalyticsResponse> {
        return requestBody(booksService.getDetailedBookAnalytics(bookId))
    }

    override suspend fun getAuthorAnalytics(dateRange: String?): Result<AuthorAnalyticsResponse> {
        return requestBody(booksService.getAuthorAnalytics(dateRange))
    }

    override suspend fun getBookPerformanceComparison(): Result<List<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse>> {
        return requestBody(booksService.getBookPerformanceComparison())
    }

    override suspend fun uploadBookFile(
        bookId: String,
        file: MultipartBody.Part
    ): Result<UploadBookFileResponse> {
        return requestBody(booksService.uploadBookFile(bookId, file))
    }

    override suspend fun uploadBookCoverImage(
        bookId: String,
        file: MultipartBody.Part
    ): Result<BookResponse> {
        return requestBody(booksService.uploadBookCoverImage(bookId, file)).fold(
            onSuccess = { response -> Result.success(response.book) },
            onFailure = { error -> Result.failure(error) }
        )
    }

    override suspend fun removeBookCoverImage(bookId: String): Result<BookResponse> {
        return requestBody(booksService.removeBookCoverImage(bookId))
    }

    override suspend fun getBookDownloadUrl(bookId: String): Result<DownloadBookResponse> {
        return requestBody(booksService.getBookDownloadUrl(bookId))
    }

    override suspend fun getBookAllReviews(
        bookId: String,
        skip: Int?,
        take: Int?
    ): Result<List<ReviewResponse>> {
        return requestPaginatedBody(booksService.getBookAllReviews(bookId, skip, take))
    }
}

