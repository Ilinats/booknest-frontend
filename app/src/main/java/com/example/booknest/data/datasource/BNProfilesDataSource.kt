package com.example.booknest.data.datasource

import com.example.booknest.data.service.ProfilesService
import com.example.booknest.domain.model.request.CreateAddressRequest
import com.example.booknest.domain.model.request.UpdateAddressRequest
import com.example.booknest.domain.model.request.UpdateNotificationSettingsRequest
import com.example.booknest.domain.model.request.UpdatePrivacyRequest
import com.example.booknest.domain.model.request.UpdateProfileRequest
import com.example.booknest.domain.model.request.UpdateSocialMediaRequest
import com.example.booknest.domain.model.request.UpdateUserProfileRequest
import com.example.booknest.domain.model.response.ActivityStatsResponse
import com.example.booknest.domain.model.response.AuthorStatsResponse
import com.example.booknest.domain.model.response.PublicUserProfileResponse
import com.example.booknest.domain.model.response.ReaderAddressResponse
import com.example.booknest.domain.model.response.SocialMediaOptionsResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.model.response.UserSearchResultResponse
import com.example.booknest.domain.model.response.UploadAvatarResponse
import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.domain.model.response.MessageResponse
import okhttp3.MultipartBody

class BNProfilesDataSource(private val profilesService: ProfilesService) : ProfilesDataSource {

    override suspend fun getMe(): Result<UserResponse> {
        return requestBody(profilesService.getMe())
    }

    override suspend fun getMyProfile(): Result<UserProfileResponse> {
        val statsResult = requestBody(profilesService.getMyStats())
        val profileResult = requestBody(profilesService.getMyProfile())

        return statsResult.fold(
            onSuccess = { statsResponse ->
                val user = statsResponse.user
                profileResult.fold(
                    onSuccess = { profile ->
                        val combinedProfile = UserProfileResponse(
                            id = profile.id,
                            userId = profile.userId ?: user.id,
                            username = user.username,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            userType = user.userType,
                            birthDate = user.birthDate,
                            bio = user.bio,
                            avatarUrl = user.avatarUrl ?: user.profilePictureUrl,
                            isVerified = user.emailVerified,
                            createdAt = profile.createdAt,
                            updatedAt = profile.updatedAt,
                            stats = statsResponse.stats,
                            socialMedia = profile.socialMedia,
                            activityPrivacy = profile.activityPrivacy,
                            profilePrivacy = profile.profilePrivacy,
                            readingListPrivacy = profile.readingListPrivacy,
                            reviewsPrivacy = profile.reviewsPrivacy,
                            notificationsEnabled = profile.notificationsEnabled,
                            emailNotifications = profile.emailNotifications,
                            notificationPreferences = profile.notificationPreferences,
                            addresses = profile.addresses
                        )
                        Result.success(combinedProfile)
                    },
                    onFailure = { profileError ->
                        val defaultProfile = UserProfileResponse(
                            id = user.id,
                            userId = user.id,
                            username = user.username,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            userType = user.userType,
                            birthDate = user.birthDate,
                            bio = user.bio,
                            avatarUrl = user.avatarUrl ?: user.profilePictureUrl,
                            isVerified = user.emailVerified,
                            createdAt = user.id,
                            updatedAt = null,
                            stats = statsResponse.stats,
                            socialMedia = null,
                            activityPrivacy = "friends",
                            profilePrivacy = "friends",
                            readingListPrivacy = "friends",
                            reviewsPrivacy = "public",
                            notificationsEnabled = true,
                            emailNotifications = true,
                            notificationPreferences = null,
                            addresses = null
                        )
                        Result.success(defaultProfile)
                    }
                )
            },
            onFailure = { statsError ->
                val userResult = requestBody(profilesService.getMe())
                userResult.fold(
                    onSuccess = { user ->
                        profileResult.fold(
                            onSuccess = { profile ->
                                val combinedProfile = UserProfileResponse(
                                    id = profile.id,
                                    userId = profile.userId ?: user.id,
                                    username = user.username,
                                    firstName = user.firstName,
                                    lastName = user.lastName,
                                    userType = user.userType,
                                    birthDate = user.birthDate,
                                    bio = user.bio,
                                    avatarUrl = user.avatarUrl ?: user.profilePictureUrl,
                                    isVerified = user.emailVerified,
                                    createdAt = profile.createdAt,
                                    updatedAt = profile.updatedAt,
                                    stats = null,
                                    socialMedia = profile.socialMedia,
                                    activityPrivacy = profile.activityPrivacy,
                                    profilePrivacy = profile.profilePrivacy,
                                    readingListPrivacy = profile.readingListPrivacy,
                                    reviewsPrivacy = profile.reviewsPrivacy,
                                    notificationsEnabled = profile.notificationsEnabled,
                                    emailNotifications = profile.emailNotifications,
                                    notificationPreferences = profile.notificationPreferences,
                                    addresses = profile.addresses
                                )
                                Result.success(combinedProfile)
                            },
                            onFailure = { profileError ->
                                Result.failure(statsError)
                            }
                        )
                    },
                    onFailure = { userError ->
                        Result.failure(statsError)
                    }
                )
            }
        )
    }

