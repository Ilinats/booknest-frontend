package com.example.booknest.data.service

import com.example.booknest.data.constants.PathConstants
import com.example.booknest.data.constants.Profiles
import com.example.booknest.data.constants.QueryConstants
import com.example.booknest.data.constants.Users
import com.example.booknest.domain.model.request.CreateAddressRequest
import com.example.booknest.domain.model.request.UpdateAddressRequest
import com.example.booknest.domain.model.request.UpdateNotificationSettingsRequest
import com.example.booknest.domain.model.request.UpdatePrivacyRequest
import com.example.booknest.domain.model.request.UpdateProfileRequest
import com.example.booknest.domain.model.request.UpdateSocialMediaRequest
import com.example.booknest.domain.model.request.UpdateUserProfileRequest
import com.example.booknest.domain.model.response.ActivityStatsResponse
import com.example.booknest.domain.model.response.PublicUserProfileResponse
import com.example.booknest.domain.model.response.ReaderAddressResponse
import com.example.booknest.domain.model.response.SocialMediaOptionsResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.model.response.UploadAvatarResponse
import com.example.booknest.domain.model.response.UserSearchResultResponse
import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.domain.model.response.AuthorStatsResponse
import com.example.booknest.domain.model.response.MessageResponse
import retrofit2.Response
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Streaming
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfilesService {
    @GET(Users.ME)
    suspend fun getMe(): Response<UserResponse>
    
    @GET(Profiles.ME)
    suspend fun getMyProfile(): Response<UserProfileResponse>

    @PATCH(Users.ME)
    suspend fun updateMyProfile(@Body profile: UpdateProfileRequest): Response<UserResponse>

    @PATCH(Users.PROFILE)
    suspend fun updateUserProfile(
        @Path(PathConstants.USER_ID) userId: String,
        @Body profile: UpdateUserProfileRequest
    ): Response<UserResponse>

    @PATCH(Profiles.SOCIAL_MEDIA)
    suspend fun updateSocialMedia(@Body request: UpdateSocialMediaRequest): Response<UserProfileResponse>

    @PATCH(Profiles.PRIVACY)
    suspend fun updatePrivacySettings(@Body request: UpdatePrivacyRequest): Response<UserProfileResponse>

    @PATCH(Profiles.NOTIFICATIONS)
    suspend fun updateNotificationSettings(@Body request: UpdateNotificationSettingsRequest): Response<UserProfileResponse>

    @GET(Profiles.USER)
    suspend fun getPublicUserProfile(
        @Path(PathConstants.USERNAME) username: String
    ): Response<PublicUserProfileResponse>

    @GET(Profiles.ACTIVITY)
    suspend fun getMyActivity(
        @Query(QueryConstants.LIMIT) limit: Int?
    ): Response<List<UserActivityResponse>>

    @GET(Profiles.ACTIVITY_PUBLIC)
    suspend fun getMyPublicActivity(
        @Query(QueryConstants.LIMIT) limit: Int?
    ): Response<List<UserActivityResponse>>

    @GET(Profiles.ACTIVITY_RECENT)
    suspend fun getMyRecentActivity(
        @Query(QueryConstants.DAYS) days: Int?,
        @Query(QueryConstants.LIMIT) limit: Int?
    ): Response<List<UserActivityResponse>>

    @GET(Profiles.USER_ACTIVITY_RECENT)
    suspend fun getUserRecentActivity(
        @Path(PathConstants.USERNAME) username: String,
        @Query(QueryConstants.DAYS) days: Int?,
        @Query(QueryConstants.LIMIT) limit: Int?
    ): Response<List<UserActivityResponse>>

    @GET(Profiles.ACTIVITY_STATS)
    suspend fun getMyActivityStats(): Response<ActivityStatsResponse>

    @GET(Profiles.ADDRESSES)
    suspend fun getMyAddresses(): Response<List<ReaderAddressResponse>>

    @POST(Profiles.ADDRESSES)
    suspend fun addAddress(@Body address: CreateAddressRequest): Response<ReaderAddressResponse>

    @PATCH(Profiles.ADDRESS_ID)
    suspend fun updateAddress(
        @Path(PathConstants.ADDRESS_ID) addressId: String,
        @Body address: UpdateAddressRequest
    ): Response<ReaderAddressResponse>

    @DELETE(Profiles.ADDRESS_ID)
    suspend fun deleteAddress(
        @Path(PathConstants.ADDRESS_ID) addressId: String
    ): Response<Unit>

    @GET(Users.MY_STATS)
    suspend fun getMyStats(): Response<UserStatsResponse>

    @GET(Users.AUTHOR_STATS)
    suspend fun getAuthorStats(
        @Path(PathConstants.AUTHOR_ID) authorId: String
    ): Response<AuthorStatsResponse>
    @Multipart
    @POST(Users.UPLOAD_AVATAR)
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<UploadAvatarResponse>
    
    @DELETE(Users.DELETE_AVATAR)
    suspend fun removeAvatar(): Response<UserResponse>
    
    @Streaming
    @DELETE(Users.DELETE_ACCOUNT)
    suspend fun deleteAccount(): Response<ResponseBody>
}

