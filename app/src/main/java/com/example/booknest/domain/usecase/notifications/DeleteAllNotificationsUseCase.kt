package com.example.booknest.domain.usecase.notifications

import com.example.booknest.domain.repository.NotificationsRepository

class DeleteAllNotificationsUseCase(
    private val notificationsRepository: NotificationsRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return notificationsRepository.deleteAllNotifications()
    }
}


