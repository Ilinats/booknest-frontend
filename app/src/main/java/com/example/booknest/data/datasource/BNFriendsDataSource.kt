package com.example.booknest.data.datasource

import com.example.booknest.data.service.FriendsService
import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.model.response.FriendshipStatusResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserResponse
class BNFriendsDataSource(private val friendsService: FriendsService) : FriendsDataSource {

    override suspend fun sendFriendRequest(username: String): Result<FriendRequestResponse> {
        return runSuspendRequest { friendsService.sendFriendRequest(username) }
    }

    override suspend fun acceptFriendRequest(requesterId: String): Result<FriendRequestResponse> {
        return runSuspendRequest { friendsService.acceptFriendRequest(requesterId) }
    }

    override suspend fun declineFriendRequest(requesterId: String): Result<Unit> {
        return runSuspendRequestUnit { friendsService.declineFriendRequest(requesterId) }
    }

    override suspend fun cancelFriendRequest(addresseeId: String): Result<Unit> {
        return runSuspendRequestUnit { friendsService.cancelFriendRequest(addresseeId) }
    }

    override suspend fun unfriendUser(friendId: String): Result<Unit> {
        return runSuspendRequestUnit { friendsService.unfriendUser(friendId) }
    }

    override suspend fun getFriends(): Result<List<UserResponse>> {
        return runSuspendRequest { friendsService.getFriends() }
    }

    override suspend fun getSentFriendRequests(): Result<List<UserResponse>> {
        return runSuspendRequest { friendsService.getSentFriendRequests() }
    }

    override suspend fun getReceivedFriendRequests(): Result<List<UserResponse>> {
        return runSuspendRequest { friendsService.getReceivedFriendRequests() }
    }

    override suspend fun getFriendshipStatus(userId: String): Result<FriendshipStatusResponse> {
        return runSuspendRequest { friendsService.getFriendshipStatus(userId) }
    }

    override suspend fun searchUsers(query: String, limit: Int?): Result<List<UserResponse>> {
        return try {
            val response = friendsService.searchUsers(query, limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.map { it.user })
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Throwable(response.errorBody()?.string() ?: "Failed to search users"))
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkOrUnknown(e))
        }
    }

    override suspend fun getFriendsActivity(limit: Int?): Result<List<UserActivityResponse>> {
        return runSuspendRequest { friendsService.getFriendsActivity(limit) }
    }
}
