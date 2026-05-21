package com.example.booknest.data.datasource

import com.example.booknest.data.service.NotificationsService
import com.example.booknest.domain.model.request.RegisterDeviceTokenRequest
import com.example.booknest.domain.model.response.DeviceTokenResponse
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.domain.model.response.NotificationsListResponse
import com.example.booknest.domain.model.response.UnreadCountResponse

class BNNotificationsDataSource(private val notificationsService: NotificationsService) :
    NotificationsDataSource {

    override suspend fun getNotifications(
        unreadOnly: Boolean?,
        skip: Int?,
        take: Int?,
    ): Result<NotificationsListResponse> {
        return runSuspendRequest {
            notificationsService.getNotifications(
                unreadOnly = unreadOnly,
                skip = skip,
                take = take,
            )
        }
    }

    override suspend fun getUnreadCount(): Result<UnreadCountResponse> {
        return runSuspendRequest { notificationsService.getUnreadCount() }
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<NotificationResponse> {
        return runSuspendRequest { notificationsService.markNotificationAsRead(notificationId) }
    }

    override suspend fun markAllNotificationsAsRead(): Result<Unit> {
        return try {
            val response = notificationsService.markAllNotificationsAsRead()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Throwable(
                        response.errorBody()?.string() ?: "Failed to mark all as read"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkOrUnknown(e))
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            val response = notificationsService.deleteNotification(notificationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Throwable(
                        response.errorBody()?.string() ?: "Failed to delete notification"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkOrUnknown(e))
        }
    }

    override suspend fun deleteAllNotifications(): Result<Unit> {
        return try {
            val response = notificationsService.deleteAllNotifications()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Throwable(
                        response.errorBody()?.string() ?: "Failed to delete all notifications"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(mapNetworkOrUnknown(e))
        }
    }

    override suspend fun registerDeviceToken(request: RegisterDeviceTokenRequest): Result<DeviceTokenResponse> {
        return runSuspendRequest { notificationsService.registerDeviceToken(request) }
    }
}
