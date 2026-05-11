package com.example.booknest.viewmodel.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.TrendingBookResponse
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.books.GetBookDetailsUseCase
import com.example.booknest.domain.usecase.books.GetNewReleasesUseCase
import com.example.booknest.domain.usecase.books.GetRecommendedBooksUseCase
import com.example.booknest.domain.usecase.books.GetTrendingBooksUseCase
import com.example.booknest.domain.usecase.books.SearchBooksUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class BookViewModel(
    private val getRecommendedBooksUseCase: GetRecommendedBooksUseCase,
    private val getNewReleasesUseCase: GetNewReleasesUseCase,
    private val browseBooksUseCase: BrowseBooksUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val getBookDetailsUseCase: GetBookDetailsUseCase,
    private val getTrendingBooksUseCase: GetTrendingBooksUseCase
) : ViewModel() {

    private val _books = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val books: StateFlow<List<RecommendedBookResponse>> = _books.asStateFlow()

    private val _bookDetails = MutableStateFlow<BookResponse?>(null)
    val bookDetails: StateFlow<BookResponse?> = _bookDetails.asStateFlow()

    private val _recommendedBooks = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val recommendedBooks: StateFlow<List<RecommendedBookResponse>> = _recommendedBooks.asStateFlow()

    private val _newReleases = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val newReleases: StateFlow<List<RecommendedBookResponse>> = _newReleases.asStateFlow()

    private val _trendingBooks = MutableStateFlow<List<TrendingBookResponse>>(emptyList())
    val trendingBooks: StateFlow<List<TrendingBookResponse>> = _trendingBooks.asStateFlow()

    private val _homeSearchResults = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val homeSearchResults: StateFlow<List<RecommendedBookResponse>> = _homeSearchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        searchForHomeScreen(query = query, take = 20)
                    } else {
                        clearHomeSearchResults()
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getRecommendedBooks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = getRecommendedBooksUseCase(10)
                result
                    .onSuccess { books ->
                        _recommendedBooks.value = books
                    }
                    .onFailure { _ -> }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getNewReleases() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = getNewReleasesUseCase(daysBack = 30, take = 10)
                result
                    .onSuccess { books ->
                        _newReleases.value = books
                    }
                    .onFailure { _ -> }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun browseBooks(
        query: String? = null,
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
        skip: Int? = null,
        take: Int? = null,
        status: String? = null,
        applicationStatus: String? = null,
        deadlineFilter: String? = null,
        sortBy: String? = null,
        append: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = browseBooksUseCase(
                    query = query,
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
                    createdFrom = createdFrom,
                    createdTo = createdTo,
                    minAvgRating = minAvgRating,
                    maxAvgRating = maxAvgRating,
                    skip = skip,
                    take = take,
                    status = status,
                    applicationStatus = applicationStatus,
                    deadlineFilter = deadlineFilter,
                    sortBy = sortBy
                )
                result
                    .onSuccess { books ->
                        if (append && skip != null && skip > 0) {
                            _books.value = _books.value + books
                        } else {
                            _books.value = books
                        }
                        _lastFetchCount.value = books.size
                    }
                    .onFailure { _ ->
                        _lastFetchCount.value = 0
                    }
            } catch (e: Exception) {
                _lastFetchCount.value = 0
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _lastFetchCount = MutableStateFlow(0)
    val lastFetchCount: StateFlow<Int> = _lastFetchCount

    private val _authorBooks = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val authorBooks: StateFlow<List<RecommendedBookResponse>> = _authorBooks.asStateFlow()

    private val _authorBooksLoading = MutableStateFlow(false)
    val authorBooksLoading: StateFlow<Boolean> = _authorBooksLoading.asStateFlow()

    private fun searchBooks(query: String, skip: Int? = null, take: Int? = null) {
        viewModelScope.launch {
            try {
                val result = searchBooksUseCase(query, skip, take)
                result
                    .onSuccess { books ->
                        _books.value = books
                    }
                    .onFailure { _ -> }
            } catch (e: Exception) {
            }
        }
    }

    fun getBookDetails(bookId: String) {
        viewModelScope.launch {
            try {
                val result = getBookDetailsUseCase(bookId)
                result
                    .onSuccess { book ->
                        _bookDetails.value = book
                    }
                    .onFailure { _ -> }
            } catch (e: Exception) {
            }
        }
    }

    fun searchForHomeScreen(query: String, take: Int = 20) {
        viewModelScope.launch {
            try {
                val result = browseBooksUseCase(
                    query = query,
                    take = take,
                    status = "active"
                )
                result
                    .onSuccess { books ->
                        _homeSearchResults.value = books
                    }
                    .onFailure { _ -> }
            } catch (e: Exception) {
            }
        }
    }

    fun clearHomeSearchResults() {
        _homeSearchResults.value = emptyList()
    }

    fun findBookInCache(bookId: String): BookResponse? {
        val allBooks = _books.value +
                _recommendedBooks.value +
                _newReleases.value +
                _homeSearchResults.value

        val foundBook = allBooks.find { it.id == bookId }
        return foundBook?.let { cachedBook ->
            BookResponse(
                id = cachedBook.id,
                title = cachedBook.title,
                authorName = cachedBook.resolvedAuthorName,
                coverImageUrl = cachedBook.coverImageUrl,
                rating = cachedBook.rating,
                seriesName = cachedBook.seriesName,
                seriesOrder = cachedBook.seriesOrder,
                publishedAt = cachedBook.publishedAt,
                applicationDeadline = cachedBook.applicationDeadline,
                availableCopies = cachedBook.availableCopies,
                totalCopies = cachedBook.totalCopies,
                genres = cachedBook.genres,
                distributionType = cachedBook.distributionType,
                author = cachedBook.author,
                authorId = cachedBook.author?.id,
                fullDescription = null,
                shortDescription = null,
                pageCount = null,
                ageRating = null,
                seriesId = null,
                series = null
            )
        }
    }

    fun calculateRating(bookRating: Double?, reviews: List<ReviewResponse>): Double {
        return if (bookRating == null || bookRating == 0.0) {
            if (reviews.isNotEmpty()) {
                reviews.map { it.rating.toDouble() }.average()
            } else {
                0.0
            }
        } else {
            bookRating
        }
    }

    fun loadAuthorBooks(authorId: String, authorName: String?) {
        viewModelScope.launch {
            try {
                _authorBooksLoading.value = true
                val result = browseBooksUseCase(
                    authorName = authorName,
                    authorId = authorId,
                    take = 100,
                    status = null
                )
                result
                    .onSuccess { books -> _authorBooks.value = books }
                    .onFailure { e -> _error.value = e.message ?: "Failed to load author books" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _authorBooksLoading.value = false
            }
        }
    }

    fun getTrendingBooks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = getTrendingBooksUseCase(10)
                result.onSuccess { trendingBooks ->
                    _trendingBooks.value = trendingBooks
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load trending books"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading trending books"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
