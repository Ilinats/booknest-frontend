package com.example.booknest.domain.usecase.notifications

import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.domain.repository.NotificationsRepository

class MarkNotificationAsReadUseCase(
    private val notificationsRepository: NotificationsRepository
) {
    suspend operator fun invoke(notificationId: String): Result<NotificationResponse> {
        return notificationsRepository.markNotificationAsRead(notificationId)
    }
}


