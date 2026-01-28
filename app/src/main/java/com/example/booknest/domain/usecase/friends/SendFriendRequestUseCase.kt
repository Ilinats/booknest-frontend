package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.repository.FriendsRepository

class SendFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(username: String): Result<FriendRequestResponse> {
        return friendsRepository.sendFriendRequest(username)
    }
}


