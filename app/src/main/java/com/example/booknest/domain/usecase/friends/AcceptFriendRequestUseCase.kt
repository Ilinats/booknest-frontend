package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.repository.FriendsRepository

class AcceptFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(requesterId: String): Result<FriendRequestResponse> {
        return friendsRepository.acceptFriendRequest(requesterId)
    }
}


