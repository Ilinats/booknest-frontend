package com.example.booknest.domain.usecase.notifications

import com.example.booknest.domain.model.response.NotificationsListResponse
import com.example.booknest.domain.repository.NotificationsRepository

class GetNotificationsUseCase(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke(
        unreadOnly: Boolean? = null,
        skip: Int? = null,
        take: Int? = null,
    ): Result<NotificationsListResponse> =
        repository.getNotifications(unreadOnly, skip, take)
}
