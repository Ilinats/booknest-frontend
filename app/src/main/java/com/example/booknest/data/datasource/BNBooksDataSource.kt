package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.BooksService
import com.example.booknest.domain.model.BookDownloadPayload
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.AuthorAnalyticsResponse
import com.example.booknest.domain.model.response.BookLeakFingerprintResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse
import com.example.booknest.domain.model.response.DownloadBookResponse
import com.example.booknest.domain.model.response.PaginatedResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.TrendingBookResponse
import com.example.booknest.domain.model.response.UploadBookFileResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class BNBooksDataSource(private val booksService: BooksService) : BooksDataSource {

    private val downloadJson = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

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
        page: Int?,
        limit: Int?,
        status: String?,
        applicationStatus: String?,
        deadlineFilter: String?,
        sortBy: String?
    ): Result<List<RecommendedBookResponse>> {
        val api = BrowseBooksQueryMapper.toApiQuery(
            search = search,
            genres = genres,
            title = title,
            authorName = authorName,
            authorId = authorId,
            seriesName = seriesName,
            seriesId = seriesId,
            ageRating = ageRating,
            distributionType = distributionType,
            publishedFrom = publishedFrom,
            publishedTo = publishedTo,
            minAvgRating = minAvgRating,
            maxAvgRating = maxAvgRating,
            page = page,
            limit = limit,
            status = status,
            applicationStatus = applicationStatus,
            deadlineFilter = deadlineFilter,
            sortBy = sortBy,
        )
        return runSuspendRequestPaginated { booksService.browseBooksFromQuery(api) }
    }

    override suspend fun searchBooks(
        query: String,
        page: Int?,
        limit: Int?
    ): Result<List<RecommendedBookResponse>> {
        val api = BrowseBooksQueryMapper.toApiQuery(
            search = query,
            page = page,
            limit = limit,
        )
        return runSuspendRequestPaginated { booksService.browseBooksFromQuery(api) }
    }

    override suspend fun getRecommendedBooks(
        limit: Int?,
        page: Int?,
    ): Result<List<RecommendedBookResponse>> {
        return runSuspendRequestPaginated {
            booksService.getRecommendedBooks(page = page, limit = limit)
        }
    }

    override suspend fun getTrendingBooks(limit: Int?): Result<List<TrendingBookResponse>> {
        return runSuspendRequest { booksService.getTrendingBooks(limit) }
    }

    override suspend fun getBookDetails(bookId: String): Result<BookResponse> {
        return runSuspendRequest { booksService.getBookDetails(bookId) }
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

        return runSuspendRequest {
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
        }
    }

    override suspend fun getMyBooks(): Result<List<BookResponse>> {
        return runSuspendRequestPaginated { booksService.getMyBooks() }
    }

    override suspend fun updateBook(bookId: String, book: UpdateBookRequest): Result<BookResponse> {
        return runSuspendRequest { booksService.updateBook(bookId, book) }
    }

    override suspend fun deleteBook(bookId: String): Result<Unit> {
        return runSuspendRequestUnit { booksService.deleteBook(bookId) }
    }

    override suspend fun publishBook(bookId: String): Result<BookResponse> {
        return runSuspendRequest { booksService.publishBook(bookId) }
    }

    override suspend fun getBookStats(bookId: String): Result<BookStatsResponse> {
        return runSuspendRequest { booksService.getBookStats(bookId) }
    }

    override suspend fun getBookAnalytics(bookId: String): Result<DetailedBookAnalyticsResponse> {
        return runSuspendRequest { booksService.getBookAnalytics(bookId) }
    }

    override suspend fun getDetailedBookAnalytics(bookId: String): Result<DetailedBookAnalyticsResponse> {
        return runSuspendRequest { booksService.getDetailedBookAnalytics(bookId) }
    }

    override suspend fun getAuthorAnalytics(dateRange: String?): Result<AuthorAnalyticsResponse> {
        return runSuspendRequest { booksService.getAuthorAnalytics(dateRange) }
    }

    override suspend fun getBookPerformanceComparison(): Result<List<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse>> {
        return runSuspendRequest { booksService.getBookPerformanceComparison() }
    }

    override suspend fun uploadBookFile(
        bookId: String,
        file: MultipartBody.Part
    ): Result<UploadBookFileResponse> {
        return runSuspendRequest { booksService.uploadBookFile(bookId, file) }
    }

    override suspend fun uploadBookCoverImage(
        bookId: String,
        file: MultipartBody.Part
    ): Result<BookResponse> {
        return runSuspendRequest { booksService.uploadBookCoverImage(bookId, file) }.fold(
            onSuccess = { response ->
                val coverUrl = response.book.coverImageUrl?.takeIf { it.isNotBlank() }
                    ?: response.coverImage.url
                Result.success(response.book.copy(coverImageUrl = coverUrl))
            },
            onFailure = { error -> Result.failure(error) }
        )
    }

    override suspend fun removeBookCoverImage(bookId: String): Result<BookResponse> {
        return runSuspendRequest { booksService.removeBookCoverImage(bookId) }
    }

    override suspend fun getBookDownload(bookId: String): Result<BookDownloadPayload> =
        withContext(Dispatchers.IO) {
            try {
                val response = booksService.downloadBook(bookId)
                if (!response.isSuccessful) {
                    val errorMessage = response.errorBody()?.use { extractErrorMessage(it.string()) }
                        ?: "Request failed"
                    return@withContext Result.failure(
                        BNError.Generic(
                            messageString = errorMessage,
                            error = null,
                            statusCode = response.code()
                        )
                    )
                }
                val body = response.body()
                    ?: return@withContext Result.failure(Throwable("Empty response body"))
                val rawType = response.headers()["Content-Type"]
                    ?: body.contentType()?.toString()
                    ?: ""
                val contentType = rawType.lowercase()

                when {
                    contentType.contains("application/json") -> {
                        val text = body.string()
                        val data = downloadJson.decodeFromString<DownloadBookResponse>(text)
                        Result.success(BookDownloadPayload.PresignedUrl(data))
                    }

                    contentType.contains("application/pdf") -> {
                        val (displayName, ext) = parseDownloadFileName(
                            response.headers()["Content-Disposition"],
                            defaultExtension = "pdf"
                        )
                        Result.success(
                            BookDownloadPayload.DirectStream(
                                body = body,
                                displayFileName = displayName,
                                extension = ext
                            )
                        )
                    }

                    contentType.contains("application/epub") ||
                        contentType.contains("epub+zip") -> {
                        val (displayName, ext) = parseDownloadFileName(
                            response.headers()["Content-Disposition"],
                            defaultExtension = "epub"
                        )
                        Result.success(
                            BookDownloadPayload.DirectStream(
                                body = body,
                                displayFileName = displayName,
                                extension = ext
                            )
                        )
                    }

                    else -> {
                        body.close()
                        Result.failure(
                            BNError.Generic(
                                messageString = "Unexpected download format",
                                error = null,
                                statusCode = response.code()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Result.failure(mapNetworkOrUnknown(e))
            }
        }

    private fun parseDownloadFileName(
        contentDisposition: String?,
        defaultExtension: String
    ): Pair<String, String> {
        if (contentDisposition.isNullOrBlank()) {
            return "book.$defaultExtension" to defaultExtension
        }
        val utf8Name = Regex(
            "filename\\*\\s*=\\s*UTF-8''([^;\\s]+)",
            RegexOption.IGNORE_CASE
        ).find(contentDisposition)?.groupValues?.getOrNull(1)?.let { encoded ->
            try {
                java.net.URLDecoder.decode(encoded, Charsets.UTF_8.name())
            } catch (_: Exception) {
                null
            }
        }
        val quotedName = Regex(
            "filename\\s*=\\s*\"([^\"]+)\"",
            RegexOption.IGNORE_CASE
        ).find(contentDisposition)?.groupValues?.getOrNull(1)?.trim()
        val unquotedName = Regex(
            "filename\\s*=\\s*([^;\\s]+)",
            RegexOption.IGNORE_CASE
        ).find(contentDisposition)?.groupValues?.getOrNull(1)?.trim()
        val raw = (utf8Name ?: quotedName ?: unquotedName)?.trim('"')?.ifBlank { null }
            ?: "book.$defaultExtension"
        val ext = raw.substringAfterLast('.', "").lowercase().takeIf { it.isNotBlank() }
            ?: defaultExtension
        val base = if (raw.contains('.')) raw else "$raw.$defaultExtension"
        return base to ext
    }

    override suspend fun decodeLeakFingerprint(
        bookId: String,
        file: MultipartBody.Part
    ): Result<BookLeakFingerprintResponse> {
        return runSuspendRequest { booksService.decodeLeakFingerprint(bookId, file) }
    }

    override suspend fun getBookAllReviews(
        bookId: String,
        page: Int?,
        limit: Int?
    ): Result<List<ReviewResponse>> {
        val resolvedLimit = limit ?: 20
        val resolvedPage = page ?: 1
        val skip = (resolvedPage - 1).coerceAtLeast(0) * resolvedLimit
        return runSuspendRequestPaginated {
            booksService.getBookAllReviews(bookId, skip = skip, take = resolvedLimit)
        }
    }
}
