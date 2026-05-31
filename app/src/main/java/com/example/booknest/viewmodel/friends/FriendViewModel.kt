package com.example.booknest.viewmodel.friends

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.model.response.FriendshipStatusResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.FriendsRepository
import com.example.booknest.domain.usecase.friends.AcceptFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.CancelFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.DeclineFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.GetFriendsActivityUseCase
import com.example.booknest.domain.usecase.friends.GetFriendsUseCase
import com.example.booknest.domain.usecase.friends.GetFriendshipStatusUseCase
import com.example.booknest.domain.usecase.friends.GetReceivedFriendRequestsUseCase
import com.example.booknest.domain.usecase.friends.GetSentFriendRequestsUseCase
import com.example.booknest.domain.usecase.friends.SearchUsersUseCase
import com.example.booknest.domain.usecase.friends.SendFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.UnfriendUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendViewModel(
    private val feedback: UserFeedback,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val getSentFriendRequestsUseCase: GetSentFriendRequestsUseCase,
    private val getReceivedFriendRequestsUseCase: GetReceivedFriendRequestsUseCase,
    private val getFriendsActivityUseCase: GetFriendsActivityUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val declineFriendRequestUseCase: DeclineFriendRequestUseCase,
    private val cancelFriendRequestUseCase: CancelFriendRequestUseCase,
    private val unfriendUserUseCase: UnfriendUserUseCase,
    private val getFriendshipStatusUseCase: GetFriendshipStatusUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _friends = MutableStateFlow<List<UserResponse>>(emptyList())
    val friends: StateFlow<List<UserResponse>> = _friends.asStateFlow()

    private val _sentRequests = MutableStateFlow<List<UserResponse>>(emptyList())
    val sentRequests: StateFlow<List<UserResponse>> = _sentRequests.asStateFlow()

    private val _receivedRequests = MutableStateFlow<List<UserResponse>>(emptyList())
    val receivedRequests: StateFlow<List<UserResponse>> = _receivedRequests.asStateFlow()

    private val _friendsActivity = MutableStateFlow<List<UserActivityResponse>>(emptyList())
    val friendsActivity: StateFlow<List<UserActivityResponse>> = _friendsActivity.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserResponse>>(emptyList())
    val searchResults: StateFlow<List<UserResponse>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun notifyError(message: String) = feedback.error(message, _error)
    private fun notifySuccess(message: String) = feedback.success(message)

    private val _friendshipStatuses = MutableStateFlow<Map<String, FriendshipStatusResponse?>>(emptyMap())
    val friendshipStatuses: StateFlow<Map<String, FriendshipStatusResponse?>> = _friendshipStatuses.asStateFlow()

    fun loadFriends() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = getFriendsUseCase()
                result
                    .onSuccess { friendsList ->
                        _friends.value = friendsList
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to load friends")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSentRequests() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = getSentFriendRequestsUseCase()
                result
                    .onSuccess { requestsList ->
                        _sentRequests.value = requestsList
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to load sent requests")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadReceivedRequests() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = getReceivedFriendRequestsUseCase()
                result
                    .onSuccess { requestsList ->
                        _receivedRequests.value = requestsList
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to load received requests")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            loadFriends()
            loadSentRequests()
            loadReceivedRequests()
        }
    }

    fun loadFriendsActivity() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = getFriendsActivityUseCase()
                result
                    .onSuccess { activityList ->
                        _friendsActivity.value = activityList
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to load friends activity")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
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
                val result = searchUsersUseCase(query)
                result
                    .onSuccess { users ->
                        _searchResults.value = users
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to search users")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
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
                val result = sendFriendRequestUseCase(username)
                result
                    .onSuccess {
                        notifySuccess("Friend request sent")
                        loadSentRequests()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to send friend request")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
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
                val result = acceptFriendRequestUseCase(requesterId)
                result
                    .onSuccess {
                        notifySuccess("Friend request accepted")
                        loadFriends()
                        loadReceivedRequests()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to accept friend request")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
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
                val result = declineFriendRequestUseCase(requesterId)
                result
                    .onSuccess {
                        notifySuccess("Friend request declined")
                        loadReceivedRequests()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to decline friend request")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelFriendRequest(addresseeId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = cancelFriendRequestUseCase(addresseeId)
                result
                    .onSuccess {
                        notifySuccess("Friend request cancelled")
                        loadSentRequests()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to cancel friend request")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
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
                val result = unfriendUserUseCase(friendId)
                result
                    .onSuccess {
                        notifySuccess("Removed from friends")
                        loadFriends()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to unfriend user")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFriendshipStatus(userId: String) {
        viewModelScope.launch {
            try {
                val result = getFriendshipStatusUseCase(userId)
                _friendshipStatuses.value = _friendshipStatuses.value + (userId to result.getOrNull())
            } catch (e: Exception) {
                _friendshipStatuses.value = _friendshipStatuses.value + (userId to null)
            }
        }
    }
}
