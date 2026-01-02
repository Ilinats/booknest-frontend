package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.AuthorAnalyticsResponse
import com.example.booknest.domain.model.response.BookPerformanceComparisonResponse
import com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse
import com.example.booknest.domain.model.response.RatingDistributionResponse
import com.example.booknest.domain.repository.BooksRepository
import com.example.booknest.domain.usecase.analytics.GetAuthorAnalyticsUseCase
import com.example.booknest.domain.usecase.analytics.GetDetailedBookAnalyticsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookAnalyticsUiState {
    object Idle : BookAnalyticsUiState()
    object Loading : BookAnalyticsUiState()
    data class Success(val analytics: DetailedBookAnalyticsResponse) : BookAnalyticsUiState()
    data class Error(val message: String) : BookAnalyticsUiState()
}

sealed class AuthorAnalyticsUiState {
    object Idle : AuthorAnalyticsUiState()
    object Loading : AuthorAnalyticsUiState()
    data class Success(val analytics: AuthorAnalyticsResponse) : AuthorAnalyticsUiState()
    data class Error(val message: String) : AuthorAnalyticsUiState()
}

class AnalyticsViewModel(
    private val getDetailedBookAnalyticsUseCase: GetDetailedBookAnalyticsUseCase,
    private val getAuthorAnalyticsUseCase: GetAuthorAnalyticsUseCase,
    private val booksRepository: BooksRepository
) : ViewModel() {

    private val _bookAnalyticsState =
        MutableStateFlow<BookAnalyticsUiState>(BookAnalyticsUiState.Idle)
    val bookAnalyticsState: StateFlow<BookAnalyticsUiState> = _bookAnalyticsState

    private val _currentBookAnalytics = MutableStateFlow<DetailedBookAnalyticsResponse?>(null)
    val currentBookAnalytics: StateFlow<DetailedBookAnalyticsResponse?> = _currentBookAnalytics

    private val _authorAnalyticsState =
        MutableStateFlow<AuthorAnalyticsUiState>(AuthorAnalyticsUiState.Idle)
    val authorAnalyticsState: StateFlow<AuthorAnalyticsUiState> = _authorAnalyticsState

    private val _currentAuthorAnalytics = MutableStateFlow<AuthorAnalyticsResponse?>(null)
    val currentAuthorAnalytics: StateFlow<AuthorAnalyticsResponse?> = _currentAuthorAnalytics

    private val _bookPerformanceComparison =
        MutableStateFlow<List<BookPerformanceComparisonResponse>>(emptyList())
    val bookPerformanceComparison: StateFlow<List<BookPerformanceComparisonResponse>> =
        _bookPerformanceComparison.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    fun loadDetailedBookAnalytics(bookId: String) {
        viewModelScope.launch {
            _bookAnalyticsState.value = BookAnalyticsUiState.Loading
            try {
                val result = getDetailedBookAnalyticsUseCase(bookId)
                result
                    .onSuccess { analytics ->
                        _currentBookAnalytics.value = analytics
                        _bookAnalyticsState.value = BookAnalyticsUiState.Success(analytics)
                    }
                    .onFailure { e ->
                        val errorMessage = e.message ?: "Failed to load book analytics"
                        _bookAnalyticsState.value = BookAnalyticsUiState.Error(errorMessage)
                        _snackbarEvent.emit("Error: $errorMessage")
                    }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _bookAnalyticsState.value = BookAnalyticsUiState.Error(errorMessage)
                _snackbarEvent.emit(errorMessage)
            }
        }
    }

    fun loadAuthorAnalytics(dateRange: String? = null) {
        viewModelScope.launch {
            _authorAnalyticsState.value = AuthorAnalyticsUiState.Loading
            try {
                val result = getAuthorAnalyticsUseCase(dateRange)
                result
                    .onSuccess { analytics ->
                        _currentAuthorAnalytics.value = analytics
                        _authorAnalyticsState.value = AuthorAnalyticsUiState.Success(analytics)
                    }
                    .onFailure { e ->
                        val errorMessage = e.message ?: "Failed to load author analytics"
                        _authorAnalyticsState.value = AuthorAnalyticsUiState.Error(errorMessage)
                        _snackbarEvent.emit("Error: $errorMessage")
                    }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _authorAnalyticsState.value = AuthorAnalyticsUiState.Error(errorMessage)
                _snackbarEvent.emit(errorMessage)
            }
        }
    }

    fun clearBookAnalyticsState() {
        _bookAnalyticsState.value = BookAnalyticsUiState.Idle
        _currentBookAnalytics.value = null
    }

    fun clearAuthorAnalyticsState() {
        _authorAnalyticsState.value = AuthorAnalyticsUiState.Idle
        _currentAuthorAnalytics.value = null
    }

    fun loadBookPerformanceComparison() {
        viewModelScope.launch {
            try {
                val result = booksRepository.getBookPerformanceComparison()
                result
                    .onSuccess { comparison ->
                        _bookPerformanceComparison.value = comparison
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(
                            e.message ?: "Failed to load book performance comparison"
                        )
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error loading book performance comparison: ${e.message}")
            }
        }
    }

    fun calculateApprovalRate(approved: Int, total: Int): Double {
        return if (total > 0) (approved.toDouble() / total * 100) else 0.0
    }

    fun calculateRejectionRate(rejected: Int, total: Int): Double {
        return if (total > 0) (rejected.toDouble() / total * 100) else 0.0
    }

    fun getRatingDistributionList(ratingDistribution: RatingDistributionResponse): List<Pair<Int, Int>> {
        return listOf(
            1 to ratingDistribution.`1`,
            2 to ratingDistribution.`2`,
            3 to ratingDistribution.`3`,
            4 to ratingDistribution.`4`,
            5 to ratingDistribution.`5`
        )
    }

    fun getTopRatingCount(ratingDistribution: RatingDistributionResponse): Int {
        return maxOf(
            ratingDistribution.`1`,
            ratingDistribution.`2`,
            ratingDistribution.`3`,
            ratingDistribution.`4`,
            ratingDistribution.`5`
        )
    }

    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                java.util.Locale.getDefault()
            )
            val outputFormat =
                java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatMonth(monthString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                java.util.Locale.getDefault()
            )
            val outputFormat = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(monthString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            monthString
        }
    }
}
