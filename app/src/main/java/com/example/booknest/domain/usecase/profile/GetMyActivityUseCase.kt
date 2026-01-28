package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetMyActivityUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(
        limit: Int? = 20,
        publicOnly: Boolean = false
    ): Result<List<UserActivityResponse>> {
        return if (publicOnly) {
            repository.getMyPublicActivity(limit)
        } else {
            repository.getMyActivity(limit)
        }
    }
}
