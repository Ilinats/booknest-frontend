package com.example.booknest.viewmodel.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.TrendingBookResponse
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.books.GetBookDetailsUseCase
import com.example.booknest.domain.usecase.books.GetNewReleasesUseCase
import com.example.booknest.domain.usecase.books.GetRecommendedBooksUseCase
import com.example.booknest.domain.usecase.books.GetTrendingBooksUseCase
import com.example.booknest.domain.usecase.books.SearchBooksUseCase
import com.example.booknest.data.session.SearchHistoryManager
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.utils.DebugLog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(FlowPreview::class)
class BookViewModel(
    private val getRecommendedBooksUseCase: GetRecommendedBooksUseCase,
    private val getNewReleasesUseCase: GetNewReleasesUseCase,
    private val browseBooksUseCase: BrowseBooksUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val getBookDetailsUseCase: GetBookDetailsUseCase,
    private val getTrendingBooksUseCase: GetTrendingBooksUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val searchHistoryManager: SearchHistoryManager,
) : ViewModel() {

    companion object {
        private const val BROWSE_LIST_PAGE_SIZE = 20
    }

    private val _books = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val books: StateFlow<List<RecommendedBookResponse>> = _books.asStateFlow()

    private val _bookDetails = MutableStateFlow<BookResponse?>(null)
    val bookDetails: StateFlow<BookResponse?> = _bookDetails.asStateFlow()

    private val _bookDetailsScreenId = MutableStateFlow<String?>(null)
    private val _bookDetailsScreenMerged = MutableStateFlow<BookResponse?>(null)
    private val _bookDetailsScreenLoading = MutableStateFlow(false)
    val bookDetailsScreenBook: StateFlow<BookResponse?> = _bookDetailsScreenMerged.asStateFlow()
    val bookDetailsScreenLoading: StateFlow<Boolean> = _bookDetailsScreenLoading.asStateFlow()

    private fun mergeDetailsScreenBook(partial: BookResponse?, full: BookResponse): BookResponse {
        if (partial == null) return full
        if (full.fullDescription != null && partial.fullDescription == null) return full
        return partial
    }

    fun beginBookDetailsScreen(bookId: String) {
        if (bookId.isBlank()) return
        _bookDetailsScreenId.value = bookId
        val cached = findBookInCache(bookId)
        _bookDetailsScreenMerged.value = cached
        _bookDetailsScreenLoading.value = cached == null
        getBookDetails(bookId, syncDetailsScreen = true)
    }

    fun refreshBookDetailsScreenFromCache() {
        val id = _bookDetailsScreenId.value ?: return
        if (_bookDetailsScreenMerged.value == null) {
            val fromCache = findBookInCache(id)
            if (fromCache != null) {
                _bookDetailsScreenMerged.value = fromCache
                _bookDetailsScreenLoading.value = false
            }
        }
    }

    fun applyBookDetailsFromApplicationCheck(stub: BookResponse?) {
        val id = _bookDetailsScreenId.value ?: return
        if (stub == null || stub.id != id) return
        if (_bookDetailsScreenMerged.value == null) {
            _bookDetailsScreenMerged.value = stub
            _bookDetailsScreenLoading.value = false
        }
    }

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

    fun clearError() { _error.value = null }

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

    private suspend fun runBrowseListFilterRequest(snap: BrowseFilterSnapshot) {
        val pageSize = _browseListPageSize.value
        try {
            _isLoading.value = true
            _error.value = null
            val query = snap.debouncedSearch.takeIf { it.isNotBlank() }
            val genreList = snap.genres.takeIf { it.isNotEmpty() }?.toList()
            val ageRating = snap.ageRating?.takeIf { it.isNotBlank() }
            val distributionType = snap.distributionType?.takeIf { it.isNotBlank() }
            val minAvgRating = if (snap.minRating > 0) snap.minRating.toDouble() else null
            val maxAvgRating = if (snap.maxRating < 5) snap.maxRating.toDouble() else null
            val applicationStatus = snap.applicationStatus?.takeIf { it.isNotBlank() }
            val deadlineFilter = snap.deadlineFilter?.takeIf { it.isNotBlank() }
            val sortBy = snap.sortBy?.takeIf { it.isNotBlank() }
            runBrowseBooks(
                query = query,
                genres = genreList,
                ageRating = ageRating,
                distributionType = distributionType,
                minAvgRating = minAvgRating,
                maxAvgRating = maxAvgRating,
                page = 1,
                limit = pageSize,
                applicationStatus = applicationStatus,
                deadlineFilter = deadlineFilter,
                sortBy = sortBy,
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
                val query = snap.debouncedSearch.takeIf { it.isNotBlank() }
                val genreList = snap.genres.takeIf { it.isNotEmpty() }?.toList()
                val ageRating = snap.ageRating?.takeIf { it.isNotBlank() }
                val distributionType = snap.distributionType?.takeIf { it.isNotBlank() }
                val minAvgRating = if (snap.minRating > 0) snap.minRating.toDouble() else null
                val maxAvgRating = if (snap.maxRating < 5) snap.maxRating.toDouble() else null
                val applicationStatus = snap.applicationStatus?.takeIf { it.isNotBlank() }
                val deadlineFilter = snap.deadlineFilter?.takeIf { it.isNotBlank() }
                val sortBy = snap.sortBy?.takeIf { it.isNotBlank() }
                runBrowseBooks(
                    query = query,
                    genres = genreList,
                    ageRating = ageRating,
                    distributionType = distributionType,
                    minAvgRating = minAvgRating,
                    maxAvgRating = maxAvgRating,
                    page = nextPage,
                    limit = pageSize,
                    applicationStatus = applicationStatus,
                    deadlineFilter = deadlineFilter,
                    sortBy = sortBy,
                ).onSuccess { books ->
                    applyBrowseListPageResult(
                        books = books,
                        append = true,
                        pageSize = pageSize,
                        newPage = nextPage,
                    )
                }.onFailure { e ->
                    _error.value = e.message ?: "Failed to load more books"
                }
            } finally {
                _browseListLoadingMore.value = false
            }
        }
    }

    private fun applyBrowseListPageResult(
        books: List<RecommendedBookResponse>,
        append: Boolean,
        pageSize: Int,
        newPage: Int,
    ) {
        if (append && newPage > 1) {
            _books.value = _books.value + books
        } else {
            _books.value = books
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

    /**
     * Loads books for a series into [seriesBooksBySeriesId]. Used by series management and the series reader screen.
     *
     * @param treatFailureAsEmptyCatalog when true, failures store an empty list (management UI). When false, the
     * series id is omitted from the map and [seriesBooksLoadError] receives a message (reader screen retry UX).
     */
    fun ensureSeriesBooksLoaded(
        seriesId: String,
        forceRefresh: Boolean = false,
        treatFailureAsEmptyCatalog: Boolean = true,
        /** When null, all statuses are requested (author series management). */
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
        viewModelScope.launch {
            combine(
                _browseListFiltersReady,
                _bookListBrowseUi,
                _bookListCategory,
            ) { ready, ui, cat ->
                Triple(ready, ui, cat)
            }.collect { (ready, _, cat) ->
                if (cat != null || !ready) return@collect
                val snap = BrowseFilterSnapshot.from(_bookListBrowseUi.value)
                if (snap == lastAppliedBrowseFilters) return@collect
                runBrowseListFilterRequest(snap)
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
                val result = getRecommendedBooksUseCase(10)
                result
                    .onSuccess { books ->
                        _recommendedBooks.value = books
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
                val result = getNewReleasesUseCase(daysBack = 30, limit = 10)
                result
                    .onSuccess { books ->
                        _newReleases.value = books
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
        page: Int? = null,
        limit: Int? = null,
        status: String? = null,
        applicationStatus: String? = null,
        deadlineFilter: String? = null,
        sortBy: String? = null,
        append: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                runBrowseBooks(
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
                    .onSuccess { books ->
                        if (append && page != null && page > 1) {
                            _books.value = _books.value + books
                        } else {
                            _books.value = books
                        }
                        _lastFetchCount.value = books.size
                    }
                    .onFailure { e ->
                        _lastFetchCount.value = 0
                        _error.value = e.message ?: "Failed to load books"
                    }
            } catch (e: Exception) {
                _lastFetchCount.value = 0
                _error.value = e.message ?: "Failed to load books"
                DebugLog.w("BookVM", "browseBooks unexpected failure", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _authorBooks = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val authorBooks: StateFlow<List<RecommendedBookResponse>> = _authorBooks.asStateFlow()

    private val _authorBooksLoading = MutableStateFlow(false)
    val authorBooksLoading: StateFlow<Boolean> = _authorBooksLoading.asStateFlow()

    private fun searchBooks(query: String, page: Int? = null, limit: Int? = null) {
        viewModelScope.launch {
            try {
                val result = searchBooksUseCase(query, page, limit)
                result
                    .onSuccess { books ->
                        _books.value = books
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Search failed"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Search failed"
            }
        }
    }

    fun getBookDetails(bookId: String, syncDetailsScreen: Boolean = false) {
        viewModelScope.launch {
            try {
                val result = getBookDetailsUseCase(bookId)
                result
                    .onSuccess { book ->
                        _bookDetails.value = book
                        if (syncDetailsScreen && _bookDetailsScreenId.value == bookId) {
                            _bookDetailsScreenMerged.value =
                                mergeDetailsScreenBook(_bookDetailsScreenMerged.value, book)
                            _bookDetailsScreenLoading.value = false
                        }
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load book details"
                        if (syncDetailsScreen && _bookDetailsScreenId.value == bookId) {
                            _bookDetailsScreenLoading.value = false
                        }
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load book details"
                if (syncDetailsScreen && _bookDetailsScreenId.value == bookId) {
                    _bookDetailsScreenLoading.value = false
                }
            }
        }
    }

    fun searchForHomeScreen(query: String, limit: Int = 20) {
        viewModelScope.launch {
            try {
                _homeSearchLoading.value = true
                val result = browseBooksUseCase(
                    query = query,
                    page = 1,
                    limit = limit,
                    status = "active"
                )
                result
                    .onSuccess { books ->
                        _homeSearchResults.value = books
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
                    authorId = authorId,
                    page = 1,
                    limit = 100,
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
                _trendingLoading.value = true
                val result = getTrendingBooksUseCase(10)
                result.onSuccess { trendingBooks ->
                    _trendingBooks.value = trendingBooks
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load trending books"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading trending books"
            } finally {
                _trendingLoading.value = false
            }
        }
    }
}
