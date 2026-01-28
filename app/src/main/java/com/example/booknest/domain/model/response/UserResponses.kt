package com.example.booknest.domain.model.response

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val userType: String? = null,
    val birthDate: String? = null,
    val emailVerified: Boolean = false,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val profilePictureUrl: String? = null,
    val address: UserAddressResponse? = null,
    val createdAt: String? = null
)

@Serializable
data class UserAddressResponse(
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null
)

@Serializable
data class UploadAvatarResponse(
    val user: UserResponse,
    val avatar: AvatarInfo
)

@Serializable
data class AvatarInfo(
    val url: String,
    val size: Int,
    val type: String,
    @SerialName("originalName")
    val originalName: String
)

@Serializable
data class UserProfileResponse(
    val id: String,
    val userId: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val userType: String? = null,
    val birthDate: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val createdAt: String,
    val updatedAt: String? = null,
    val stats: UserStatsDataResponse? = null,
    val socialMedia: SocialMediaResponse? = null,
    val activityPrivacy: String? = null,
    val profilePrivacy: String? = null,
    val readingListPrivacy: String? = null,
    val reviewsPrivacy: String? = null,
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = true,
    val notificationPreferences: List<String>? = null,
    val addresses: List<ReaderAddressResponse>? = null
)

@Serializable
data class SocialMediaResponse(
    val instagram: String? = null,
    val tiktok: String? = null,
    val youtube: String? = null,
    val goodreads: String? = null,
    val custom: List<CustomSocialLinkResponse>? = null
)

@Serializable
data class CustomSocialLinkResponse(
    val platform: String,
    val url: String
)

@Serializable
data class NotificationPreferencesResponse(
    val notificationTypes: List<String>? = null
)

@Serializable
data class ReaderAddressResponse(
    val id: String,
    val streetAddress: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val isPrimary: Boolean
)

@Serializable
data class SocialMediaOptionsResponse(
    val predefined: List<SocialMediaOptionResponse>,
    val custom: CustomSocialOptionsResponse
)

@Serializable
data class SocialMediaOptionResponse(
    val key: String,
    val name: String,
    val icon: String,
    val placeholder: String
)

@Serializable
data class CustomSocialOptionsResponse(
    val enabled: Boolean,
    val maxCustomLinks: Int,
    val placeholder: String
)

@Serializable
data class PublicProfileResponse(
    val id: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val userType: String? = null,
    val birthDate: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val stats: UserStatsDataResponse? = null,
    val socialMedia: SocialMediaResponse? = null,
    val activityPrivacy: String? = null,
    val profilePrivacy: String? = null,
    val readingListPrivacy: String? = null,
    val reviewsPrivacy: String? = null,
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = true,
    val notificationPreferences: List<String>? = null,
    val addresses: List<ReaderAddressResponse>? = null
) {
    fun toUserProfileResponse(user: UserResponse): UserProfileResponse {
        return UserProfileResponse(
            id = id ?: user.id,
            userId = userId ?: user.id,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            userType = user.userType,
            birthDate = user.birthDate,
            bio = user.bio,
            avatarUrl = user.avatarUrl ?: user.profilePictureUrl,
            isVerified = user.emailVerified,
            createdAt = createdAt ?: user.createdAt ?: user.id,
            updatedAt = updatedAt,
            stats = stats,
            socialMedia = socialMedia,
            activityPrivacy = activityPrivacy,
            profilePrivacy = profilePrivacy,
            readingListPrivacy = readingListPrivacy,
            reviewsPrivacy = reviewsPrivacy,
            notificationsEnabled = notificationsEnabled,
            emailNotifications = emailNotifications,
            notificationPreferences = notificationPreferences,
            addresses = addresses
        )
    }
}

@Serializable
data class PublicUserProfileResponse(
    val user: UserResponse,
    val profile: PublicProfileResponse,
    val isFriend: Boolean
) {
    fun toFullProfile(): UserProfileResponse {
        return profile.toUserProfileResponse(user)
    }
}

@Serializable
data class UserSearchResultResponse(
    val data: List<UserResponse>,
    val total: Int
)

@Serializable
data class UserActivityResponse(
    val id: String,
    val userId: String,
    val activityType: String,
    val bookId: String? = null,
    val applicationId: String? = null,
    val metadata: Map<String, JsonElement>? = null,
    val createdAt: String,
    val user: UserResponse? = null,
    val book: BookResponse? = null,
    val application: ApplicationResponse? = null
) {
    fun getMetadataString(key: String): String? {
        return metadata?.get(key)?.jsonPrimitive?.content
    }
    
    fun getMetadataInt(key: String): Int? {
        return metadata?.get(key)?.jsonPrimitive?.content?.toIntOrNull()
    }
}

@Serializable
data class ActivityStatsResponse(
    val totalActivities: Int,
    val activitiesByType: Map<String, Int>,
    val lastActivity: String
)

@Serializable
data class UserStatsResponse(
    val user: UserResponse,
    val stats: UserStatsDataResponse
)

@Serializable
data class AuthorStatsResponse(
    val author: UserResponse,
    val stats: UserStatsDataResponse
)

object GenresBreakdownSerializer : KSerializer<Map<String, Int>?> {
    override val descriptor: SerialDescriptor = 
        kotlinx.serialization.descriptors.buildClassSerialDescriptor("GenresBreakdown")

    override fun serialize(encoder: Encoder, value: Map<String, Int>?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeSerializableValue(JsonObject.serializer(), JsonObject(value.mapValues { JsonPrimitive(it.value) }))
        }
    }

    override fun deserialize(decoder: Decoder): Map<String, Int>? {
        return try {
            val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
            when (jsonElement) {
                is JsonObject -> {
                    val result = mutableMapOf<String, Int>()
                    jsonElement.entries.forEach { entry ->
                        val intValue = (entry.value as? JsonPrimitive)?.content?.toIntOrNull() ?: 0
                        result[entry.key] = intValue
                    }
                    result
                }
                is JsonArray -> {
                    if (jsonElement.isEmpty()) {
                        null
                    } else {
                        val result = mutableMapOf<String, Int>()
                        jsonElement.forEach { element ->
                            if (element is JsonObject) {
                                val genreName = element["genreName"]?.jsonPrimitive?.content
                                val count = element["count"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                                if (genreName != null) {
                                    result[genreName] = count
                                }
                            }
                        }
                        result
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
data class UserStatsDataResponse(
    val totalBooks: Int? = null,
    val publishedBooks: Int? = null,
    val draftBooks: Int? = null,
    val totalApplications: Int,
    val approvedApplications: Int,
    val pendingApplications: Int,
    val completedReads: Int? = null,
    val completedReadsThisMonth: Int? = null,
    val completedReadsThisYear: Int? = null,
    val applicationsThisMonth: Int? = null,
    val approvalRate: Int? = null,
    val averageResponseTime: Double? = null,
    val totalReviews: Int? = null,
    val averageRating: Double? = null,
    val pagesRead: Int? = null,
    val totalWordCount: Int? = null,
    @Serializable(with = GenresBreakdownSerializer::class)
    val genresBreakdown: Map<String, Int>? = null,
    val averageReadingTime: Double? = null,
    val reviewCompletionRate: Double? = null,
    val userType: String
)

