package com.example.booknest.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoadingUser = MutableStateFlow(false)
    val isLoadingUser: StateFlow<Boolean> = _isLoadingUser.asStateFlow()

    private val _userLoadError = MutableStateFlow<String?>(null)
    val userLoadError: StateFlow<String?> = _userLoadError.asStateFlow()

    private val _fetchedUserType = MutableStateFlow<String?>(null)

    val effectiveUserType: StateFlow<String?> = combine(
        sessionManager.currentUser,
        _fetchedUserType
    ) { user, fetched -> user?.userType ?: fetched }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isAuthor: StateFlow<Boolean> = combine(
        sessionManager.currentUser,
        _fetchedUserType
    ) { user, fetched -> (user?.userType ?: fetched)?.lowercase() == "author" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Resolves the user type from session. Fetches from the API if not yet loaded.
     * Call this once when the main screen becomes active.
     */
    fun resolveUserType() {
        viewModelScope.launch {
            if (sessionManager.currentUser.value == null && _fetchedUserType.value == null) {
                val fetched = sessionManager.fetchUserType()
                if (fetched.isNotEmpty()) {
                    _fetchedUserType.value = fetched
                }
            }
            if (sessionManager.currentUser.value == null) {
                fetchCurrentUser()
            }
        }
    }

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

