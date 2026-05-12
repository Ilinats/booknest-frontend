package com.example.booknest.data.service

import com.example.booknest.data.constants.Books
import com.example.booknest.data.constants.PathConstants
import com.example.booknest.data.constants.QueryConstants
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse
import com.example.booknest.domain.model.response.AuthorAnalyticsResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.UploadBookFileResponse
import com.example.booknest.domain.model.response.DownloadBookResponse
import com.example.booknest.domain.model.response.PaginatedResponse
import com.example.booknest.domain.model.response.ReviewResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BooksService {
    @GET(Books.LIST)
    suspend fun browseBooks(
        @Query(QueryConstants.SEARCH) search: String? = null,
        @Query(QueryConstants.GENRES) genres: List<Int>? = null,
        @Query(QueryConstants.TITLE) title: String? = null,
        @Query(QueryConstants.AUTHOR_NAME) authorName: String? = null,
        @Query(QueryConstants.AUTHOR_ID) authorId: String? = null,
        @Query(QueryConstants.SERIES_NAME) seriesName: String? = null,
        @Query(QueryConstants.SERIES_ID) seriesId: String? = null,
        @Query(QueryConstants.AGE_RATING) ageRating: String? = null,
        @Query(QueryConstants.DISTRIBUTION_TYPE) distributionType: String? = null,
        @Query(QueryConstants.PUBLISHED_FROM) publishedFrom: String? = null,
        @Query(QueryConstants.PUBLISHED_TO) publishedTo: String? = null,
        @Query(QueryConstants.CREATED_FROM) createdFrom: String? = null,
        @Query(QueryConstants.CREATED_TO) createdTo: String? = null,
        @Query(QueryConstants.MIN_AVG_RATING) minAvgRating: Double? = null,
        @Query(QueryConstants.MAX_AVG_RATING) maxAvgRating: Double? = null,
        @Query(QueryConstants.SKIP) skip: Int? = null,
        @Query(QueryConstants.TAKE) take: Int? = null,
        @Query(QueryConstants.STATUS) status: String? = null,
        @Query(QueryConstants.APPLICATION_STATUS) applicationStatus: String? = null,
        @Query(QueryConstants.DEADLINE_FILTER) deadlineFilter: String? = null,
        @Query(QueryConstants.SORT_BY) sortBy: String? = null
    ): Response<PaginatedResponse<RecommendedBookResponse>>

    @GET(Books.LIST)
    suspend fun searchBooks(
        @Query(QueryConstants.SEARCH) query: String,
        @Query(QueryConstants.SKIP) skip: Int?,
        @Query(QueryConstants.TAKE) take: Int?
    ): Response<PaginatedResponse<RecommendedBookResponse>>

    @GET(Books.RECOMMENDED)
    suspend fun getRecommendedBooks(
        @Query(QueryConstants.TAKE) take: Int?
    ): Response<PaginatedResponse<RecommendedBookResponse>>

    @GET(Books.BY_ID)
    suspend fun getBookDetails(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<BookResponse>

    @Multipart
    @POST(Books.LIST)
    suspend fun createBook(
        @Part("title") title: RequestBody,
        @Part("shortDescription") shortDescription: RequestBody?,
        @Part("fullDescription") fullDescription: RequestBody?,
        @Part("pageCount") pageCount: RequestBody?,
        @Part("ageRating") ageRating: RequestBody,
        @Part("distributionType") distributionType: RequestBody,
        @Part("totalCopies") totalCopies: RequestBody?,
        @Part("applicationDeadline") applicationDeadline: RequestBody,
        @Part("reviewDeadline") reviewDeadline: RequestBody?,
        @Part("selectionCriteria") selectionCriteria: RequestBody?,
        @Part("selectionMethod") selectionMethod: RequestBody?,
        @Part("genres") genres: RequestBody?,
        @Part("seriesId") seriesId: RequestBody?,
        @Part("seriesOrder") seriesOrder: RequestBody?,
        @Part file: MultipartBody.Part?
    ): Response<BookResponse>

    @GET(Books.MY_BOOKS)
    suspend fun getMyBooks(): Response<List<BookResponse>>

    @PATCH(Books.BY_ID)
    suspend fun updateBook(
        @Path(PathConstants.BOOK_ID) bookId: String,
        @Body book: UpdateBookRequest
    ): Response<BookResponse>

    @DELETE(Books.BY_ID)
    suspend fun deleteBook(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<Unit>

    @POST(Books.PUBLISH)
    suspend fun publishBook(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<BookResponse>

    @GET(Books.STATS)
    suspend fun getBookStats(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<BookStatsResponse>

    @GET(Books.ANALYTICS)
    suspend fun getBookAnalytics(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<DetailedBookAnalyticsResponse>

    @GET(Books.ANALYTICS_DETAILED)
    suspend fun getDetailedBookAnalytics(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<DetailedBookAnalyticsResponse>

    @GET(Books.AUTHOR_ANALYTICS)
    suspend fun getAuthorAnalytics(
        @Query("dateRange") dateRange: String? = null
    ): Response<AuthorAnalyticsResponse>

    @GET(Books.BOOK_PERFORMANCE_COMPARISON)
    suspend fun getBookPerformanceComparison(): Response<List<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse>>

    @Multipart
    @POST(Books.UPLOAD)
    suspend fun uploadBookFile(
        @Path(PathConstants.BOOK_ID) bookId: String,
        @Part file: MultipartBody.Part
    ): Response<UploadBookFileResponse>

    @Multipart
    @POST(Books.COVER)
    suspend fun uploadBookCoverImage(
        @Path(PathConstants.BOOK_ID) bookId: String,
        @Part file: MultipartBody.Part
    ): Response<com.example.booknest.domain.model.response.UploadBookCoverResponse>

    @DELETE(Books.DELETE_COVER)
    suspend fun removeBookCoverImage(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<BookResponse>

    @GET(Books.DOWNLOAD)
    suspend fun getBookDownloadUrl(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<DownloadBookResponse>

    @GET(Books.ALL_REVIEWS)
    suspend fun getBookAllReviews(
        @Path(PathConstants.BOOK_ID) bookId: String,
        @Query("skip") skip: Int? = null,
        @Query("take") take: Int? = null
    ): Response<PaginatedResponse<ReviewResponse>>

    @GET(Books.TRENDING)
    suspend fun getTrendingBooks(
        @Query(QueryConstants.LIMIT) limit: Int? = null
    ): Response<List<com.example.booknest.domain.model.response.TrendingBookResponse>>
}

