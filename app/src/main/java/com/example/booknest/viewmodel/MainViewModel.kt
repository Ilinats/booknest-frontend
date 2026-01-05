package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoadingUser = MutableStateFlow(false)
    val isLoadingUser: StateFlow<Boolean> = _isLoadingUser.asStateFlow()

    private val _userLoadError = MutableStateFlow<String?>(null)
    val userLoadError: StateFlow<String?> = _userLoadError.asStateFlow()

    /**
     * Fetches the current user from the API and updates the SessionManager.
     * This should be called when the user is logged in but currentUser is null.
     */
    fun fetchCurrentUser() {
        viewModelScope.launch {
            val token = sessionManager.getToken()
            if (token.isEmpty()) {
                _userLoadError.value = "No authentication token available"
                return@launch
            }

            val currentUser = sessionManager.currentUser.value
            if (currentUser != null) {
                // User already loaded, no need to fetch
                return@launch
            }

            _isLoadingUser.value = true
            _userLoadError.value = null

            try {
                val result = getCurrentUserUseCase()
                result
                    .onSuccess { user ->
                        sessionManager.updateUser(user)
                        _isLoadingUser.value = false
                    }
                    .onFailure { error ->
                        _userLoadError.value = error.message ?: "Failed to fetch user"
                        _isLoadingUser.value = false
                    }
            } catch (e: Exception) {
                _userLoadError.value = e.message ?: "Unknown error occurred"
                _isLoadingUser.value = false
            }
        }
    }
}

