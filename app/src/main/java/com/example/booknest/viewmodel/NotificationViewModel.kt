package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.ApiService
import com.example.booknest.network.Notification
import com.example.booknest.network.RegisterDeviceTokenRequest
import com.example.booknest.network.TokenCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val apiService: ApiService,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentOffset = 0
    private val limit = 50
    private var hasMore = true
    
    private val _processingNotifications = MutableStateFlow<Set<String>>(emptySet())
    val processingNotifications: StateFlow<Set<String>> = _processingNotifications.asStateFlow()
    
    fun loadNotifications(unreadOnly: Boolean = false, refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                // Check if user is logged in and token is available
                val isLoggedIn = authManager.isLoggedIn.first()
                if (!isLoggedIn) {
                    println("DEBUG: Cannot load notifications - user not logged in")
                    return@launch
                }
                
                // Get token directly from authManager to ensure it's available
                var token = authManager.getAccessToken()
                if (token == null) {
                    println("DEBUG: Cannot load notifications - token is null")
                    // Wait a bit and try again (token might still be loading)
                    kotlinx.coroutines.delay(200)
                    token = authManager.getAccessToken()
                    if (token == null) {
                        println("DEBUG: Token still null after retry")
                        return@launch
                    }
                }
                
                // Ensure TokenCache is also updated
                if (TokenCache.accessToken != token) {
                    TokenCache.accessToken = token
                }
                
                _isLoading.value = true
                _error.value = null
                
                if (refresh) {
                    currentOffset = 0
                    hasMore = true
                }
                
                val response = apiService.getNotifications(
                    limit = limit,
                    offset = currentOffset,
                    unreadOnly = unreadOnly
                )
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val notificationsResponse = apiResponse.data
                        if (refresh) {
                            _notifications.value = notificationsResponse.notifications
                        } else {
                            _notifications.value = _notifications.value + notificationsResponse.notifications
                        }
                        currentOffset += notificationsResponse.notifications.size
                        hasMore = notificationsResponse.notifications.size >= limit
                    } else {
                        _error.value = apiResponse?.message ?: "Failed to load notifications"
                    }
                } else {
                    _error.value = "Failed to load notifications: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                // Check if user is logged in
                val isLoggedIn = authManager.isLoggedIn.first()
                if (!isLoggedIn) {
                    println("DEBUG: Cannot load unread count - user not logged in")
                    return@launch
                }
                
                // Get token directly from authManager to ensure it's available
                var token = authManager.getAccessToken()
                if (token == null) {
                    // Wait a bit and try again (token might still be loading)
                    kotlinx.coroutines.delay(200)
                    token = authManager.getAccessToken()
                    if (token == null) {
                        println("DEBUG: Cannot load unread count - token is null")
                        return@launch
                    }
                }
                
                // Ensure TokenCache is also updated
                if (TokenCache.accessToken != token) {
                    TokenCache.accessToken = token
                }
                
                val response = apiService.getUnreadCount()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _unreadCount.value = apiResponse.data?.count ?: 0
                    }
                }
            } catch (e: Exception) {
                // Silently fail for unread count
                println("DEBUG: Error loading unread count: ${e.message}")
            }
        }
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val notification = _notifications.value.find { it.id == notificationId }
                val wasUnread = notification?.isRead == false
                
                val response = apiService.markNotificationAsRead(notificationId)
                if (response.isSuccessful) {
                    // Update local state
                    _notifications.value = _notifications.value.map { notification ->
                        if (notification.id == notificationId) {
                            response.body()?.data ?: notification
                        } else {
                            notification
                        }
                    }
                    // Update unread count locally if it was unread
                    if (wasUnread) {
                        _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                    }
                    // Also refresh from server to ensure accuracy
                    loadUnreadCount()
                } else {
                    _error.value = "Failed to mark notification as read"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            }
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = apiService.markAllNotificationsAsRead()
                if (response.isSuccessful) {
                    // Update all notifications to read
                    _notifications.value = _notifications.value.map { notification ->
                        notification.copy(isRead = true, readAt = java.time.Instant.now().toString())
                    }
                    _unreadCount.value = 0
                } else {
                    _error.value = "Failed to mark all notifications as read"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val notification = _notifications.value.find { it.id == notificationId }
                val wasUnread = notification?.isRead == false
                
                val response = apiService.deleteNotification(notificationId)
                if (response.isSuccessful) {
                    _notifications.value = _notifications.value.filter { it.id != notificationId }
                    // Update unread count locally if it was unread
                    if (wasUnread) {
                        _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                    }
                    // Also refresh from server to ensure accuracy
                    loadUnreadCount()
                } else {
                    _error.value = "Failed to delete notification"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            }
        }
    }
    
    fun acceptFriendRequest(requesterId: String, notificationId: String) {
        viewModelScope.launch {
            try {
                // Mark as processing
                _processingNotifications.value = _processingNotifications.value + notificationId
                
                // Check if notification was unread before deleting
                val notification = _notifications.value.find { it.id == notificationId }
                val wasUnread = notification?.isRead == false
                
                val response = apiService.acceptFriendRequest(requesterId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success != false) {
                        // Delete the notification after accepting
                        val deleteResponse = apiService.deleteNotification(notificationId)
                        if (deleteResponse.isSuccessful) {
                            // Remove from list and clear processing state
                            _notifications.value = _notifications.value.filter { it.id != notificationId }
                            _processingNotifications.value = _processingNotifications.value - notificationId
                            // Update unread count locally if it was unread
                            if (wasUnread) {
                                _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                            }
                            // Also refresh from server to ensure accuracy
                            loadUnreadCount()
                        } else {
                            // Even if delete fails, remove from list since friend request was accepted
                            _notifications.value = _notifications.value.filter { it.id != notificationId }
                            _processingNotifications.value = _processingNotifications.value - notificationId
                            // Update unread count locally if it was unread
                            if (wasUnread) {
                                _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                            }
                            loadUnreadCount()
                        }
                    } else {
                        _error.value = apiResponse?.message ?: "Failed to accept friend request"
                        // Remove from processing on error
                        _processingNotifications.value = _processingNotifications.value - notificationId
                    }
                } else {
                    _error.value = "Failed to accept friend request: ${response.code()}"
                    // Remove from processing on error
                    _processingNotifications.value = _processingNotifications.value - notificationId
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                // Remove from processing on error
                _processingNotifications.value = _processingNotifications.value - notificationId
            }
        }
    }
    
    fun declineFriendRequest(requesterId: String, notificationId: String) {
        viewModelScope.launch {
            try {
                // Mark as processing
                _processingNotifications.value = _processingNotifications.value + notificationId
                
                // Check if notification was unread before deleting
                val notification = _notifications.value.find { it.id == notificationId }
                val wasUnread = notification?.isRead == false
                
                val response = apiService.declineFriendRequest(requesterId)
                if (response.isSuccessful) {
                    // Delete the notification after declining
                    val deleteResponse = apiService.deleteNotification(notificationId)
                    if (deleteResponse.isSuccessful) {
                        // Remove from list and clear processing state
                        _notifications.value = _notifications.value.filter { it.id != notificationId }
                        _processingNotifications.value = _processingNotifications.value - notificationId
                        // Update unread count locally if it was unread
                        if (wasUnread) {
                            _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                        }
                        // Also refresh from server to ensure accuracy
                        loadUnreadCount()
                    } else {
                        // Even if delete fails, remove from list since friend request was declined
                        _notifications.value = _notifications.value.filter { it.id != notificationId }
                        _processingNotifications.value = _processingNotifications.value - notificationId
                        // Update unread count locally if it was unread
                        if (wasUnread) {
                            _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                        }
                        loadUnreadCount()
                    }
                } else {
                    _error.value = "Failed to decline friend request: ${response.code()}"
                    // Remove from processing on error
                    _processingNotifications.value = _processingNotifications.value - notificationId
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                // Remove from processing on error
                _processingNotifications.value = _processingNotifications.value - notificationId
            }
        }
    }
    
    fun registerDeviceToken(token: String, deviceId: String? = null, appVersion: String? = null) {
        viewModelScope.launch {
            try {
                val request = RegisterDeviceTokenRequest(
                    token = token,
                    deviceType = "android",
                    deviceId = deviceId,
                    appVersion = appVersion
                )
                val response = apiService.registerDeviceToken(request)
                if (response.isSuccessful) {
                    // Token registered successfully
                } else {
                    println("Failed to register device token: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Error registering device token: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

class NotificationViewModelFactory(
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(
                apiService = authManager.apiService,
                authManager = authManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

