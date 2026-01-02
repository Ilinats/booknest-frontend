package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.FriendsRepository

class GetFriendsUseCase(
    private val repository: FriendsRepository
) {
    suspend operator fun invoke(): Result<List<UserResponse>> =
        repository.getFriends()
}
