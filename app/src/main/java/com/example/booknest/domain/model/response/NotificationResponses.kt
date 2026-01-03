package com.example.booknest.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val readAt: String? = null,
    val data: Map<String, String>? = null,
    val bookId: String? = null,
    val applicationId: String? = null,
    val relatedUserId: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class NotificationsListResponse(
    val data: List<NotificationResponse>,
    val total: Int,
    val skip: Int? = null,
    val take: Int? = null,
    val hasMore: Boolean? = null
) {
    val notifications: List<NotificationResponse>
        get() = data
}

@Serializable
data class UnreadCountResponse(
    val count: Int
)

@Serializable
data class DeviceTokenResponse(
    val id: String,
    val userId: String,
    val token: String,
    val deviceType: String? = null,
    val deviceId: String? = null,
    val appVersion: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

