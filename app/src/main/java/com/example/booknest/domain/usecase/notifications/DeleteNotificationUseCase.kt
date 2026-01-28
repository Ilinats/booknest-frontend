package com.example.booknest.domain.usecase.notifications

import com.example.booknest.domain.repository.NotificationsRepository

class DeleteNotificationUseCase(
    private val notificationsRepository: NotificationsRepository
) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return notificationsRepository.deleteNotification(notificationId)
    }
}


