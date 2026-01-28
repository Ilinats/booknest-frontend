package com.example.booknest.data.repository

import com.example.booknest.data.datasource.FriendsDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.model.response.FriendshipStatusResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.FriendsRepository

class BNFriendsRepository(private val friendsDataSource: FriendsDataSource) : FriendsRepository {

    override suspend fun sendFriendRequest(username: String): Result<FriendRequestResponse> {
        return resultBody(friendsDataSource.sendFriendRequest(username))
    }

    override suspend fun acceptFriendRequest(requesterId: String): Result<FriendRequestResponse> {
        return resultBody(friendsDataSource.acceptFriendRequest(requesterId))
    }

    override suspend fun declineFriendRequest(requesterId: String): Result<Unit> {
        return resultBody(friendsDataSource.declineFriendRequest(requesterId))
    }

    override suspend fun cancelFriendRequest(addresseeId: String): Result<Unit> {
        return resultBody(friendsDataSource.cancelFriendRequest(addresseeId))
    }

    override suspend fun unfriendUser(friendId: String): Result<Unit> {
        return resultBody(friendsDataSource.unfriendUser(friendId))
    }

    override suspend fun getFriends(): Result<List<UserResponse>> {
        return resultBody(friendsDataSource.getFriends())
    }

    override suspend fun getSentFriendRequests(): Result<List<UserResponse>> {
        return resultBody(friendsDataSource.getSentFriendRequests())
    }

    override suspend fun getReceivedFriendRequests(): Result<List<UserResponse>> {
        return resultBody(friendsDataSource.getReceivedFriendRequests())
    }

    override suspend fun getFriendshipStatus(userId: String): Result<FriendshipStatusResponse> {
        return resultBody(friendsDataSource.getFriendshipStatus(userId))
    }

    override suspend fun searchUsers(query: String, limit: Int?): Result<List<UserResponse>> {
        return resultBody(friendsDataSource.searchUsers(query, limit))
    }

    override suspend fun getFriendsActivity(limit: Int?): Result<List<UserActivityResponse>> {
        return resultBody(friendsDataSource.getFriendsActivity(limit))
    }
}

