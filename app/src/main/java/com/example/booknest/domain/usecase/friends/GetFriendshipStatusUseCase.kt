package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.model.response.FriendshipStatusResponse
import com.example.booknest.domain.repository.FriendsRepository

class GetFriendshipStatusUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(userId: String): Result<FriendshipStatusResponse> {
        return friendsRepository.getFriendshipStatus(userId)
    }
}


