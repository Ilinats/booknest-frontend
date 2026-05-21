package com.example.booknest.viewmodel.notifications

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.enums.NotificationType
import com.example.booknest.domain.model.request.RegisterDeviceTokenRequest
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.domain.repository.FriendsRepository
import com.example.booknest.domain.repository.NotificationsRepository
import com.example.booknest.domain.usecase.friends.AcceptFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.DeclineFriendRequestUseCase
import com.example.booknest.domain.usecase.notifications.DeleteAllNotificationsUseCase
import com.example.booknest.domain.usecase.notifications.DeleteNotificationUseCase
import com.example.booknest.domain.usecase.notifications.GetNotificationsUseCase
import com.example.booknest.domain.usecase.notifications.GetUnreadCountUseCase
import com.example.booknest.domain.usecase.notifications.MarkAllNotificationsAsReadUseCase
import com.example.booknest.domain.usecase.notifications.MarkNotificationAsReadUseCase
import com.example.booknest.domain.usecase.notifications.RegisterDeviceTokenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val feedback: UserFeedback,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val getUnreadCountUseCase: GetUnreadCountUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val deleteAllNotificationsUseCase: DeleteAllNotificationsUseCase,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val declineFriendRequestUseCase: DeclineFriendRequestUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notifications: StateFlow<List<NotificationResponse>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun notifyError(message: String) = feedback.error(message, _error)

    private var hasMore = true

    private val _processingNotifications = MutableStateFlow<Set<String>>(emptySet())
    val processingNotifications: StateFlow<Set<String>> = _processingNotifications.asStateFlow()

    /** Refetch from API and merge (e.g. when the screen becomes visible again). */
    fun refreshNotifications() {
        loadNotifications(refresh = true)
    }

    fun loadNotifications(refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val isLoggedIn = sessionManager.isLoggedIn.value == true
                if (!isLoggedIn) {
                    return@launch
                }

                val token = sessionManager.getToken()
                if (token.isEmpty()) {
                    kotlinx.coroutines.delay(200)
                    if (sessionManager.getToken().isEmpty()) {
                        return@launch
                    }
                }

                if (!refresh && _notifications.value.isNotEmpty()) {
                    return@launch
                }

                _isLoading.value = true
                _error.value = null

                // Omit unreadOnly (backend returns read + unread). Unread tab filters client-side.
                val result = getNotificationsUseCase(
                    unreadOnly = null,
                    skip = 0,
                    take = NOTIFICATIONS_PAGE_SIZE,
                )
                result
                    .onSuccess { notificationsResponse ->
                        _notifications.value = mergeNotifications(
                            current = _notifications.value,
                            fetched = notificationsResponse.notifications,
                        )
                        hasMore = notificationsResponse.hasMore == true
                        loadUnreadCount()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to load notifications")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val isLoggedIn = sessionManager.isLoggedIn.value == true
                if (!isLoggedIn) {
                    return@launch
                }

                val token = sessionManager.getToken()
                if (token.isEmpty()) {
                    kotlinx.coroutines.delay(200)
                    if (sessionManager.getToken().isEmpty()) {
                        return@launch
                    }
                }

                val result = getUnreadCountUseCase()
                result
                    .onSuccess { unread ->
                        _unreadCount.value = unread.count
                    }
            } catch (e: Exception) {
                // silently ignore unread count load failures
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val notification = _notifications.value.find { it.id == notificationId } ?: return@launch
                if (notification.isRead) return@launch

                val wasUnread = true
                val previous = notification

                _notifications.value = _notifications.value.map { n ->
                    if (n.id == notificationId) {
                        n.copy(
                            isRead = true,
                            readAt = java.time.Instant.now().toString()
                        )
                    } else {
                        n
                    }
                }
                _unreadCount.value = maxOf(0, _unreadCount.value - 1)

                val result = markNotificationAsReadUseCase(notificationId)
                result
                    .onSuccess { updated ->
                        _notifications.value = _notifications.value.map { n ->
                            if (n.id == notificationId) {
                                updated.copy(isRead = true)
                            } else {
                                n
                            }
                        }
                        loadUnreadCount()
                    }
                    .onFailure { e ->
                        _notifications.value = _notifications.value.map { n ->
                            if (n.id == notificationId) previous else n
                        }
                        if (wasUnread) {
                            _unreadCount.value = _unreadCount.value + 1
                        }
                        notifyError(e.message ?: "Failed to mark notification as read")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = markAllNotificationsAsReadUseCase()
                result
                    .onSuccess {
                        _notifications.value = _notifications.value.map { notification ->
                            notification.copy(
                                isRead = true,
                                readAt = java.time.Instant.now().toString()
                            )
                        }
                        _unreadCount.value = 0
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to mark all notifications as read")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
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

                val result = deleteNotificationUseCase(notificationId)
                result
                    .onSuccess {
                        _notifications.value =
                            _notifications.value.filter { it.id != notificationId }
                        if (wasUnread) {
                            _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                        }
                        loadUnreadCount()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to delete notification")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val unreadCountBefore = _notifications.value.count { !it.isRead }
                val result = deleteAllNotificationsUseCase()
                result
                    .onSuccess {
                        _notifications.value = emptyList()
                        _unreadCount.value = 0
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to delete all notifications")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptFriendRequest(requesterId: String, notificationId: String) {
        viewModelScope.launch {
            try {
                _processingNotifications.value = _processingNotifications.value + notificationId

                val notification = _notifications.value.find { it.id == notificationId }
                if (notification?.type != NotificationType.FRIEND_REQUEST_RECEIVED) {
                    if (notification != null && !notification.isRead) markAsRead(notificationId)
                    _processingNotifications.value =
                        _processingNotifications.value - notificationId
                    return@launch
                }

                val wasUnread = !notification.isRead
                val result = acceptFriendRequestUseCase(requesterId)
                result
                    .onSuccess {
                        removeIncomingFriendRequestNotification(notificationId, wasUnread)
                        _processingNotifications.value =
                            _processingNotifications.value - notificationId
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to accept friend request")
                        _processingNotifications.value =
                            _processingNotifications.value - notificationId
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
                _processingNotifications.value = _processingNotifications.value - notificationId
            }
        }
    }

    fun declineFriendRequest(requesterId: String, notificationId: String) {
        viewModelScope.launch {
            try {
                _processingNotifications.value = _processingNotifications.value + notificationId

                val notification = _notifications.value.find { it.id == notificationId }
                if (notification?.type != NotificationType.FRIEND_REQUEST_RECEIVED) {
                    if (notification != null && !notification.isRead) markAsRead(notificationId)
                    _processingNotifications.value =
                        _processingNotifications.value - notificationId
                    return@launch
                }

                val wasUnread = !notification.isRead
                val result = declineFriendRequestUseCase(requesterId)
                result
                    .onSuccess {
                        removeIncomingFriendRequestNotification(notificationId, wasUnread)
                        _processingNotifications.value =
                            _processingNotifications.value - notificationId
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to decline friend request")
                        _processingNotifications.value =
                            _processingNotifications.value - notificationId
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
                _processingNotifications.value = _processingNotifications.value - notificationId
            }
        }
    }

    private fun mergeNotifications(
        current: List<NotificationResponse>,
        fetched: List<NotificationResponse>,
    ): List<NotificationResponse> {
        if (current.isEmpty()) return fetched
        val fetchedIds = fetched.map { it.id }.toSet()
        // Prefer server rows; keep local unread-only rows during in-flight mark-read.
        val pendingLocal = current.filter { it.id !in fetchedIds && !it.isRead }
        return (fetched + pendingLocal).sortedByDescending { it.createdAt }
    }

    /** Incoming friend-request rows are removed after Accept/Decline; all other types stay in All. */
    private suspend fun removeIncomingFriendRequestNotification(
        notificationId: String,
        wasUnread: Boolean,
    ) {
        deleteNotificationUseCase(notificationId)
            .onSuccess {
                _notifications.value =
                    _notifications.value.filter { it.id != notificationId }
                if (wasUnread) {
                    _unreadCount.value = maxOf(0, _unreadCount.value - 1)
                }
                loadUnreadCount()
            }
    }

    private companion object {
        /** Backend default take is 20; request enough for the in-app list. */
        const val NOTIFICATIONS_PAGE_SIZE = 100
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
                val result = registerDeviceTokenUseCase(request)
                result
                    .onFailure { _ -> }
            } catch (e: Exception) {
                // silently ignore device token registration failures
            }
        }
    }
}