    override suspend fun updateMyProfile(profile: UpdateProfileRequest): Result<UserProfileResponse> {
        val updateResult = requestBody(profilesService.updateMyProfile(profile))
        return updateResult.fold(
            onSuccess = {
                getMyProfile()
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    override suspend fun updateUserProfile(
        userId: String,
        profile: UpdateUserProfileRequest
    ): Result<UserResponse> {
        return requestBody(profilesService.updateUserProfile(userId, profile))
    }

    override suspend fun updateSocialMedia(request: UpdateSocialMediaRequest): Result<UserProfileResponse> {
        return requestBody(profilesService.updateSocialMedia(request))
    }

    override suspend fun updatePrivacySettings(request: UpdatePrivacyRequest): Result<UserProfileResponse> {
        return requestBody(profilesService.updatePrivacySettings(request))
    }

    override suspend fun updateNotificationSettings(request: UpdateNotificationSettingsRequest): Result<UserProfileResponse> {
        return requestBody(profilesService.updateNotificationSettings(request))
    }

    override suspend fun getPublicUserProfile(username: String): Result<PublicUserProfileResponse> {
        return requestBody(profilesService.getPublicUserProfile(username))
    }

    override suspend fun getMyActivity(limit: Int?): Result<List<UserActivityResponse>> {
        return requestBody(profilesService.getMyActivity(limit))
    }

    override suspend fun getMyPublicActivity(limit: Int?): Result<List<UserActivityResponse>> {
        return requestBody(profilesService.getMyPublicActivity(limit))
    }

    override suspend fun getMyRecentActivity(
        days: Int?,
        limit: Int?
    ): Result<List<UserActivityResponse>> {
        return requestBody(profilesService.getMyRecentActivity(days, limit))
    }

    override suspend fun getUserRecentActivity(
        username: String,
        days: Int?,
        limit: Int?
    ): Result<List<UserActivityResponse>> {
        return requestBody(profilesService.getUserRecentActivity(username, days, limit))
    }

    override suspend fun getMyActivityStats(): Result<ActivityStatsResponse> {
        return requestBody(profilesService.getMyActivityStats())
    }

    override suspend fun getMyAddresses(): Result<List<ReaderAddressResponse>> {
        return requestBody(profilesService.getMyAddresses())
    }

    override suspend fun addAddress(address: CreateAddressRequest): Result<ReaderAddressResponse> {
        return requestBody(profilesService.addAddress(address))
    }

    override suspend fun updateAddress(
        addressId: String,
        address: UpdateAddressRequest
    ): Result<ReaderAddressResponse> {
        return requestBody(profilesService.updateAddress(addressId, address))
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> {
        return requestBodyUnit(profilesService.deleteAddress(addressId))
    }

    override suspend fun getMyStats(): Result<UserStatsResponse> {
        return requestBody(profilesService.getMyStats())
    }

    override suspend fun getAuthorStats(authorId: String): Result<AuthorStatsResponse> {
        return requestBody(profilesService.getAuthorStats(authorId))
    }

    override suspend fun uploadAvatar(avatarPart: MultipartBody.Part): Result<UploadAvatarResponse> {
        return requestBody(profilesService.uploadAvatar(avatarPart))
    }

    override suspend fun removeAvatar(): Result<UserResponse> {
        return requestBody(profilesService.removeAvatar())
    }

    override suspend fun deleteAccount(): Result<Unit> {
        var responseBody: okhttp3.ResponseBody? = null
        return try {
            val response = profilesService.deleteAccount()
            responseBody = response.body()
            
            android.util.Log.d("BNProfilesDataSource", "Delete account response: code=${response.code()}, isSuccessful=${response.isSuccessful}")
            
            if (response.isSuccessful && response.code() == 200) {
                try {
                    responseBody?.string()
                } catch (e: Exception) {
                    android.util.Log.w("BNProfilesDataSource", "Error reading response body", e)
                }
                android.util.Log.d("BNProfilesDataSource", "Delete account successful")
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }
                val errorMessage = extractErrorMessage(errorBody)
                android.util.Log.e("BNProfilesDataSource", "Delete account failed: $errorMessage")
                val errorWithMessage = com.example.booknest.data.error.BNError.Generic(
                    messageString = errorMessage,
                    error = null,
                    statusCode = response.code()
                )
                Result.failure(errorWithMessage)
            }
        } catch (ex: Exception) {
            android.util.Log.e("BNProfilesDataSource", "Exception deleting account: ${ex.message}", ex)
            Result.failure(Throwable(ex.message ?: "Failed to delete account: ${ex.javaClass.simpleName}"))
        } finally {
            try {
                responseBody?.close()
            } catch (e: Exception) {
            }
        }
    }
}

