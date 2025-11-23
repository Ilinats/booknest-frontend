package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.ApiService
import com.example.booknest.network.FriendRequest
import com.example.booknest.network.FriendshipStatus
import com.example.booknest.network.UserData
import com.example.booknest.network.UserActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendViewModel(
    private val apiService: ApiService,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _friends = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friends: StateFlow<List<FriendRequest>> = _friends.asStateFlow()
    
    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()
    
    private val _friendsActivity = MutableStateFlow<List<UserActivity>>(emptyList())
    val friendsActivity: StateFlow<List<UserActivity>> = _friendsActivity.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<UserData>>(emptyList())
    val searchResults: StateFlow<List<UserData>> = _searchResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadFriends() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getFriends()
                if (response.isSuccessful) {
                    response.body()?.data?.let { friendsList ->
                        _friends.value = friendsList
                    }
                } else {
                    _error.value = "Failed to load friends"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadFriendRequests(type: String = "received") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getFriendRequests(type = type)
                if (response.isSuccessful) {
                    response.body()?.data?.let { requestsList ->
                        _friendRequests.value = requestsList
                    }
                } else {
                    _error.value = "Failed to load friend requests"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadFriendsActivity() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getFriendsActivity()
                if (response.isSuccessful) {
                    response.body()?.data?.let { activityList ->
                        _friendsActivity.value = activityList
                    }
                } else {
                    _error.value = "Failed to load friends activity"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.searchUsers(query)
                if (response.isSuccessful) {
                    response.body()?.data?.let { users ->
                        _searchResults.value = users
                    }
                } else {
                    _error.value = "Failed to search users"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sendFriendRequest(username: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.sendFriendRequest(username)
                if (response.isSuccessful) {
                    // Refresh friend requests
                    loadFriendRequests("sent")
                } else {
                    _error.value = "Failed to send friend request"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun acceptFriendRequest(requesterId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.acceptFriendRequest(requesterId)
                if (response.isSuccessful) {
                    // Refresh friends and requests
                    loadFriends()
                    loadFriendRequests("received")
                } else {
                    _error.value = "Failed to accept friend request"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun declineFriendRequest(requesterId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.declineFriendRequest(requesterId)
                if (response.isSuccessful) {
                    // Refresh friend requests
                    loadFriendRequests("received")
                } else {
                    _error.value = "Failed to decline friend request"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun unfriendUser(friendId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.unfriendUser(friendId)
                if (response.isSuccessful) {
                    // Refresh friends
                    loadFriends()
                } else {
                    _error.value = "Failed to unfriend user"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun blockUser(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.blockUser(userId)
                if (response.isSuccessful) {
                    // Refresh friends
                    loadFriends()
                } else {
                    _error.value = "Failed to block user"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun unblockUser(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.unblockUser(userId)
                if (response.isSuccessful) {
                    // Refresh friends
                    loadFriends()
                } else {
                    _error.value = "Failed to unblock user"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getFriendshipStatus(userId: String, onResult: (FriendshipStatus?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.getFriendshipStatus(userId)
                if (response.isSuccessful) {
                    onResult(response.body()?.data)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

class FriendViewModelFactory(
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendViewModel(
                apiService = authManager.apiService,
                authManager = authManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
