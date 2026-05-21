package com.example.booknest.viewmodel.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SearchHistoryManager
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BrowseBooksViewModel(
    private val browseBooksUseCase: BrowseBooksUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val searchHistoryManager: SearchHistoryManager,
    private val bookCatalogCache: BookCatalogCache,
) : ViewModel() {

    private val _books = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val books: StateFlow<List<RecommendedBookResponse>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    private val _browseFilterGenres = MutableStateFlow<List<GenreResponse>>(emptyList())
    val browseFilterGenres: StateFlow<List<GenreResponse>> = _browseFilterGenres.asStateFlow()

    private val _browseGenresLoading = MutableStateFlow(false)
    val browseGenresLoading: StateFlow<Boolean> = _browseGenresLoading.asStateFlow()

    private val _seriesBooksBySeriesId = MutableStateFlow<Map<String, List<RecommendedBookResponse>>>(emptyMap())
    val seriesBooksBySeriesId: StateFlow<Map<String, List<RecommendedBookResponse>>> =
        _seriesBooksBySeriesId.asStateFlow()

    private val _seriesBooksLoadingIds = MutableStateFlow<Set<String>>(emptySet())
    val seriesBooksLoadingIds: StateFlow<Set<String>> = _seriesBooksLoadingIds.asStateFlow()

    private val _seriesBooksLoadError = MutableStateFlow<Map<String, String>>(emptyMap())
    val seriesBooksLoadError: StateFlow<Map<String, String>> = _seriesBooksLoadError.asStateFlow()

    private val seriesBooksClaimMutex = Mutex()

    private val _browseListFiltersReady = MutableStateFlow(false)
    val browseListFiltersReady: StateFlow<Boolean> = _browseListFiltersReady.asStateFlow()

    private val _browseListHasMore = MutableStateFlow(false)
    val browseListHasMore: StateFlow<Boolean> = _browseListHasMore.asStateFlow()

    private val _browseListPage = MutableStateFlow(1)

    private val _browseListPageSize = MutableStateFlow(20)
    val browseListPageSize: StateFlow<Int> = _browseListPageSize.asStateFlow()

    private val _lastFetchCount = MutableStateFlow(0)
    val lastFetchCount: StateFlow<Int> = _lastFetchCount

    private val _bookListBrowseUi = MutableStateFlow(BookListBrowseUiState())
    val bookListBrowseUi: StateFlow<BookListBrowseUiState> = _bookListBrowseUi.asStateFlow()

    private val _bookListCategory = MutableStateFlow<String?>(null)
    val bookListCategory: StateFlow<String?> = _bookListCategory.asStateFlow()

    private var bookListScreenArgs: Pair<String?, String?>? = null
    private var lastAppliedBrowseFilters: BrowseFilterSnapshot? = null
    private var bookListSearchDebounceJob: Job? = null

    private val _browseListLoadingMore = MutableStateFlow(false)
    val browseListLoadingMore: StateFlow<Boolean> = _browseListLoadingMore.asStateFlow()

    private fun applyBrowseFilterSnapshotFromCurrentUi() {
        lastAppliedBrowseFilters = BrowseFilterSnapshot.from(_bookListBrowseUi.value)
    }

    init {
        viewModelScope.launch {
            combine(
                _browseListFiltersReady,
                _bookListBrowseUi,
                _bookListCategory,
            ) { ready, _, cat ->
                Triple(ready, _bookListBrowseUi.value, cat)
            }.collect { (ready, _, cat) ->
                if (cat != null || !ready) return@collect
                val snap = BrowseFilterSnapshot.from(_bookListBrowseUi.value)
                if (snap == lastAppliedBrowseFilters) return@collect
                runBrowseListFilterRequest(snap)
            }
        }
    }

    fun onBookListRouteArgs(searchQuery: String?, category: String?) {
        val prev = bookListScreenArgs
        bookListScreenArgs = category to searchQuery
        val categoryChanged = prev?.first != category
        val iq = searchQuery ?: ""
        bookListSearchDebounceJob?.cancel()
        _bookListCategory.value = category
        when {
            category == null && categoryChanged ->
                _bookListBrowseUi.value = BookListBrowseUiState(searchQuery = iq, debouncedSearchQuery = iq)

            category == null ->
                _bookListBrowseUi.update {
                    it.copy(searchQuery = iq, debouncedSearchQuery = iq, showRecentSearches = false)
                }

            else ->
                _bookListBrowseUi.update {
                    it.copy(
                        searchQuery = iq,
                        debouncedSearchQuery = iq,
                        showRecentSearches = false,
                        showFilters = false,
                    )
                }
        }
        applyBrowseFilterSnapshotFromCurrentUi()
        onBrowseListRouteChanged(category, searchQuery, _browseListPageSize.value)
    }

    fun updateBookListSearchInput(text: String) {
        _bookListBrowseUi.update { it.copy(searchQuery = text) }
        bookListSearchDebounceJob?.cancel()
        bookListSearchDebounceJob = viewModelScope.launch {
            delay(300)
            _bookListBrowseUi.update { it.copy(debouncedSearchQuery = text) }
            if (_bookListCategory.value == null && _browseListFiltersReady.value && text.isNotBlank()) {
                searchHistoryManager.addSearch(text)
            }
        }
    }

    fun applyBookListRecentSearch(text: String) {
        bookListSearchDebounceJob?.cancel()
        _bookListBrowseUi.update {
            it.copy(searchQuery = text, debouncedSearchQuery = text, showRecentSearches = false)
        }
        viewModelScope.launch {
            if (_bookListCategory.value == null && _browseListFiltersReady.value && text.isNotBlank()) {
                searchHistoryManager.addSearch(text)
            }
        }
    }

    fun clearBookListSearchImmediate() {
        bookListSearchDebounceJob?.cancel()
        _bookListBrowseUi.update { it.copy(searchQuery = "", debouncedSearchQuery = "") }
    }

    fun setBookListShowFilters(show: Boolean) {
        _bookListBrowseUi.update { it.copy(showFilters = show) }
        if (show) loadGenresForBrowseFilters()
    }

    fun setBookListShowRecentSearches(show: Boolean) {
        _bookListBrowseUi.update { it.copy(showRecentSearches = show) }
    }

    fun updateBookListBrowseUi(transform: (BookListBrowseUiState) -> BookListBrowseUiState) {
        _bookListBrowseUi.update(transform)
    }

    fun clearBookListSearchHistory() {
        viewModelScope.launch { searchHistoryManager.clearHistory() }
    }

    fun loadMoreBrowseList(category: String?) {
        if (category != null) return
        if (!_browseListHasMore.value) return
        viewModelScope.launch {
            if (_browseListLoadingMore.value) return@launch
            _browseListLoadingMore.value = true
            try {
                val pageSize = _browseListPageSize.value
                val nextPage = _browseListPage.value + 1
                val snap = BrowseFilterSnapshot.from(_bookListBrowseUi.value)
                runBrowseBooks(
                    query = snap.debouncedSearch.takeIf { it.isNotBlank() },
                    genres = snap.genres.takeIf { it.isNotEmpty() }?.toList(),
                    ageRating = snap.ageRating?.takeIf { it.isNotBlank() },
                    distributionType = snap.distributionType?.takeIf { it.isNotBlank() },
                    minAvgRating = if (snap.minRating > 0) snap.minRating.toDouble() else null,
                    maxAvgRating = if (snap.maxRating < 5) snap.maxRating.toDouble() else null,
                    page = nextPage,
                    limit = pageSize,
                    applicationStatus = snap.applicationStatus?.takeIf { it.isNotBlank() },
                    deadlineFilter = snap.deadlineFilter?.takeIf { it.isNotBlank() },
                    sortBy = snap.sortBy?.takeIf { it.isNotBlank() },
                )
                    .onSuccess { books ->
                        applyBrowseListPageResult(
                            books = books,
                            append = true,
                            pageSize = pageSize,
                            newPage = nextPage,
                        )
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load more books"
                    }
            } finally {
                _browseListLoadingMore.value = false
            }
        }
    }

    fun onBrowseListRouteChanged(category: String?, searchQuery: String?, pageSize: Int) {
        viewModelScope.launch {
            _browseListFiltersReady.value = false
            _browseListPage.value = 1
            _browseListHasMore.value = false
            _browseListPageSize.value = pageSize
            try {
                _isLoading.value = true
                _error.value = null
                when (category) {
                    "search" -> {
                        val q = searchQuery
                        if (q == null) {
                            _browseListFiltersReady.value = true
                            return@launch
                        }
                        runBrowseBooks(query = q, page = 1, limit = pageSize)
                            .onSuccess { books ->
                                applyBrowseListPageResult(
                                    books = books,
                                    append = false,
                                    pageSize = pageSize,
                                    newPage = 1,
                                )
                                applyBrowseFilterSnapshotFromCurrentUi()
                                _browseListFiltersReady.value = true
                            }
                            .onFailure { e ->
                                _lastFetchCount.value = 0
                                _error.value = e.message ?: "Failed to load books"
                                _browseListFiltersReady.value = true
                            }
                    }

                    null -> {
                        runBrowseBooks(query = null, page = 1, limit = pageSize)
                            .onSuccess { books ->
                                applyBrowseListPageResult(
                                    books = books,
                                    append = false,
                                    pageSize = pageSize,
                                    newPage = 1,
                                )
                                applyBrowseFilterSnapshotFromCurrentUi()
                                _browseListFiltersReady.value = true
                            }
                            .onFailure { e ->
                                _lastFetchCount.value = 0
                                _error.value = e.message ?: "Failed to load books"
                                _browseListFiltersReady.value = true
                            }
                    }

                    else -> {
                        _browseListFiltersReady.value = true
                    }
                }
            } catch (e: Exception) {
                _lastFetchCount.value = 0
                _error.value = e.message ?: "Failed to load books"
                _browseListFiltersReady.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadGenresForBrowseFilters() {
        viewModelScope.launch {
            if (_browseFilterGenres.value.isNotEmpty() || _browseGenresLoading.value) return@launch
            _browseGenresLoading.value = true
            getGenresUseCase()
                .onSuccess { _browseFilterGenres.value = it }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to load genres"
                }
            _browseGenresLoading.value = false
        }
    }

    fun ensureSeriesBooksLoaded(
        seriesId: String,
        forceRefresh: Boolean = false,
        treatFailureAsEmptyCatalog: Boolean = true,
        status: String? = "active",
    ) {
        if (seriesId.isBlank()) return
        viewModelScope.launch {
            val shouldFetch = seriesBooksClaimMutex.withLock {
                if (!forceRefresh && _seriesBooksBySeriesId.value.containsKey(seriesId)) return@withLock false
                if (_seriesBooksLoadingIds.value.contains(seriesId)) return@withLock false
                _seriesBooksLoadingIds.value = _seriesBooksLoadingIds.value + seriesId
                if (forceRefresh) {
                    _seriesBooksBySeriesId.update { it - seriesId }
                    _seriesBooksLoadError.update { it - seriesId }
                }
                true
            }
            if (!shouldFetch) return@launch

            try {
                browseBooksUseCase(
                    seriesId = seriesId,
                    status = status?.takeIf { it.isNotBlank() },
                    page = 1,
                    limit = 100,
                ).onSuccess { bookList ->
                    val sorted = bookList
                        .filter { it.seriesId == seriesId }
                        .sortedBy { it.seriesOrder ?: Int.MAX_VALUE }
                    _seriesBooksBySeriesId.update { it + (seriesId to sorted) }
                    _seriesBooksLoadError.update { it - seriesId }
                    bookCatalogCache.register(sorted)
                }.onFailure { e ->
                    val message = e.message ?: "Failed to load series books"
                    if (treatFailureAsEmptyCatalog) {
                        _seriesBooksBySeriesId.update { it + (seriesId to emptyList()) }
                    } else {
                        _seriesBooksLoadError.update { it + (seriesId to message) }
                    }
                }
            } finally {
                _seriesBooksLoadingIds.update { it - seriesId }
            }
        }
    }

    fun clearSeriesBooksLoadError(seriesId: String) {
        _seriesBooksLoadError.update { it - seriesId }
    }

    private suspend fun runBrowseListFilterRequest(snap: BrowseFilterSnapshot) {
        val pageSize = _browseListPageSize.value
        try {
            _isLoading.value = true
            _error.value = null
            runBrowseBooks(
                query = snap.debouncedSearch.takeIf { it.isNotBlank() },
                genres = snap.genres.takeIf { it.isNotEmpty() }?.toList(),
                ageRating = snap.ageRating?.takeIf { it.isNotBlank() },
                distributionType = snap.distributionType?.takeIf { it.isNotBlank() },
                minAvgRating = if (snap.minRating > 0) snap.minRating.toDouble() else null,
                maxAvgRating = if (snap.maxRating < 5) snap.maxRating.toDouble() else null,
                page = 1,
                limit = pageSize,
                applicationStatus = snap.applicationStatus?.takeIf { it.isNotBlank() },
                deadlineFilter = snap.deadlineFilter?.takeIf { it.isNotBlank() },
                sortBy = snap.sortBy?.takeIf { it.isNotBlank() },
            ).onSuccess { books ->
                applyBrowseListPageResult(
                    books = books,
                    append = false,
                    pageSize = pageSize,
                    newPage = 1,
                )
                lastAppliedBrowseFilters = snap
            }.onFailure { e ->
                _lastFetchCount.value = 0
                _error.value = e.message ?: "Failed to load books"
            }
        } catch (e: Exception) {
            _lastFetchCount.value = 0
            _error.value = e.message ?: "Failed to load books"
        } finally {
            _isLoading.value = false
        }
    }

    private fun applyBrowseListPageResult(
        books: List<RecommendedBookResponse>,
        append: Boolean,
        pageSize: Int,
        newPage: Int,
    ) {
        if (append && newPage > 1) {
            val merged = _books.value + books
            _books.value = merged
            bookCatalogCache.register(merged)
        } else {
            _books.value = books
            bookCatalogCache.register(books)
        }
        _lastFetchCount.value = books.size
        _browseListPage.value = newPage
        _browseListHasMore.value = books.size >= pageSize
    }

    private suspend fun runBrowseBooks(
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
        page: Int? = null,
        limit: Int? = null,
        status: String? = null,
        applicationStatus: String? = null,
        deadlineFilter: String? = null,
        sortBy: String? = null,
    ): Result<List<RecommendedBookResponse>> =
        browseBooksUseCase(
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
            page = page,
            limit = limit,
            status = status,
            applicationStatus = applicationStatus,
            deadlineFilter = deadlineFilter,
            sortBy = sortBy,
        )
}
