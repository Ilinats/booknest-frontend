package com.example.booknest.data.datasource

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
import okhttp3.MultipartBody

interface ProfilesDataSource {
    suspend fun getMyProfile(): Result<UserProfileResponse>
    suspend fun updateMyProfile(profile: UpdateProfileRequest): Result<UserProfileResponse>
    suspend fun updateUserProfile(
        userId: String,
        profile: UpdateUserProfileRequest
    ): Result<UserResponse>

    suspend fun getSocialMediaOptions(): Result<SocialMediaOptionsResponse>
    suspend fun updateSocialMedia(request: UpdateSocialMediaRequest): Result<UserProfileResponse>
    suspend fun updatePrivacySettings(request: UpdatePrivacyRequest): Result<UserProfileResponse>
    suspend fun updateNotificationSettings(request: UpdateNotificationSettingsRequest): Result<UserProfileResponse>
    suspend fun getPublicUserProfile(username: String): Result<PublicUserProfileResponse>
    suspend fun getMyActivity(limit: Int?): Result<List<UserActivityResponse>>
    suspend fun getMyPublicActivity(limit: Int?): Result<List<UserActivityResponse>>
    suspend fun getMyRecentActivity(days: Int?, limit: Int?): Result<List<UserActivityResponse>>
    suspend fun getUserRecentActivity(
        username: String,
        days: Int?,
        limit: Int?
    ): Result<List<UserActivityResponse>>

    suspend fun getMyActivityStats(): Result<ActivityStatsResponse>
    suspend fun getMyAddresses(): Result<List<ReaderAddressResponse>>
    suspend fun addAddress(address: CreateAddressRequest): Result<ReaderAddressResponse>
    suspend fun updateAddress(
        addressId: String,
        address: UpdateAddressRequest
    ): Result<ReaderAddressResponse>

    suspend fun deleteAddress(addressId: String): Result<Unit>
    suspend fun getMyStats(): Result<UserStatsResponse>
    suspend fun getAuthorStats(authorId: String): Result<AuthorStatsResponse>
    suspend fun searchUsers(query: String, limit: Int?): Result<UserSearchResultResponse>
    suspend fun uploadAvatar(avatarPart: MultipartBody.Part): Result<UploadAvatarResponse>
    suspend fun removeAvatar(): Result<UserResponse>
    suspend fun deleteAccount(): Result<Unit>
}

