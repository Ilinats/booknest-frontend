package com.example.booknest.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.error.BNError
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.usecase.profile.GetMyRecentActivityUseCase
import com.example.booknest.domain.usecase.profile.GetUserRecentActivityUseCase
import com.example.booknest.viewmodel.common.UserFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileActivityViewModel(
    private val feedback: UserFeedback,
    private val getMyRecentActivityUseCase: GetMyRecentActivityUseCase,
    private val getUserRecentActivityUseCase: GetUserRecentActivityUseCase,
) : ViewModel() {

    private val _myRecentActivity = MutableStateFlow<List<UserActivityResponse>>(emptyList())
    val myRecentActivity: StateFlow<List<UserActivityResponse>> = _myRecentActivity.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun loadMyRecentActivity(days: Int = 7) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                getMyRecentActivityUseCase(days = days, limit = 50)
                    .onSuccess { activities ->
                        _myRecentActivity.value = activities
                    }
                    .onFailure { e ->
                        feedback.error(e.message ?: "Failed to load recent activity", _error)
                    }
            } catch (e: Exception) {
                feedback.error(e.message ?: "Unknown error occurred", _error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserRecentActivity(username: String, days: Int = 7) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                getUserRecentActivityUseCase(username, days = days, limit = 50)
                    .onSuccess { activities ->
                        _myRecentActivity.value = activities
                    }
                    .onFailure { e ->
                        feedback.error(e.message ?: "Failed to load user activity", _error)
                        if (isHiddenOrForbiddenActivity(e)) {
                            _myRecentActivity.value = emptyList()
                        }
                    }
            } catch (e: Exception) {
                feedback.error(e.message ?: "Unknown error occurred", _error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isHiddenOrForbiddenActivity(throwable: Throwable): Boolean =
        when (throwable) {
            is BNError.Unauthorized -> true
            is BNError.Generic ->
                throwable.statusCode == 403 || throwable.statusCode == 401
            else -> false
        }
}
