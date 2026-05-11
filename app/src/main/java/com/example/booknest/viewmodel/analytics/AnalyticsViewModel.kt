package com.example.booknest.viewmodel.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.AuthorAnalyticsResponse
import com.example.booknest.domain.model.response.BookPerformanceComparisonResponse
import com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse
import com.example.booknest.domain.model.response.RatingDistributionResponse
import com.example.booknest.domain.usecase.analytics.GetAuthorAnalyticsUseCase
import com.example.booknest.domain.usecase.analytics.GetBookPerformanceComparisonUseCase
import com.example.booknest.domain.usecase.analytics.GetDetailedBookAnalyticsUseCase
import com.example.booknest.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val getDetailedBookAnalyticsUseCase: GetDetailedBookAnalyticsUseCase,
    private val getAuthorAnalyticsUseCase: GetAuthorAnalyticsUseCase,
    private val getBookPerformanceComparisonUseCase: GetBookPerformanceComparisonUseCase
) : ViewModel() {

    private val _bookAnalyticsState =
        MutableStateFlow<UiState<DetailedBookAnalyticsResponse>>(UiState.Idle)
    val bookAnalyticsState: StateFlow<UiState<DetailedBookAnalyticsResponse>> =
        _bookAnalyticsState.asStateFlow()

    private val _currentBookAnalytics = MutableStateFlow<DetailedBookAnalyticsResponse?>(null)
    val currentBookAnalytics: StateFlow<DetailedBookAnalyticsResponse?> =
        _currentBookAnalytics.asStateFlow()

    private val _authorAnalyticsState =
        MutableStateFlow<UiState<AuthorAnalyticsResponse>>(UiState.Idle)
    val authorAnalyticsState: StateFlow<UiState<AuthorAnalyticsResponse>> =
        _authorAnalyticsState.asStateFlow()

    private val _currentAuthorAnalytics = MutableStateFlow<AuthorAnalyticsResponse?>(null)
    val currentAuthorAnalytics: StateFlow<AuthorAnalyticsResponse?> =
        _currentAuthorAnalytics.asStateFlow()

    private val _bookPerformanceComparison =
        MutableStateFlow<List<BookPerformanceComparisonResponse>>(emptyList())
    val bookPerformanceComparison: StateFlow<List<BookPerformanceComparisonResponse>> =
        _bookPerformanceComparison.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    fun loadDetailedBookAnalytics(bookId: String) {
        viewModelScope.launch {
            _bookAnalyticsState.value = UiState.Loading
            try {
                val result = getDetailedBookAnalyticsUseCase(bookId)
                result
                    .onSuccess { analytics ->
                        _currentBookAnalytics.value = analytics
                        _bookAnalyticsState.value = UiState.Success(analytics)
                    }
                    .onFailure { e ->
                        val errorMessage = e.message ?: "Failed to load book analytics"
                        _bookAnalyticsState.value = UiState.Error(errorMessage, e)
                        _error.value = errorMessage
                    }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _bookAnalyticsState.value = UiState.Error(errorMessage, e)
                _error.value = errorMessage
            }
        }
    }

    fun loadAuthorAnalytics(dateRange: String? = null) {
        viewModelScope.launch {
            _authorAnalyticsState.value = UiState.Loading
            try {
                val result = getAuthorAnalyticsUseCase(dateRange)
                result
                    .onSuccess { analytics ->
                        _currentAuthorAnalytics.value = analytics
                        _authorAnalyticsState.value = UiState.Success(analytics)
                    }
                    .onFailure { e ->
                        val errorMessage = e.message ?: "Failed to load author analytics"
                        _authorAnalyticsState.value = UiState.Error(errorMessage, e)
                        _error.value = errorMessage
                    }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _authorAnalyticsState.value = UiState.Error(errorMessage, e)
                _error.value = errorMessage
            }
        }
    }

    fun loadBookPerformanceComparison() {
        viewModelScope.launch {
            try {
                val result = getBookPerformanceComparisonUseCase()
                result
                    .onSuccess { comparison ->
                        _bookPerformanceComparison.value = comparison
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load book performance comparison"
                    }
            } catch (e: Exception) {
                _error.value = "Error loading book performance comparison: ${e.message}"
            }
        }
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
