package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val apiService: ApiService,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _myProfile = MutableStateFlow<UserProfile?>(null)
    val myProfile: StateFlow<UserProfile?> = _myProfile.asStateFlow()
    
    private val _myActivity = MutableStateFlow<List<UserActivity>>(emptyList())
    val myActivity: StateFlow<List<UserActivity>> = _myActivity.asStateFlow()
    
    private val _myPublicActivity = MutableStateFlow<List<UserActivity>>(emptyList())
    val myPublicActivity: StateFlow<List<UserActivity>> = _myPublicActivity.asStateFlow()
    
    private val _myRecentActivity = MutableStateFlow<List<UserActivity>>(emptyList())
    val myRecentActivity: StateFlow<List<UserActivity>> = _myRecentActivity.asStateFlow()
    
    private val _activityStats = MutableStateFlow<ActivityStats?>(null)
    val activityStats: StateFlow<ActivityStats?> = _activityStats.asStateFlow()
    
    private val _publicProfile = MutableStateFlow<PublicUserProfile?>(null)
    val publicProfile: StateFlow<PublicUserProfile?> = _publicProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadMyProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getMyProfile()
                if (response.isSuccessful) {
                    _myProfile.value = response.body()?.data
                } else {
                    _error.value = "Failed to load profile"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
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
    
    fun updateMyProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.updateMyProfile(profile)
                if (response.isSuccessful) {
                    _myProfile.value = response.body()?.data
                } else {
                    _error.value = "Failed to update profile"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSocialMedia(socialMedia: com.example.booknest.network.SocialMedia) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val request = com.example.booknest.network.UpdateSocialMediaRequest(
                    instagram = socialMedia.instagram,
                    tiktok = socialMedia.tiktok,
                    youtube = socialMedia.youtube,
                    goodreads = socialMedia.goodreads,
                    custom = socialMedia.custom
                )
                val response = apiService.updateSocialMedia(request)
                if (response.isSuccessful) {
                    _myProfile.value = response.body()?.data
                } else {
                    _error.value = "Failed to update social media"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
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
                if (response.isSuccessful) {
                    _myProfile.value = response.body()?.data
                } else {
                    _error.value = "Failed to update privacy settings"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateNotificationSettings(
        notificationsEnabled: Boolean? = null,
        emailNotifications: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val request = UpdateNotificationRequest(
                    notificationsEnabled = notificationsEnabled,
                    emailNotifications = emailNotifications
                )
                val response = apiService.updateNotificationSettings(request)
                if (response.isSuccessful) {
                    _myProfile.value = response.body()?.data
                } else {
                    _error.value = "Failed to update notification settings"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

class ProfileViewModelFactory(
    private val authManager: AuthManager
) {
    fun create(): ProfileViewModel {
        return ProfileViewModel(
            apiService = authManager.apiService,
            authManager = authManager
        )
    }
}