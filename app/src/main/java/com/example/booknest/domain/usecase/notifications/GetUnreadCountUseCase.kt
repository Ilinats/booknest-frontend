package com.example.booknest.domain.usecase.notifications

import com.example.booknest.domain.model.response.UnreadCountResponse
import com.example.booknest.domain.repository.NotificationsRepository

class GetUnreadCountUseCase(
    private val notificationsRepository: NotificationsRepository
) {
    suspend operator fun invoke(): Result<UnreadCountResponse> {
        return notificationsRepository.getUnreadCount()
    }
}


