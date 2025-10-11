package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BookAnalyticsUiState {
    object Idle : BookAnalyticsUiState()
    object Loading : BookAnalyticsUiState()
    data class Success(val analytics: DetailedBookAnalytics) : BookAnalyticsUiState()
    data class Error(val message: String) : BookAnalyticsUiState()
}

sealed class AuthorAnalyticsUiState {
    object Idle : AuthorAnalyticsUiState()
    object Loading : AuthorAnalyticsUiState()
    data class Success(val analytics: AuthorAnalytics) : AuthorAnalyticsUiState()
    data class Error(val message: String) : AuthorAnalyticsUiState()
}

class AnalyticsViewModel(private val authManager: AuthManager) : ViewModel() {

    // Book Analytics state
    private val _bookAnalyticsState = MutableStateFlow<BookAnalyticsUiState>(BookAnalyticsUiState.Idle)
    val bookAnalyticsState: StateFlow<BookAnalyticsUiState> = _bookAnalyticsState

    private val _currentBookAnalytics = MutableStateFlow<DetailedBookAnalytics?>(null)
    val currentBookAnalytics: StateFlow<DetailedBookAnalytics?> = _currentBookAnalytics

    // Author Analytics state
    private val _authorAnalyticsState = MutableStateFlow<AuthorAnalyticsUiState>(AuthorAnalyticsUiState.Idle)
    val authorAnalyticsState: StateFlow<AuthorAnalyticsUiState> = _authorAnalyticsState

    private val _currentAuthorAnalytics = MutableStateFlow<AuthorAnalytics?>(null)
    val currentAuthorAnalytics: StateFlow<AuthorAnalytics?> = _currentAuthorAnalytics

    // Common state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    // Book Analytics operations
    fun loadDetailedBookAnalytics(bookId: String) {
        viewModelScope.launch {
            _bookAnalyticsState.value = BookAnalyticsUiState.Loading
            try {
                val response = RetrofitInstance.api.getDetailedBookAnalytics(bookId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val analytics = apiResponse.data
                        if (analytics != null) {
                            _currentBookAnalytics.value = analytics
                            _bookAnalyticsState.value = BookAnalyticsUiState.Success(analytics)
                        } else {
                            val errorMessage = "No analytics data received"
                            _bookAnalyticsState.value = BookAnalyticsUiState.Error(errorMessage)
                            _snackbarEvent.emit("Error: $errorMessage")
                        }
                    } else {
                        val errorMessage = apiResponse.message ?: "Failed to load book analytics"
                        _bookAnalyticsState.value = BookAnalyticsUiState.Error(errorMessage)
                        _snackbarEvent.emit("Error: $errorMessage")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to load book analytics"
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

    // Author Analytics operations
    fun loadAuthorAnalytics() {
        viewModelScope.launch {
            _authorAnalyticsState.value = AuthorAnalyticsUiState.Loading
            try {
                val response = RetrofitInstance.api.getAuthorAnalytics()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val analytics = apiResponse.data
                        if (analytics != null) {
                            _currentAuthorAnalytics.value = analytics
                            _authorAnalyticsState.value = AuthorAnalyticsUiState.Success(analytics)
                        } else {
                            val errorMessage = "No author analytics data received"
                            _authorAnalyticsState.value = AuthorAnalyticsUiState.Error(errorMessage)
                            _snackbarEvent.emit("Error: $errorMessage")
                        }
                    } else {
                        val errorMessage = apiResponse.message ?: "Failed to load author analytics"
                        _authorAnalyticsState.value = AuthorAnalyticsUiState.Error(errorMessage)
                        _snackbarEvent.emit("Error: $errorMessage")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to load author analytics"
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

    // Helper functions for analytics calculations
    fun calculateApprovalRate(approved: Int, total: Int): Double {
        return if (total > 0) (approved.toDouble() / total * 100) else 0.0
    }

    fun calculateRejectionRate(rejected: Int, total: Int): Double {
        return if (total > 0) (rejected.toDouble() / total * 100) else 0.0
    }

    fun getRatingDistributionList(ratingDistribution: RatingDistribution): List<Pair<Int, Int>> {
        return listOf(
            1 to ratingDistribution.`1`,
            2 to ratingDistribution.`2`,
            3 to ratingDistribution.`3`,
            4 to ratingDistribution.`4`,
            5 to ratingDistribution.`5`
        )
    }

    fun getTopRatingCount(ratingDistribution: RatingDistribution): Int {
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
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatMonth(monthString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(monthString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            monthString
        }
    }
}

class AnalyticsViewModelFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
