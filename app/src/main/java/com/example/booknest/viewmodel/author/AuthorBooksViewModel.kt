package com.example.booknest.viewmodel.author

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.usecase.applications.GetBookApplicationsUseCase
import com.example.booknest.domain.usecase.author.DeleteBookUseCase
import com.example.booknest.domain.usecase.author.GetBookStatsUseCase
import com.example.booknest.domain.usecase.author.GetMyBooksUseCase
import com.example.booknest.domain.usecase.author.PublishBookUseCase
import com.example.booknest.viewmodel.applications.pendingCount
import com.example.booknest.viewmodel.common.UserFeedback
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BookStatus(val value: String) {
    DRAFT("draft"),
    ACTIVE("active"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    ARCHIVED("archived"),
}

enum class BookSortOption {
    DATE_CREATED, TITLE, STATUS, APPLICATION_COUNT,
}

private data class BooksFilterState(
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val sortOption: BookSortOption = BookSortOption.DATE_CREATED,
)

class AuthorBooksViewModel(
    private val feedback: UserFeedback,
    private val getMyBooksUseCase: GetMyBooksUseCase,
    private val getBookStatsUseCase: GetBookStatsUseCase,
    private val getBookApplicationsUseCase: GetBookApplicationsUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val publishBookUseCase: PublishBookUseCase,
    catalogRefresher: AuthorBooksCatalogRefresher,
) : ViewModel() {

    private val _myBooks = MutableStateFlow<List<BookResponse>>(emptyList())
    val myBooks: StateFlow<List<BookResponse>> = _myBooks.asStateFlow()

    private val _isLoadingBooks = MutableStateFlow(false)
    val isLoadingBooks: StateFlow<Boolean> = _isLoadingBooks.asStateFlow()

    private val _bookStats = MutableStateFlow<Map<String, BookStatsResponse>>(emptyMap())
    val bookStats: StateFlow<Map<String, BookStatsResponse>> = _bookStats.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    private fun notifyError(message: String) = feedback.error(message, _error)
    private fun notifySuccess(message: String) = feedback.success(message, _successMessage)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _sortOption = MutableStateFlow(BookSortOption.DATE_CREATED)
    val sortOption: StateFlow<BookSortOption> = _sortOption.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun updateSortOption(option: BookSortOption) {
        _sortOption.value = option
    }

    val tabCounts: StateFlow<Map<Int, Int>> = _myBooks.map { books ->
        val nonArchived = books.filter { it.status != BookStatus.ARCHIVED.value }
        mapOf(
            0 to nonArchived.size,
            1 to nonArchived.count { it.status == BookStatus.DRAFT.value },
            2 to nonArchived.count { it.status == BookStatus.ACTIVE.value },
            3 to nonArchived.count { it.status == BookStatus.IN_PROGRESS.value },
            4 to nonArchived.count { it.status == BookStatus.COMPLETED.value },
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val _filterState: StateFlow<BooksFilterState> = combine(
        _searchQuery, _selectedTab, _sortOption,
    ) { query, tab, sort -> BooksFilterState(query, tab, sort) }
        .stateIn(viewModelScope, SharingStarted.Lazily, BooksFilterState())

    val filteredBooks: StateFlow<List<BookResponse>> = combine(
        _myBooks, _filterState, _bookStats,
    ) { books, filter, stats ->
        val nonArchived = books.filter { it.status != BookStatus.ARCHIVED.value }
        val byTab = when (filter.selectedTab) {
            1 -> nonArchived.filter { it.status == BookStatus.DRAFT.value }
            2 -> nonArchived.filter { it.status == BookStatus.ACTIVE.value }
            3 -> nonArchived.filter { it.status == BookStatus.IN_PROGRESS.value }
            4 -> nonArchived.filter { it.status == BookStatus.COMPLETED.value }
            else -> nonArchived
        }
        val searched = if (filter.searchQuery.isBlank()) {
            byTab
        } else {
            byTab.filter { book ->
                book.title.contains(filter.searchQuery, ignoreCase = true) ||
                    book.shortDescription?.contains(filter.searchQuery, ignoreCase = true) == true
            }
        }
        when (filter.sortOption) {
            BookSortOption.DATE_CREATED -> searched.sortedByDescending { it.createdAt ?: "" }
            BookSortOption.TITLE -> searched.sortedBy { it.title }
            BookSortOption.STATUS -> searched.sortedBy { it.status }
            BookSortOption.APPLICATION_COUNT -> searched.sortedByDescending { book ->
                stats[book.id]?.effectiveTotalApplications ?: 0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            catalogRefresher.refreshRequests.collect {
                loadMyBooks()
            }
        }
    }

    fun loadMyBooks() {
        viewModelScope.launch {
            try {
                _isLoadingBooks.value = true
                getMyBooksUseCase()
                    .onSuccess { books ->
                        _myBooks.value = books
                        books.forEach { book -> getBookStats(book.id) }
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to load books") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error loading books")
            } finally {
                _isLoadingBooks.value = false
            }
        }
    }

    fun getBookStats(bookId: String) {
        viewModelScope.launch {
            try {
                getBookStatsUseCase(bookId)
                    .onSuccess { stats ->
                        val applications = getBookApplicationsUseCase(bookId).getOrNull().orEmpty()
                        val corrected = stats.copy(
                            pendingApplications = applications.pendingCount(),
                        )
                        _bookStats.value = _bookStats.value + (bookId to corrected)
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to load book stats") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error loading book stats")
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            try {
                deleteBookUseCase(bookId)
                    .onSuccess {
                        notifySuccess("Book deleted successfully!")
                        loadMyBooks()
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to delete book") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error deleting book")
            }
        }
    }

    fun publishBook(
        bookId: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
    ) {
        viewModelScope.launch(NonCancellable) {
            try {
                publishBookUseCase(bookId)
                    .onSuccess {
                        notifySuccess("Book published successfully")
                        loadMyBooks()
                        onSuccess?.invoke()
                    }
                    .onFailure { e ->
                        if (e !is CancellationException) {
                            val message = e.message ?: "Failed to publish book"
                            notifyError(message)
                            onError?.invoke(message)
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    val message = e.message ?: "Error publishing book"
                    notifyError(message)
                    onError?.invoke(message)
                }
            }
        }
    }
}
