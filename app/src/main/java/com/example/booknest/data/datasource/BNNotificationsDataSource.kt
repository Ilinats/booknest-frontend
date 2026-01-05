package com.example.booknest.data.datasource

import com.example.booknest.data.service.NotificationsService
import com.example.booknest.domain.model.request.RegisterDeviceTokenRequest
import com.example.booknest.domain.model.request.UpdateDeviceTokenRequest
import com.example.booknest.domain.model.response.DeviceTokenResponse
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.domain.model.response.NotificationsListResponse
import com.example.booknest.domain.model.response.UnreadCountResponse

class BNNotificationsDataSource(private val notificationsService: NotificationsService) :
    NotificationsDataSource {

    override suspend fun getNotifications(unreadOnly: Boolean?): Result<NotificationsListResponse> {
        return requestBody(notificationsService.getNotifications(unreadOnly))
    }

    override suspend fun getUnreadCount(): Result<UnreadCountResponse> {
        return requestBody(notificationsService.getUnreadCount())
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<NotificationResponse> {
        return requestBody(notificationsService.markNotificationAsRead(notificationId))
    }

    override suspend fun markAllNotificationsAsRead(): Result<Unit> {
        val response = notificationsService.markAllNotificationsAsRead()
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(
                Throwable(
                    response.errorBody()?.string() ?: "Failed to mark all as read"
                )
            )
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        val response = notificationsService.deleteNotification(notificationId)
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(
                Throwable(
                    response.errorBody()?.string() ?: "Failed to delete notification"
                )
            )
        }
    }

    override suspend fun deleteAllNotifications(): Result<Unit> {
        val response = notificationsService.deleteAllNotifications()
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(
                Throwable(
                    response.errorBody()?.string() ?: "Failed to delete all notifications"
                )
            )
        }
    }

    override suspend fun registerDeviceToken(request: RegisterDeviceTokenRequest): Result<DeviceTokenResponse> {
        return requestBody(notificationsService.registerDeviceToken(request))
    }
}

