package com.example.booknest.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.PublicUserProfileResponse
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.usecase.profile.GetMyProfileUseCase
import com.example.booknest.domain.usecase.profile.GetPublicUserProfileUseCase
import com.example.booknest.domain.usecase.profile.GetUserProfileUseCase
import com.example.booknest.presentation.common.UiState
import com.example.booknest.viewmodel.common.RequestGate
import com.example.booknest.viewmodel.common.UserFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val feedback: UserFeedback,
    private val sessionManager: SessionManager,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getPublicUserProfileUseCase: GetPublicUserProfileUseCase,
    profileRefreshBus: ProfileRefreshBus,
) : ViewModel() {

    private val profileLoadGate = RequestGate()

    private val _myProfile = MutableStateFlow<UserProfileResponse?>(null)
    val myProfile: StateFlow<UserProfileResponse?> = _myProfile.asStateFlow()

    private val _currentProfile = MutableStateFlow<UserProfileResponse?>(null)
    val currentProfile: StateFlow<UserProfileResponse?> = _currentProfile.asStateFlow()

    private val _profileState = MutableStateFlow<UiState<UserProfileResponse>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserProfileResponse>> = _profileState.asStateFlow()

    private val _publicProfile = MutableStateFlow<PublicUserProfileResponse?>(null)
    val publicProfile: StateFlow<PublicUserProfileResponse?> = _publicProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

    init {
        viewModelScope.launch {
            profileRefreshBus.refreshRequests.collect {
                loadMyProfile()
            }
        }
    }

    fun loadMyProfile() {
        val token = profileLoadGate.nextToken()
        _profileState.value = UiState.Loading
        viewModelScope.launch {
            val sessionToken = sessionManager.getToken()
            if (sessionToken.isEmpty()) {
                return@launch
            }

            try {
                _isLoading.value = true
                _error.value = null

                getMyProfileUseCase()
                    .onSuccess { profile ->
                        if (!profileLoadGate.isCurrent(token)) return@onSuccess
                        onProfileLoaded(profile)
                    }
                    .onFailure { e ->
                        if (!profileLoadGate.isCurrent(token)) return@onFailure
                        handleProfileError(e.message ?: "Failed to load profile")
                    }
            } catch (e: Exception) {
                if (!profileLoadGate.isCurrent(token)) return@launch
                handleProfileError(e.message ?: "Unknown error occurred")
            } finally {
                if (profileLoadGate.isCurrent(token)) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun loadUserProfile(username: String) {
        val token = profileLoadGate.nextToken()
        _profileState.value = UiState.Loading
        _publicProfile.value = null
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                getUserProfileUseCase(username)
                    .onSuccess { publicProfile ->
                        if (!profileLoadGate.isCurrent(token)) return@onSuccess
                        _publicProfile.value = publicProfile
                        val combinedProfile = publicProfile.toFullProfile()
                        _profileState.value = UiState.Success(combinedProfile)
                    }
                    .onFailure { e ->
                        if (!profileLoadGate.isCurrent(token)) return@onFailure
                        handleProfileError(e.message ?: "Failed to load profile")
                    }
            } catch (e: Exception) {
                if (!profileLoadGate.isCurrent(token)) return@launch
                handleProfileError(e.message ?: "Unknown error occurred")
            } finally {
                if (profileLoadGate.isCurrent(token)) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun loadPublicUserProfile(username: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                getPublicUserProfileUseCase(username)
                    .onSuccess { profile ->
                        _publicProfile.value = profile
                    }
                    .onFailure { e ->
                        _publicProfile.value = null
                        _error.value = e.message ?: "Failed to load profile"
                    }
            } catch (e: Exception) {
                _publicProfile.value = null
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun onProfileLoaded(profile: UserProfileResponse) {
        _myProfile.value = profile
        _currentProfile.value = profile
        _profileState.value = UiState.Success(profile)
    }

    private suspend fun handleProfileError(message: String) {
        _profileState.value = UiState.Error(message)
        feedback.error(message, _error)
    }
}
