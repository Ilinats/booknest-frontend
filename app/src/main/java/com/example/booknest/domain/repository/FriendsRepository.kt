package com.example.booknest.domain.repository

import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.model.response.FriendshipStatusResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserResponse

interface FriendsRepository {
    suspend fun sendFriendRequest(username: String): Result<FriendRequestResponse>
    suspend fun acceptFriendRequest(requesterId: String): Result<FriendRequestResponse>
    suspend fun declineFriendRequest(requesterId: String): Result<Unit>
    suspend fun cancelFriendRequest(addresseeId: String): Result<Unit>
    suspend fun unfriendUser(friendId: String): Result<Unit>
    suspend fun getFriends(): Result<List<UserResponse>>
    suspend fun getSentFriendRequests(): Result<List<UserResponse>>
    suspend fun getReceivedFriendRequests(): Result<List<UserResponse>>
    suspend fun getFriendshipStatus(userId: String): Result<FriendshipStatusResponse>
    suspend fun searchUsers(query: String, limit: Int? = 20): Result<List<UserResponse>>
    suspend fun getFriendsActivity(limit: Int? = 50): Result<List<UserActivityResponse>>
}
