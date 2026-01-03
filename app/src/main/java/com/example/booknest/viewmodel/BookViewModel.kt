package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.TrendingBookResponse
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.books.GetBookDetailsUseCase
import com.example.booknest.domain.usecase.books.GetNewReleasesUseCase
import com.example.booknest.domain.usecase.books.GetRecommendedBooksUseCase
import com.example.booknest.domain.usecase.books.SearchBooksUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class BookViewModel(
    private val getRecommendedBooksUseCase: GetRecommendedBooksUseCase,
    private val getNewReleasesUseCase: GetNewReleasesUseCase,
    private val browseBooksUseCase: BrowseBooksUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val getBookDetailsUseCase: GetBookDetailsUseCase
) : ViewModel() {

    private val _books = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val books: StateFlow<List<RecommendedBookResponse>> = _books

    private val _featuredBooks = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val featuredBooks: StateFlow<List<RecommendedBookResponse>> = _featuredBooks

    private val _bookDetails = MutableStateFlow<BookResponse?>(null)
    val bookDetails: StateFlow<BookResponse?> = _bookDetails

    private val _recommendedBooks = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val recommendedBooks: StateFlow<List<RecommendedBookResponse>> = _recommendedBooks

    private val _newReleases = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val newReleases: StateFlow<List<RecommendedBookResponse>> = _newReleases

    private val _trendingBooks = MutableStateFlow<List<TrendingBookResponse>>(emptyList())
    val trendingBooks: StateFlow<List<TrendingBookResponse>> = _trendingBooks

    private val _homeSearchResults = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val homeSearchResults: StateFlow<List<RecommendedBookResponse>> = _homeSearchResults

    private val _searchQuery = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        searchBooks(query)
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
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
                        println("Recommended books loaded: ${books.size} books")
                    }
                    .onFailure { e ->
                        println("Recommended books error: ${e.message}")
                    }
            } catch (e: Exception) {
                println("Recommended books exception: ${e.message}")
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
                        println("New releases loaded: ${books.size} books")
                    }
                    .onFailure { e ->
                        println("New releases error: ${e.message}")
                    }
            } catch (e: Exception) {
                println("New releases exception: ${e.message}")
                e.printStackTrace()
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
        status: String? = "active",
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
                    .onFailure { e ->
                        println("Browse books error: ${e.message}")
                        _lastFetchCount.value = 0
                    }
            } catch (e: Exception) {
                println("Browse books exception: ${e.message}")
                _lastFetchCount.value = 0
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _lastFetchCount = MutableStateFlow(0)
    val lastFetchCount: StateFlow<Int> = _lastFetchCount

    fun getFeaturedBooks() {
        viewModelScope.launch {
            try {
                val result = browseBooksUseCase(
                    query = null,
                    genres = null,
                    title = null,
                    authorName = null,
                    authorId = null,
                    seriesName = null,
                    seriesId = null,
                    ageRating = null,
                    distributionType = null,
                    publishedFrom = null,
                    publishedTo = null,
                    createdFrom = null,
                    createdTo = null,
                    minAvgRating = null,
                    maxAvgRating = null,
                    skip = null,
                    take = 10,
                    status = "active"
                )
                result
                    .onSuccess { books ->
                        _featuredBooks.value = books
                        println("Featured books loaded: ${books.size} books")
                    }
                    .onFailure { e ->
                        println("Featured books error: ${e.message}")
                    }
            } catch (e: Exception) {
                println("Featured books exception: ${e.message}")
            }
        }
    }

    private fun searchBooks(query: String, skip: Int? = null, take: Int? = null) {
        viewModelScope.launch {
            try {
                val result = searchBooksUseCase(query, skip, take)
                result
                    .onSuccess { books ->
                        _books.value = books
                        println("Search books loaded: ${books.size} books for query: $query")
                    }
                    .onFailure { e ->
                        println("Search books error: ${e.message}")
                    }
            } catch (e: Exception) {
                println("Search books exception: ${e.message}")
                e.printStackTrace()
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
                    .onFailure { e ->
                        println("Book details error: ${e.message}")
                    }
            } catch (e: Exception) {
            }
        }
    }

    fun clearBookDetails() {
        _bookDetails.value = null
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
                    .onFailure { e ->
                        println("Home search error: ${e.message}")
                    }
            } catch (e: Exception) {
                println("Home search exception: ${e.message}")
            }
        }
    }

    fun clearHomeSearchResults() {
        _homeSearchResults.value = emptyList()
    }

    fun getTrendingBooks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val booksService = org.koin.core.context.GlobalContext.get()
                    .get<com.example.booknest.data.service.BooksService>()
                val response = booksService.getTrendingBooks(10)
                if (response.isSuccessful && response.body() != null) {
                    _trendingBooks.value = response.body()!!
                }
            } catch (e: Exception) {
                println("Trending books error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
