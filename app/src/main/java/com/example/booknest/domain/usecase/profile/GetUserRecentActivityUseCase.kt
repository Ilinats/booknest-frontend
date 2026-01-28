package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetUserRecentActivityUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(username: String, days: Int? = 7, limit: Int? = 50): Result<List<UserActivityResponse>> {
        return profileRepository.getUserRecentActivity(username, days, limit)
    }
}


