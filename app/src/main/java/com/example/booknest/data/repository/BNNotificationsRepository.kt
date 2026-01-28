package com.example.booknest.data.repository

import com.example.booknest.data.datasource.NotificationsDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.request.RegisterDeviceTokenRequest
import com.example.booknest.domain.model.request.UpdateDeviceTokenRequest
import com.example.booknest.domain.model.response.DeviceTokenResponse
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.domain.model.response.NotificationsListResponse
import com.example.booknest.domain.model.response.UnreadCountResponse
import com.example.booknest.domain.repository.NotificationsRepository

class BNNotificationsRepository(private val notificationsDataSource: NotificationsDataSource) :
    NotificationsRepository {

    override suspend fun getNotifications(unreadOnly: Boolean?): Result<NotificationsListResponse> {
        return resultBody(notificationsDataSource.getNotifications(unreadOnly))
    }

    override suspend fun getUnreadCount(): Result<UnreadCountResponse> {
        return resultBody(notificationsDataSource.getUnreadCount())
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<NotificationResponse> {
        return resultBody(notificationsDataSource.markNotificationAsRead(notificationId))
    }

    override suspend fun markAllNotificationsAsRead(): Result<Unit> {
        return resultBody(notificationsDataSource.markAllNotificationsAsRead())
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return resultBody(notificationsDataSource.deleteNotification(notificationId))
    }

    override suspend fun deleteAllNotifications(): Result<Unit> {
        return resultBody(notificationsDataSource.deleteAllNotifications())
    }

    override suspend fun registerDeviceToken(request: RegisterDeviceTokenRequest): Result<DeviceTokenResponse> {
        return resultBody(notificationsDataSource.registerDeviceToken(request))
    }
}
