package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.repository.FriendsRepository

class UnfriendUserUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(friendId: String): Result<Unit> {
        return friendsRepository.unfriendUser(friendId)
    }
}


