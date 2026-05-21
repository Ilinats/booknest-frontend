package com.example.booknest.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.domain.usecase.author.GetMyBooksUseCase
import com.example.booknest.domain.usecase.profile.GetAuthorStatsUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.presentation.common.UiState
import com.example.booknest.viewmodel.author.withBookStatusCountsFrom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileStatsViewModel(
    private val getMyStatsUseCase: GetMyStatsUseCase,
    private val getAuthorStatsUseCase: GetAuthorStatsUseCase,
    private val getMyBooksUseCase: GetMyBooksUseCase,
) : ViewModel() {

    private val _statsState = MutableStateFlow<UiState<UserStatsResponse>>(UiState.Idle)
    val statsState: StateFlow<UiState<UserStatsResponse>> = _statsState.asStateFlow()

    private val _currentStats = MutableStateFlow<UserStatsResponse?>(null)
    val currentStats: StateFlow<UserStatsResponse?> = _currentStats.asStateFlow()

    fun loadMyStats() {
        viewModelScope.launch {
            _statsState.value = UiState.Loading
            try {
                val result = getMyStatsUseCase()
                result
                    .onSuccess { stats ->
                        val adjusted = adjustAuthorBookStatusCounts(stats)
                        _currentStats.value = adjusted
                        _statsState.value = UiState.Success(adjusted)
                    }
                    .onFailure { e ->
                        _statsState.value = UiState.Error(e.message ?: "Failed to load stats")
                    }
            } catch (e: Exception) {
                _statsState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadAuthorStats(authorId: String) {
        viewModelScope.launch {
            _statsState.value = UiState.Loading
            try {
                val result = getAuthorStatsUseCase(authorId)
                result
                    .onSuccess { stats ->
                        val userStats = UserStatsResponse(user = stats.author, stats = stats.stats)
                        _currentStats.value = userStats
                        _statsState.value = UiState.Success(userStats)
                    }
                    .onFailure { e ->
                        _statsState.value = UiState.Error(e.message ?: "Failed to load stats")
                    }
            } catch (e: Exception) {
                _statsState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private suspend fun adjustAuthorBookStatusCounts(stats: UserStatsResponse): UserStatsResponse {
        if (stats.stats.userType != "author") return stats
        val books = getMyBooksUseCase().getOrNull() ?: return stats
        return stats.copy(stats = stats.stats.withBookStatusCountsFrom(books))
    }
}
