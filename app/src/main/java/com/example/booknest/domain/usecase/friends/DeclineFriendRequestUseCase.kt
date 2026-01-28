package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.repository.FriendsRepository

class DeclineFriendRequestUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(requesterId: String): Result<Unit> {
        return friendsRepository.declineFriendRequest(requesterId)
    }
}


