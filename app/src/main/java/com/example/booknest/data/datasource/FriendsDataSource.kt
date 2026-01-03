package com.example.booknest.data.datasource

import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.model.response.FriendshipStatusResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserResponse

interface FriendsDataSource {
    suspend fun sendFriendRequest(username: String): Result<FriendRequestResponse>
    suspend fun acceptFriendRequest(requesterId: String): Result<FriendRequestResponse>
    suspend fun declineFriendRequest(requesterId: String): Result<Unit>
    suspend fun cancelFriendRequest(addresseeId: String): Result<Unit>
    suspend fun unfriendUser(friendId: String): Result<Unit>
    suspend fun blockUser(userId: String): Result<FriendRequestResponse>
    suspend fun unblockUser(userId: String): Result<Unit>
    suspend fun getFriends(): Result<List<UserResponse>>
    suspend fun getSentFriendRequests(): Result<List<UserResponse>>
    suspend fun getReceivedFriendRequests(): Result<List<UserResponse>>
    suspend fun getFriendshipStatus(userId: String): Result<FriendshipStatusResponse>
    suspend fun searchUsers(query: String, limit: Int?): Result<List<UserResponse>>
    suspend fun getFriendSuggestions(limit: Int?): Result<List<UserResponse>>
    suspend fun getFriendsActivity(limit: Int?): Result<List<UserActivityResponse>>
}

