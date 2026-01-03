package com.example.booknest.data.repository

import com.example.booknest.data.datasource.ProfilesDataSource
import com.example.booknest.data.datasource.resultBody
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
import com.example.booknest.domain.repository.ProfileRepository
import okhttp3.MultipartBody

class BNProfileRepository(private val profilesDataSource: ProfilesDataSource) : ProfileRepository {

    override suspend fun getMyProfile(): Result<UserProfileResponse> {
        return resultBody(profilesDataSource.getMyProfile())
    }

    override suspend fun updateMyProfile(profile: UpdateProfileRequest): Result<UserProfileResponse> {
        return resultBody(profilesDataSource.updateMyProfile(profile))
    }

    override suspend fun updateUserProfile(
        userId: String,
        profile: UpdateUserProfileRequest
    ): Result<UserResponse> {
        return resultBody(profilesDataSource.updateUserProfile(userId, profile))
    }

    override suspend fun getSocialMediaOptions(): Result<SocialMediaOptionsResponse> {
        return resultBody(profilesDataSource.getSocialMediaOptions())
    }

    override suspend fun updateSocialMedia(request: UpdateSocialMediaRequest): Result<UserProfileResponse> {
        return resultBody(profilesDataSource.updateSocialMedia(request))
    }

    override suspend fun updatePrivacySettings(request: UpdatePrivacyRequest): Result<UserProfileResponse> {
        return resultBody(profilesDataSource.updatePrivacySettings(request))
    }

    override suspend fun updateNotificationSettings(request: UpdateNotificationSettingsRequest): Result<UserProfileResponse> {
        return resultBody(profilesDataSource.updateNotificationSettings(request))
    }

    override suspend fun getPublicUserProfile(username: String): Result<PublicUserProfileResponse> {
        return resultBody(profilesDataSource.getPublicUserProfile(username))
    }

    override suspend fun getMyActivity(limit: Int?): Result<List<UserActivityResponse>> {
        return resultBody(profilesDataSource.getMyActivity(limit))
    }

    override suspend fun getMyPublicActivity(limit: Int?): Result<List<UserActivityResponse>> {
        return resultBody(profilesDataSource.getMyPublicActivity(limit))
    }

    override suspend fun getMyRecentActivity(
        days: Int?,
        limit: Int?
    ): Result<List<UserActivityResponse>> {
        return resultBody(profilesDataSource.getMyRecentActivity(days, limit))
    }

    override suspend fun getUserRecentActivity(
        username: String,
        days: Int?,
        limit: Int?
    ): Result<List<UserActivityResponse>> {
        return resultBody(profilesDataSource.getUserRecentActivity(username, days, limit))
    }

    override suspend fun getMyActivityStats(): Result<ActivityStatsResponse> {
        return resultBody(profilesDataSource.getMyActivityStats())
    }

    override suspend fun getMyAddresses(): Result<List<ReaderAddressResponse>> {
        return resultBody(profilesDataSource.getMyAddresses())
    }

    override suspend fun addAddress(address: CreateAddressRequest): Result<ReaderAddressResponse> {
        return resultBody(profilesDataSource.addAddress(address))
    }

    override suspend fun updateAddress(
        addressId: String,
        address: UpdateAddressRequest
    ): Result<ReaderAddressResponse> {
        return resultBody(profilesDataSource.updateAddress(addressId, address))
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> {
        return resultBody(profilesDataSource.deleteAddress(addressId))
    }

    override suspend fun getMyStats(): Result<UserStatsResponse> {
        return resultBody(profilesDataSource.getMyStats())
    }

    override suspend fun getAuthorStats(authorId: String): Result<AuthorStatsResponse> {
        return resultBody(profilesDataSource.getAuthorStats(authorId))
    }

    override suspend fun searchUsers(query: String, limit: Int?): Result<UserSearchResultResponse> {
        return resultBody(profilesDataSource.searchUsers(query, limit))
    }

    override suspend fun uploadAvatar(avatarPart: MultipartBody.Part): Result<UploadAvatarResponse> {
        return resultBody(profilesDataSource.uploadAvatar(avatarPart))
    }

    override suspend fun removeAvatar(): Result<UserResponse> {
        return resultBody(profilesDataSource.removeAvatar())
    }
}

