package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetMyRecentActivityUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(days: Int? = 7, limit: Int? = 50): Result<List<UserActivityResponse>> {
        return profileRepository.getMyRecentActivity(days, limit)
    }
}


