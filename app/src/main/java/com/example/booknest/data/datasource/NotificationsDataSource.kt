package com.example.booknest.data.datasource

import com.example.booknest.domain.model.request.RegisterDeviceTokenRequest
import com.example.booknest.domain.model.request.UpdateDeviceTokenRequest
import com.example.booknest.domain.model.response.DeviceTokenResponse
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.domain.model.response.NotificationsListResponse
import com.example.booknest.domain.model.response.UnreadCountResponse

interface NotificationsDataSource {
    suspend fun getNotifications(
        unreadOnly: Boolean? = null,
        skip: Int? = null,
        take: Int? = null,
    ): Result<NotificationsListResponse>
    suspend fun getUnreadCount(): Result<UnreadCountResponse>
    suspend fun markNotificationAsRead(notificationId: String): Result<NotificationResponse>
    suspend fun markAllNotificationsAsRead(): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    suspend fun deleteAllNotifications(): Result<Unit>
    suspend fun registerDeviceToken(request: RegisterDeviceTokenRequest): Result<DeviceTokenResponse>
}

