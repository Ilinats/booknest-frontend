package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.ApiService
import com.example.booknest.network.UserProfile
import com.example.booknest.network.UserActivity
import com.example.booknest.network.ActivityStats
import com.example.booknest.network.PublicUserProfile
import com.example.booknest.network.UpdateSocialMediaRequest
import com.example.booknest.network.UpdatePrivacyRequest
import com.example.booknest.network.UpdateNotificationRequest
import com.example.booknest.network.UpdateProfileRequest
import com.example.booknest.network.UpdateUserProfileRequest
import com.example.booknest.network.NotificationPreferences
import com.example.booknest.network.UserStatsResponse
import com.example.booknest.network.Book
import com.example.booknest.network.RecommendedBook
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val apiService: ApiService,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _myProfile = MutableStateFlow<UserProfile?>(null)
    val myProfile: StateFlow<UserProfile?> = _myProfile.asStateFlow()
    
    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()
    
    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()
    
    private val _myActivity = MutableStateFlow<List<UserActivity>>(emptyList())
    val myActivity: StateFlow<List<UserActivity>> = _myActivity.asStateFlow()
    
    private val _myPublicActivity = MutableStateFlow<List<UserActivity>>(emptyList())
    val myPublicActivity: StateFlow<List<UserActivity>> = _myPublicActivity.asStateFlow()
    
    private val _myRecentActivity = MutableStateFlow<List<UserActivity>>(emptyList())
    val myRecentActivity: StateFlow<List<UserActivity>> = _myRecentActivity.asStateFlow()
    
    private val _activityStats = MutableStateFlow<ActivityStats?>(null)
    val activityStats: StateFlow<ActivityStats?> = _activityStats.asStateFlow()
    
    private val _statsState = MutableStateFlow<StatsUiState>(StatsUiState.Idle)
    val statsState: StateFlow<StatsUiState> = _statsState.asStateFlow()
    
    private val _currentStats = MutableStateFlow<UserStatsResponse?>(null)
    val currentStats: StateFlow<UserStatsResponse?> = _currentStats.asStateFlow()
    
    private val _publicProfile = MutableStateFlow<PublicUserProfile?>(null)
    val publicProfile: StateFlow<PublicUserProfile?> = _publicProfile.asStateFlow()
    
    private val _profileEditState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Idle)
    val profileEditState: StateFlow<ProfileEditUiState> = _profileEditState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()
    
    private val _authorBooks = MutableStateFlow<List<Book>>(emptyList())
    val authorBooks: StateFlow<List<Book>> = _authorBooks.asStateFlow()
    
    private val _authorBooksLoading = MutableStateFlow(false)
    val authorBooksLoading: StateFlow<Boolean> = _authorBooksLoading.asStateFlow()
    
    fun loadMyProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getMyProfile()
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { profile ->
                        onProfileLoaded(profile)
                    } ?: handleProfileError("Profile not found")
                } else {
                    handleProfileError(response.body()?.message ?: "Failed to load profile")
                }
            } catch (e: Exception) {
                handleProfileError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getUserProfile(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { profile ->
                        onProfileLoaded(profile)
                    } ?: handleProfileError("Profile not found")
                } else {
                    handleProfileError(response.body()?.message ?: "Failed to load profile")
                }
            } catch (e: Exception) {
                handleProfileError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadMyActivity() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getMyActivity()
                if (response.isSuccessful) {
                    _myActivity.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Failed to load activity"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadMyPublicActivity() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getMyPublicActivity()
                if (response.isSuccessful) {
                    _myPublicActivity.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Failed to load public activity"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadMyRecentActivity(days: Int = 7) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getMyRecentActivity(days = days)
                if (response.isSuccessful) {
                    _myRecentActivity.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Failed to load recent activity"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadActivityStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getMyActivityStats()
                if (response.isSuccessful) {
                    _activityStats.value = response.body()?.data
                } else {
                    _error.value = "Failed to load activity stats"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadMyStats() {
        viewModelScope.launch {
            _statsState.value = StatsUiState.Loading
            try {
                val response = apiService.getMyStats()
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { stats ->
                        _currentStats.value = stats
                        _statsState.value = StatsUiState.Success(stats)
                    } ?: handleStatsError("Stats not found")
                } else {
                    handleStatsError(response.body()?.message ?: "Failed to load stats")
                }
            } catch (e: Exception) {
                handleStatsError(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun loadAuthorStats(authorId: String) {
        viewModelScope.launch {
            _statsState.value = StatsUiState.Loading
            try {
                val response = apiService.getAuthorStats(authorId)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { stats ->
                        val userStatsResponse = UserStatsResponse(
                            user = stats.author,
                            stats = stats.stats
                        )
                        _currentStats.value = userStatsResponse
                        _statsState.value = StatsUiState.Success(userStatsResponse)
                    } ?: handleStatsError("Stats not found")
                } else {
                    handleStatsError(response.body()?.message ?: "Failed to load stats")
                }
            } catch (e: Exception) {
                handleStatsError(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun loadPublicUserProfile(username: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getPublicUserProfile(username)
                if (response.isSuccessful) {
                    _publicProfile.value = response.body()?.data
                } else {
                    _error.value = "Failed to load user profile"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        birthDate: String? = null,
        bio: String? = null,
        avatarUrl: String? = null
    ) {
        viewModelScope.launch {
            _profileEditState.value = ProfileEditUiState.Loading
            try {
                val request = UpdateProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    birthDate = birthDate,
                    bio = bio,
                    avatarUrl = avatarUrl
                )
                val response = apiService.updateMyProfile(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { profile ->
                        onProfileLoaded(profile)
                        _profileEditState.value = ProfileEditUiState.Success
                        _snackbarEvent.emit("Profile updated successfully")
                    } ?: handleProfileEditError("Profile update returned no data")
                } else {
                    handleProfileEditError(response.body()?.message ?: "Failed to update profile")
                }
            } catch (e: Exception) {
                handleProfileEditError(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun updateSocialMedia(socialMedia: com.example.booknest.network.SocialMedia) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val request = UpdateSocialMediaRequest(
                    instagram = socialMedia.instagram,
                    tiktok = socialMedia.tiktok,
                    youtube = socialMedia.youtube,
                    goodreads = socialMedia.goodreads,
                    custom = socialMedia.custom
                )
                val response = apiService.updateSocialMedia(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { profile ->
                        onProfileLoaded(profile)
                        _snackbarEvent.emit("Social media updated")
                    } ?: emitErrorMessage("Failed to update social media")
                } else {
                    emitErrorMessage(response.body()?.message ?: "Failed to update social media")
                }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updatePrivacySettings(
        activityPrivacy: String? = null,
        profilePrivacy: String? = null,
        readingListPrivacy: String? = null,
        reviewsPrivacy: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val request = UpdatePrivacyRequest(
                    activityPrivacy = activityPrivacy,
                    profilePrivacy = profilePrivacy,
                    readingListPrivacy = readingListPrivacy,
                    reviewsPrivacy = reviewsPrivacy
                )
                val response = apiService.updatePrivacySettings(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { profile ->
                        onProfileLoaded(profile)
                        _snackbarEvent.emit("Privacy settings updated")
                    } ?: emitErrorMessage("Failed to update privacy settings")
                } else {
                    emitErrorMessage(response.body()?.message ?: "Failed to update privacy settings")
                }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateNotificationSettings(
        notificationsEnabled: Boolean? = null,
        emailNotifications: Boolean? = null,
        notificationPreferences: NotificationPreferences? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val request = UpdateNotificationRequest(
                    notificationsEnabled = notificationsEnabled,
                    emailNotifications = emailNotifications,
                    notificationPreferences = notificationPreferences
                )
                val response = apiService.updateNotificationSettings(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { profile ->
                        onProfileLoaded(profile)
                        _snackbarEvent.emit("Notification settings updated")
                    } ?: emitErrorMessage("Failed to update notification settings")
                } else {
                    emitErrorMessage(response.body()?.message ?: "Failed to update notification settings")
                }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateUserProfile(
        firstName: String? = null,
        lastName: String? = null,
        birthDate: String? = null,
        bio: String? = null,
        avatarUrl: String? = null
    ) {
        viewModelScope.launch {
            _profileEditState.value = ProfileEditUiState.Loading
            try {
                val request = UpdateUserProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    birthDate = birthDate,
                    bio = bio,
                    avatarUrl = avatarUrl
                )
                val response = apiService.updateUserProfile(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Reload profile to get updated data
                    loadMyProfile()
                    _profileEditState.value = ProfileEditUiState.Success
                    _snackbarEvent.emit("Account settings updated successfully")
                } else {
                    handleProfileEditError(response.body()?.message ?: "Failed to update account settings")
                }
            } catch (e: Exception) {
                handleProfileEditError(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun loadAuthorBooks(authorId: String, authorName: String?) {
        viewModelScope.launch {
            try {
                _authorBooksLoading.value = true
                _error.value = null
                
                // Use browseBooks without status filter to get all books regardless of status
                // Then filter by authorName since RecommendedBook doesn't have authorId
                val response = apiService.browseBooks(
                    query = null,
                    genreId = null,
                    ageRating = null,
                    distributionType = null,
                    publishedFrom = null,
                    publishedTo = null,
                    skip = null,
                    take = 100, // Get more books to filter
                    status = null // No status filter to get all books
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        // Filter books by author name and convert to Book
                        val filteredBooks = (apiResponse.data ?: emptyList())
                            .filter { recommendedBook ->
                                // Filter by authorName if available, otherwise we'd need authorId
                                authorName?.let { name ->
                                    recommendedBook.authorName?.equals(name, ignoreCase = true) == true
                                } ?: false
                            }
                            .map { recommendedBook ->
                                recommendedBook.toBook()
                            }
                        
                        _authorBooks.value = filteredBooks
                    } else {
                        _error.value = "Failed to load author books: ${apiResponse.message}"
                    }
                } else {
                    _error.value = "Failed to load author books"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                println("Author books exception: ${e.message}")
            } finally {
                _authorBooksLoading.value = false
            }
        }
    }
    
    // Extension function to convert RecommendedBook to Book
    private fun RecommendedBook.toBook(): Book {
        return Book(
            id = this.id,
            authorId = "", // Required but not available in RecommendedBook
            title = this.title,
            shortDescription = null,
            fullDescription = null,
            coverImageUrl = this.coverImageUrl,
            pageCount = null,
            ageRating = null,
            distributionType = null,
            fileUrl = null,
            fileSize = null,
            fileType = null,
            totalCopies = null,
            availableCopies = null,
            applicationDeadline = null,
            reviewDeadlineDays = null,
            selectionCriteria = null,
            selectionMethod = null,
            status = null,
            createdAt = null,
            updatedAt = null,
            publishedAt = this.publishedAt,
            seriesId = null,
            seriesOrder = this.seriesOrder,
            seriesName = this.seriesName,
            authorName = this.authorName,
            author = null,
            rating = this.rating,
            genres = null
        )
    }
    
    private fun onProfileLoaded(profile: UserProfile) {
        _myProfile.value = profile
        _currentProfile.value = profile
        _profileState.value = ProfileUiState.Success(profile)
    }
    
    private suspend fun handleProfileError(message: String) {
        _profileState.value = ProfileUiState.Error(message)
        emitErrorMessage(message)
    }
    
    private suspend fun handleProfileEditError(message: String) {
        _profileEditState.value = ProfileEditUiState.Error(message)
        emitErrorMessage(message)
    }
    
    private suspend fun handleStatsError(message: String) {
        _statsState.value = StatsUiState.Error(message)
        emitErrorMessage(message)
    }
    
    private suspend fun emitErrorMessage(message: String) {
        _error.value = message
        _snackbarEvent.emit(message)
    }
}

class ProfileViewModelFactory(
    private val authManager: AuthManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(
                apiService = authManager.apiService,
                authManager = authManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileEditUiState {
    object Idle : ProfileEditUiState()
    object Loading : ProfileEditUiState()
    object Success : ProfileEditUiState()
    data class Error(val message: String) : ProfileEditUiState()
}

sealed class StatsUiState {
    object Idle : StatsUiState()
    object Loading : StatsUiState()
    data class Success(val stats: UserStatsResponse) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}