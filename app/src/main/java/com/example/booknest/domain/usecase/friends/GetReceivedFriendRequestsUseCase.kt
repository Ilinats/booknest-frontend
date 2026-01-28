package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.FriendsRepository

class GetReceivedFriendRequestsUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(): Result<List<UserResponse>> {
        return friendsRepository.getReceivedFriendRequests()
    }
}


