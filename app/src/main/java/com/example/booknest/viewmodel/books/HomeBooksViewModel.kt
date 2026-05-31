package com.example.booknest.viewmodel.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.TrendingBookResponse
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.books.GetNewReleasesUseCase
import com.example.booknest.domain.usecase.books.GetRecommendedBooksUseCase
import com.example.booknest.domain.usecase.books.GetTrendingBooksUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HomeBooksViewModel(
    private val getRecommendedBooksUseCase: GetRecommendedBooksUseCase,
    private val getNewReleasesUseCase: GetNewReleasesUseCase,
    private val getTrendingBooksUseCase: GetTrendingBooksUseCase,
    private val browseBooksUseCase: BrowseBooksUseCase,
    private val bookCatalogCache: BookCatalogCache,
) : ViewModel() {

    private val _recommendedBooks = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val recommendedBooks: StateFlow<List<RecommendedBookResponse>> = _recommendedBooks.asStateFlow()

    private val _newReleases = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val newReleases: StateFlow<List<RecommendedBookResponse>> = _newReleases.asStateFlow()

    private val _trendingBooks = MutableStateFlow<List<TrendingBookResponse>>(emptyList())
    val trendingBooks: StateFlow<List<TrendingBookResponse>> = _trendingBooks.asStateFlow()

    private val _homeSearchResults = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val homeSearchResults: StateFlow<List<RecommendedBookResponse>> = _homeSearchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _recommendedLoading = MutableStateFlow(false)
    val recommendedLoading: StateFlow<Boolean> = _recommendedLoading.asStateFlow()

    private val _newReleasesLoading = MutableStateFlow(false)
    val newReleasesLoading: StateFlow<Boolean> = _newReleasesLoading.asStateFlow()

    private val _trendingLoading = MutableStateFlow(false)
    val trendingLoading: StateFlow<Boolean> = _trendingLoading.asStateFlow()

    private val _homeSearchLoading = MutableStateFlow(false)
    val homeSearchLoading: StateFlow<Boolean> = _homeSearchLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        searchForHomeScreen(query = query, limit = 20)
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
                _recommendedLoading.value = true
                getRecommendedBooksUseCase(10)
                    .onSuccess { books ->
                        _recommendedBooks.value = books
                        bookCatalogCache.register(books)
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load recommendations"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load recommendations"
            } finally {
                _recommendedLoading.value = false
            }
        }
    }

    fun getNewReleases() {
        viewModelScope.launch {
            try {
                _newReleasesLoading.value = true
                getNewReleasesUseCase(daysBack = 30, limit = 10)
                    .onSuccess { books ->
                        _newReleases.value = books
                        bookCatalogCache.register(books)
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load new releases"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load new releases"
            } finally {
                _newReleasesLoading.value = false
            }
        }
    }

    fun getTrendingBooks() {
        viewModelScope.launch {
            try {
                _trendingLoading.value = true
                getTrendingBooksUseCase(10)
                    .onSuccess { trending ->
                        _trendingBooks.value = trending
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load trending books"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading trending books"
            } finally {
                _trendingLoading.value = false
            }
        }
    }

    fun searchForHomeScreen(query: String, limit: Int = 20) {
        viewModelScope.launch {
            try {
                _homeSearchLoading.value = true
                browseBooksUseCase(
                    query = query,
                    page = 1,
                    limit = limit,
                    status = "active",
                )
                    .onSuccess { books ->
                        _homeSearchResults.value = books
                        bookCatalogCache.register(books)
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Search failed"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Search failed"
            } finally {
                _homeSearchLoading.value = false
            }
        }
    }

    fun clearHomeSearchResults() {
        _homeSearchResults.value = emptyList()
    }
}
