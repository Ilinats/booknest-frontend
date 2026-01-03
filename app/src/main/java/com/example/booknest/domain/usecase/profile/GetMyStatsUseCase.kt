package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetMyStatsUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserStatsResponse> =
        repository.getMyStats()
}
