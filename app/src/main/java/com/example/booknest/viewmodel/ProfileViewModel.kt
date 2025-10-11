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

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileEditUiState {
    object Idle : ProfileEditUiState()
    object Loading : ProfileEditUiState()
    data class Success(val user: UserData) : ProfileEditUiState()
    data class Error(val message: String) : ProfileEditUiState()
}

sealed class StatsUiState {
    object Idle : StatsUiState()
    object Loading : StatsUiState()
    data class Success(val stats: UserStatsResponse) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}

class ProfileViewModel(private val authManager: AuthManager) : ViewModel() {

    // Profile state
    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val profileState: StateFlow<ProfileUiState> = _profileState

    private val _profileEditState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Idle)
    val profileEditState: StateFlow<ProfileEditUiState> = _profileEditState

    private val _statsState = MutableStateFlow<StatsUiState>(StatsUiState.Idle)
    val statsState: StateFlow<StatsUiState> = _statsState

    // Current profile data
    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile

    private val _currentStats = MutableStateFlow<UserStatsResponse?>(null)
    val currentStats: StateFlow<UserStatsResponse?> = _currentStats

    // Common state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    // Profile operations
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            try {
                val response = RetrofitInstance.api.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val profile = apiResponse.data
                        if (profile != null) {
                            _currentProfile.value = profile
                            _profileState.value = ProfileUiState.Success(profile)
                        } else {
                            val errorMessage = "No profile data received"
                            _profileState.value = ProfileUiState.Error(errorMessage)
                            _snackbarEvent.emit("Error: $errorMessage")
                        }
                    } else {
                        val errorMessage = apiResponse.message ?: "Failed to load profile"
                        _profileState.value = ProfileUiState.Error(errorMessage)
                        _snackbarEvent.emit("Error: $errorMessage")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to load profile"
                    _profileState.value = ProfileUiState.Error(errorMessage)
                    _snackbarEvent.emit("Error: $errorMessage")
                }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _profileState.value = ProfileUiState.Error(errorMessage)
                _snackbarEvent.emit(errorMessage)
            }
        }
    }

    fun updateProfile(
        firstName: String?,
        lastName: String?,
        birthDate: String?,
        bio: String?,
        avatarUrl: String?
    ) {
        viewModelScope.launch {
            _profileEditState.value = ProfileEditUiState.Loading
            try {
                val updateRequest = UpdateProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    birthDate = birthDate,
                    bio = bio,
                    avatarUrl = avatarUrl
                )
                
                val response = RetrofitInstance.api.updateMyProfile(updateRequest)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val updatedUser = apiResponse.data
                        if (updatedUser != null) {
                            _profileEditState.value = ProfileEditUiState.Success(updatedUser)
                            _snackbarEvent.emit("Profile updated successfully")
                            // Update current user in auth manager
                            authManager.updateCurrentUser(updatedUser)
                        } else {
                            val errorMessage = "No user data received after update"
                            _profileEditState.value = ProfileEditUiState.Error(errorMessage)
                            _snackbarEvent.emit("Error: $errorMessage")
                        }
                    } else {
                        val errorMessage = apiResponse.message ?: "Failed to update profile"
                        _profileEditState.value = ProfileEditUiState.Error(errorMessage)
                        _snackbarEvent.emit("Error: $errorMessage")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to update profile"
                    _profileEditState.value = ProfileEditUiState.Error(errorMessage)
                    _snackbarEvent.emit("Error: $errorMessage")
                }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _profileEditState.value = ProfileEditUiState.Error(errorMessage)
                _snackbarEvent.emit(errorMessage)
            }
        }
    }

    fun loadMyStats() {
        viewModelScope.launch {
            _statsState.value = StatsUiState.Loading
            try {
                val response = RetrofitInstance.api.getMyStats()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val stats = apiResponse.data
                        if (stats != null) {
                            _currentStats.value = stats
                            _statsState.value = StatsUiState.Success(stats)
                        } else {
                            val errorMessage = "No stats data received"
                            _statsState.value = StatsUiState.Error(errorMessage)
                            _snackbarEvent.emit("Error: $errorMessage")
                        }
                    } else {
                        val errorMessage = apiResponse.message ?: "Failed to load stats"
                        _statsState.value = StatsUiState.Error(errorMessage)
                        _snackbarEvent.emit("Error: $errorMessage")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to load stats"
                    _statsState.value = StatsUiState.Error(errorMessage)
                    _snackbarEvent.emit("Error: $errorMessage")
                }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _statsState.value = StatsUiState.Error(errorMessage)
                _snackbarEvent.emit(errorMessage)
            }
        }
    }

    fun loadAuthorStats(authorId: String) {
        viewModelScope.launch {
            _statsState.value = StatsUiState.Loading
            try {
                val response = RetrofitInstance.api.getAuthorStats(authorId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val authorStats = apiResponse.data
                        if (authorStats != null) {
                            // Convert AuthorStatsResponse to UserStatsResponse for consistency
                            val stats = UserStatsResponse(
                                user = authorStats.author,
                                stats = authorStats.stats
                            )
                            _currentStats.value = stats
                            _statsState.value = StatsUiState.Success(stats)
                        } else {
                            val errorMessage = "No author stats data received"
                            _statsState.value = StatsUiState.Error(errorMessage)
                            _snackbarEvent.emit("Error: $errorMessage")
                        }
                    } else {
                        val errorMessage = apiResponse.message ?: "Failed to load author stats"
                        _statsState.value = StatsUiState.Error(errorMessage)
                        _snackbarEvent.emit("Error: $errorMessage")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to load author stats"
                    _statsState.value = StatsUiState.Error(errorMessage)
                    _snackbarEvent.emit("Error: $errorMessage")
                }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _statsState.value = StatsUiState.Error(errorMessage)
                _snackbarEvent.emit(errorMessage)
            }
        }
    }

    fun clearProfileState() {
        _profileState.value = ProfileUiState.Idle
        _currentProfile.value = null
    }

    fun clearEditState() {
        _profileEditState.value = ProfileEditUiState.Idle
    }

    fun clearStatsState() {
        _statsState.value = StatsUiState.Idle
        _currentStats.value = null
    }

    fun getCurrentUser(): UserData? = authManager.getCurrentUser()
}

class ProfileViewModelFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
