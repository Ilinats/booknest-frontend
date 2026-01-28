package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.response.LotteryResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class RunLotterySelectionUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(bookId: String): Result<LotteryResponse> {
        return applicationsRepository.runLotterySelection(bookId)
    }
}


