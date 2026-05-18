package com.example.booknest.viewmodel.author

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.domain.usecase.applications.GetOverdueReviewsUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.domain.usecase.reviews.GetAuthorLatestReviewsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorDashboardViewModel(
    private val feedback: UserFeedback,
    private val getMyStatsUseCase: GetMyStatsUseCase,
    private val getAuthorLatestReviewsUseCase: GetAuthorLatestReviewsUseCase,
    private val getOverdueReviewsUseCase: GetOverdueReviewsUseCase
) : ViewModel() {

    data class QuickStats(
        val totalBooks: Int = 0,
        val activeBooks: Int = 0,
        val totalApplications: Int = 0,
        val pendingApplications: Int = 0,
        val applicationsThisMonth: Int = 0,
        val avgResponseTime: String = "0 days",
        val totalReviews: Int = 0,
        val averageRating: Double = 0.0,
        val approvalRate: Int = 0
    )

    private val _quickStats = MutableStateFlow(QuickStats())
    val quickStats: StateFlow<QuickStats> = _quickStats.asStateFlow()

    private val _authorStats = MutableStateFlow<UserStatsResponse?>(null)
    val authorStats: StateFlow<UserStatsResponse?> = _authorStats.asStateFlow()

    private val _recentReviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val recentReviews: StateFlow<List<ReviewResponse>> = _recentReviews.asStateFlow()

    private val _overdueReviews = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val overdueReviews: StateFlow<List<ApplicationResponse>> = _overdueReviews.asStateFlow()

    private val _isLoadingStats = MutableStateFlow(false)
    val isLoadingStats: StateFlow<Boolean> = _isLoadingStats.asStateFlow()

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    private fun notifyError(message: String) = feedback.error(message, _error)

    fun loadAuthorStats() {
        viewModelScope.launch {
            try {
                _isLoadingStats.value = true
                val result = getMyStatsUseCase()
                result
                    .onSuccess { stats ->
                        _authorStats.value = stats
                        updateQuickStatsFromStats(stats)
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to load stats") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error loading stats")
            } finally {
                _isLoadingStats.value = false
            }
        }
    }

    fun loadRecentReviews() {
        viewModelScope.launch {
            try {
                _isLoadingReviews.value = true
                val result = getAuthorLatestReviewsUseCase(limit = 3)
                result
                    .onSuccess { reviews -> _recentReviews.value = reviews }
                    .onFailure { e -> notifyError(e.message ?: "Failed to load reviews") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error loading reviews")
            } finally {
                _isLoadingReviews.value = false
            }
        }
    }

    fun loadOverdueReviews() {
        viewModelScope.launch {
            try {
                val result = getOverdueReviewsUseCase()
                result
                    .onSuccess { applications -> _overdueReviews.value = applications }
                    .onFailure { _overdueReviews.value = emptyList() }
            } catch (e: Exception) {
                _overdueReviews.value = emptyList()
            }
        }
    }

    private fun updateQuickStatsFromStats(stats: UserStatsResponse) {
        val statsData = stats.stats
        val avgResponseTime = statsData.averageResponseTime?.let {
            if (it < 1) "${(it * 24).toInt()} hours" else "${it.toInt()} days"
        } ?: "0 days"
        _quickStats.value = QuickStats(
            totalBooks = statsData.totalBooks ?: 0,
            activeBooks = statsData.publishedBooks ?: 0,
            totalApplications = statsData.totalApplications,
            pendingApplications = statsData.pendingApplications,
            applicationsThisMonth = statsData.applicationsThisMonth ?: 0,
            avgResponseTime = avgResponseTime,
            totalReviews = statsData.totalReviews ?: 0,
            averageRating = statsData.averageRating ?: 0.0,
            approvalRate = statsData.approvalRate ?: (if (statsData.totalApplications > 0) {
                ((statsData.approvedApplications.toDouble() / statsData.totalApplications) * 100).toInt()
            } else 0)
        )
    }
}
