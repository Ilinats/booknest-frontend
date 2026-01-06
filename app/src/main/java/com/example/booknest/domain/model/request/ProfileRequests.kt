package com.example.booknest.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class UpdateUserProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class UpdateSocialMediaRequest(
    val instagram: String? = null,
    val tiktok: String? = null,
    val youtube: String? = null,
    val goodreads: String? = null,
    val custom: List<CustomSocialLink>? = null
)

@Serializable
data class CustomSocialLink(
    val platform: String,
    val url: String
)

@Serializable
data class UpdatePrivacyRequest(
    val activityPrivacy: String? = null,
    val profilePrivacy: String? = null,
    val readingListPrivacy: String? = null,
    val reviewsPrivacy: String? = null
)

@Serializable
data class UpdateNotificationSettingsRequest(
    val notificationsEnabled: Boolean? = null,
    val emailNotifications: Boolean? = null,
    val notificationPreferences: List<String>? = null
)

@Serializable
data class CreateAddressRequest(
    val streetAddress: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val isPrimary: Boolean = false
)

@Serializable
data class UpdateAddressRequest(
    val streetAddress: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val isPrimary: Boolean? = null
)

