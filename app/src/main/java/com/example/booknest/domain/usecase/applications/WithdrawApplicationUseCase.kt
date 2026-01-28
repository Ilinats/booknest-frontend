package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.repository.ApplicationsRepository

class WithdrawApplicationUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(applicationId: String): Result<Unit> {
        return applicationsRepository.withdrawApplication(applicationId)
    }
}


