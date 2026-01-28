package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.repository.FriendsRepository

class CancelFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(addresseeId: String): Result<Unit> {
        return friendsRepository.cancelFriendRequest(addresseeId)
    }
}


