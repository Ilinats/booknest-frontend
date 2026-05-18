package com.example.booknest.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.domain.usecase.profile.GetAuthorStatsUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileStatsViewModel(
    private val getMyStatsUseCase: GetMyStatsUseCase,
    private val getAuthorStatsUseCase: GetAuthorStatsUseCase
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
                        _currentStats.value = stats
                        _statsState.value = UiState.Success(stats)
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
}
