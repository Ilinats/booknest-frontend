package com.example.booknest.data.service

import com.example.booknest.data.constants.DeviceTokens
import com.example.booknest.data.constants.Notifications
import com.example.booknest.data.constants.PathConstants
import com.example.booknest.data.constants.QueryConstants
import com.example.booknest.domain.model.request.RegisterDeviceTokenRequest
import com.example.booknest.domain.model.request.UpdateDeviceTokenRequest
import com.example.booknest.domain.model.response.DeviceTokenResponse
import com.example.booknest.domain.model.response.MessageResponse
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.domain.model.response.NotificationsListResponse
import com.example.booknest.domain.model.response.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsService {
    @GET(Notifications.LIST)
    suspend fun getNotifications(
        @Query(QueryConstants.UNREAD_ONLY) unreadOnly: Boolean?
    ): Response<NotificationsListResponse>

    @GET(Notifications.UNREAD_COUNT)
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @PATCH(Notifications.MARK_READ)
    suspend fun markNotificationAsRead(
        @Path(PathConstants.NOTIFICATION_ID) notificationId: String
    ): Response<NotificationResponse>

    @PATCH(Notifications.MARK_ALL_READ)
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>

    @DELETE(Notifications.DELETE)
    suspend fun deleteNotification(
        @Path(PathConstants.NOTIFICATION_ID) notificationId: String
    ): Response<MessageResponse>

    @DELETE(Notifications.DELETE_ALL)
    suspend fun deleteAllNotifications(): Response<MessageResponse>

    @POST(DeviceTokens.REGISTER)
    suspend fun registerDeviceToken(@Body request: RegisterDeviceTokenRequest): Response<DeviceTokenResponse>
}

