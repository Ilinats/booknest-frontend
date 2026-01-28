package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.FriendsRepository

class SearchUsersUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(query: String, limit: Int? = 20): Result<List<UserResponse>> {
        return friendsRepository.searchUsers(query, limit)
    }
}


