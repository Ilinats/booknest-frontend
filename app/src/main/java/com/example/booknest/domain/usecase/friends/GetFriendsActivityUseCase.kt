package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.repository.FriendsRepository

class GetFriendsActivityUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(limit: Int? = 50): Result<List<UserActivityResponse>> {
        return friendsRepository.getFriendsActivity(limit)
    }
}


