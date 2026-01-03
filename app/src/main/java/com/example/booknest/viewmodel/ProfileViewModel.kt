package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.CreateAddressRequest
import com.example.booknest.domain.model.request.CustomSocialLink
import com.example.booknest.domain.model.request.NotificationPreferencesRequest
import com.example.booknest.domain.model.request.UpdateAddressRequest
import com.example.booknest.domain.model.request.UpdateNotificationSettingsRequest
import com.example.booknest.domain.model.request.UpdatePrivacyRequest
import com.example.booknest.domain.model.request.UpdateProfileRequest
import com.example.booknest.domain.model.request.UpdateSocialMediaRequest
import com.example.booknest.domain.model.request.UpdateUserProfileRequest
import com.example.booknest.domain.model.response.ActivityStatsResponse
import com.example.booknest.domain.model.response.PublicUserProfileResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.SocialMediaResponse
import com.example.booknest.domain.model.response.NotificationPreferencesResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.domain.repository.ProfileRepository
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.files.UploadProfileImageUseCase
import com.example.booknest.domain.usecase.profile.GetAuthorStatsUseCase
import com.example.booknest.domain.usecase.profile.GetMyActivityUseCase
import com.example.booknest.domain.usecase.profile.GetMyProfileUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.domain.usecase.profile.GetUserProfileUseCase
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import com.example.booknest.data.service.ProfilesService

class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val getMyStatsUseCase: GetMyStatsUseCase,
    private val getAuthorStatsUseCase: GetAuthorStatsUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getMyActivityUseCase: GetMyActivityUseCase,
    private val profileRepository: ProfileRepository,
    private val browseBooksUseCase: BrowseBooksUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val context: Context? = null
) : ViewModel() {

    private val _myProfile = MutableStateFlow<UserProfileResponse?>(null)
    val myProfile: StateFlow<UserProfileResponse?> = _myProfile.asStateFlow()

    private val _addresses =
        MutableStateFlow<List<com.example.booknest.domain.model.response.ReaderAddressResponse>>(
            emptyList()
        )
    val addresses: StateFlow<List<com.example.booknest.domain.model.response.ReaderAddressResponse>> =
        _addresses.asStateFlow()

    private val _currentProfile = MutableStateFlow<UserProfileResponse?>(null)
    val currentProfile: StateFlow<UserProfileResponse?> = _currentProfile.asStateFlow()

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _myActivity = MutableStateFlow<List<UserActivityResponse>>(emptyList())
    val myActivity: StateFlow<List<UserActivityResponse>> = _myActivity.asStateFlow()

    private val _myPublicActivity = MutableStateFlow<List<UserActivityResponse>>(emptyList())
    val myPublicActivity: StateFlow<List<UserActivityResponse>> = _myPublicActivity.asStateFlow()

    private val _myRecentActivity = MutableStateFlow<List<UserActivityResponse>>(emptyList())
    val myRecentActivity: StateFlow<List<UserActivityResponse>> = _myRecentActivity.asStateFlow()

    private val _activityStats = MutableStateFlow<ActivityStatsResponse?>(null)
    val activityStats: StateFlow<ActivityStatsResponse?> = _activityStats.asStateFlow()

    private val _statsState = MutableStateFlow<StatsUiState>(StatsUiState.Idle)
    val statsState: StateFlow<StatsUiState> = _statsState.asStateFlow()

    private val _currentStats = MutableStateFlow<UserStatsResponse?>(null)
    val currentStats: StateFlow<UserStatsResponse?> = _currentStats.asStateFlow()

    private val _publicProfile = MutableStateFlow<PublicUserProfileResponse?>(null)
    val publicProfile: StateFlow<PublicUserProfileResponse?> = _publicProfile.asStateFlow()

    private val _profileEditState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Idle)
    val profileEditState: StateFlow<ProfileEditUiState> = _profileEditState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _authorBooks = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val authorBooks: StateFlow<List<RecommendedBookResponse>> = _authorBooks.asStateFlow()

    private val _authorBooksLoading = MutableStateFlow(false)
    val authorBooksLoading: StateFlow<Boolean> = _authorBooksLoading.asStateFlow()

    fun loadMyProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            try {
                _isLoading.value = true
                _error.value = null

                val result = getMyProfileUseCase()
                result
                    .onSuccess { profile ->
                        println("DEBUG: Profile loaded successfully: username='${profile.username}', firstName='${profile.firstName}', lastName='${profile.lastName}', hasStats=${profile.stats != null}")
                        onProfileLoaded(profile)
                    }
                    .onFailure { e ->
                        println("DEBUG: Profile load failed: ${e.message}")
                        e.printStackTrace()
                        handleProfileError(e.message ?: "Failed to load profile")
                    }
            } catch (e: Exception) {
                println("DEBUG: Profile load exception: ${e.message}")
                e.printStackTrace()
                handleProfileError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserProfile(username: String) {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            try {
                _isLoading.value = true
                _error.value = null

                val result = getUserProfileUseCase(username)
                result
                    .onSuccess { publicProfile ->
                        _publicProfile.value = publicProfile
                        val combinedProfile = publicProfile.toFullProfile()
                        _profileState.value = ProfileUiState.Success(combinedProfile)
                    }
                    .onFailure { e ->
                        handleProfileError(e.message ?: "Failed to load profile")
                    }
            } catch (e: Exception) {
                handleProfileError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMyActivity() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = getMyActivityUseCase()
                result
                    .onSuccess { activities ->
                        _myActivity.value = activities
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load activity"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMyPublicActivity() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = getMyActivityUseCase(publicOnly = true)
                result
                    .onSuccess { activities ->
                        _myPublicActivity.value = activities
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load public activity"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMyRecentActivity(days: Int = 7) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = profileRepository.getMyRecentActivity(days = days, limit = 50)
                result
                    .onSuccess { activities ->
                        _myRecentActivity.value = activities
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load recent activity"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserRecentActivity(username: String, days: Int = 7) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result =
                    profileRepository.getUserRecentActivity(username, days = days, limit = 50)
                result
                    .onSuccess { activities ->
                        _myRecentActivity.value = activities
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load user activity"
                        if (e.message?.contains("private", ignoreCase = true) == true ||
                            e.message?.contains("403", ignoreCase = true) == true
                        ) {
                            _myRecentActivity.value = emptyList()
                        }
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadActivityStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = profileRepository.getMyActivityStats()
                result
                    .onSuccess { stats ->
                        _activityStats.value = stats
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load activity stats"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMyStats() {
        viewModelScope.launch {
            _statsState.value = StatsUiState.Loading
            try {
                val result = getMyStatsUseCase()
                result
                    .onSuccess { stats ->
                        _currentStats.value = stats
                        _statsState.value = StatsUiState.Success(stats)
                    }
                    .onFailure { e ->
                        handleStatsError(e.message ?: "Failed to load stats")
                    }
            } catch (e: Exception) {
                handleStatsError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadAuthorStats(authorId: String) {
        viewModelScope.launch {
            _statsState.value = StatsUiState.Loading
            try {
                val result = getAuthorStatsUseCase(authorId)
                result
                    .onSuccess { stats ->
                        val userStats = UserStatsResponse(
                            user = stats.author,
                            stats = stats.stats
                        )
                        _currentStats.value = userStats
                        _statsState.value = StatsUiState.Success(userStats)
                    }
                    .onFailure { e ->
                        handleStatsError(e.message ?: "Failed to load stats")
                    }
            } catch (e: Exception) {
                handleStatsError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadPublicUserProfile(username: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = profileRepository.getPublicUserProfile(username)
                result
                    .onSuccess { profile ->
                        _publicProfile.value = profile
                        _error.value = null
                    }
                    .onFailure { e ->
                        _publicProfile.value = null
                        _error.value = null
                    }
            } catch (e: Exception) {
                _publicProfile.value = null
                _error.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(
        username: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        birthDate: String? = null,
        bio: String? = null,
        avatarUrl: String? = null
    ) {
        viewModelScope.launch {
            _profileEditState.value = ProfileEditUiState.Loading
            try {
                val request = UpdateProfileRequest(
                    username = username?.takeIf { it.isNotBlank() },
                    firstName = firstName?.takeIf { it.isNotBlank() },
                    lastName = lastName?.takeIf { it.isNotBlank() },
                    birthDate = birthDate?.takeIf { it.isNotBlank() },
                    bio = bio?.takeIf { it.isNotBlank() },
                    avatarUrl = avatarUrl?.takeIf { it.isNotBlank() }
                )
                val result = profileRepository.updateMyProfile(request)
                result
                    .onSuccess { profile ->
                        onProfileLoaded(profile)
                        _profileEditState.value = ProfileEditUiState.Success
                        _snackbarEvent.emit("Profile updated successfully")
                    }
                    .onFailure { e ->
                        handleProfileEditError(e.message ?: "Failed to update profile")
                    }
            } catch (e: Exception) {
                handleProfileEditError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateSocialMedia(socialMedia: SocialMediaResponse) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val customLinks = socialMedia.custom?.map {
                    CustomSocialLink(platform = it.platform, url = it.url)
                }
                val request = UpdateSocialMediaRequest(
                    instagram = socialMedia.instagram,
                    tiktok = socialMedia.tiktok,
                    youtube = socialMedia.youtube,
                    goodreads = socialMedia.goodreads,
                    custom = customLinks
                )
                val result = profileRepository.updateSocialMedia(request)
                result
                    .onSuccess { profile ->
                        onProfileLoaded(profile)
                        loadMyProfile()
                        _snackbarEvent.emit("Social media updated successfully")
                    }
                    .onFailure { e ->
                        emitErrorMessage(e.message ?: "Failed to update social media")
                    }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePrivacySettings(
        activityPrivacy: String? = null,
        profilePrivacy: String? = null,
        readingListPrivacy: String? = null,
        reviewsPrivacy: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val request = UpdatePrivacyRequest(
                    activityPrivacy = activityPrivacy,
                    profilePrivacy = profilePrivacy,
                    readingListPrivacy = readingListPrivacy,
                    reviewsPrivacy = reviewsPrivacy
                )
                val result = profileRepository.updatePrivacySettings(request)
                result
                    .onSuccess { profile ->
                        onProfileLoaded(profile)
                        _snackbarEvent.emit("Privacy settings updated")
                    }
                    .onFailure { e ->
                        emitErrorMessage(e.message ?: "Failed to update privacy settings")
                    }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNotificationSettings(
        notificationsEnabled: Boolean? = null,
        emailNotifications: Boolean? = null,
        notificationPreferences: NotificationPreferencesResponse? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val preferencesRequest = notificationPreferences?.let {
                    NotificationPreferencesRequest(
                        friendRequests = it.friendRequests,
                        friendRequestAccepted = it.friendRequestAccepted,
                        applicationApproved = it.applicationApproved,
                        applicationRejected = it.applicationRejected,
                        reviewDeadlineReminders = it.reviewDeadlineReminders,
                        authorBookPublished = it.authorBookPublished
                    )
                }
                val request = UpdateNotificationSettingsRequest(
                    notificationsEnabled = notificationsEnabled,
                    emailNotifications = emailNotifications,
                    notificationPreferences = preferencesRequest
                )
                val result = profileRepository.updateNotificationSettings(request)
                result
                    .onSuccess { profile ->
                        onProfileLoaded(profile)
                        _snackbarEvent.emit("Notification settings updated")
                    }
                    .onFailure { e ->
                        emitErrorMessage(e.message ?: "Failed to update notification settings")
                    }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(
        firstName: String? = null,
        lastName: String? = null,
        birthDate: String? = null,
        bio: String? = null,
        avatarUrl: String? = null
    ) {
        viewModelScope.launch {
            _profileEditState.value = ProfileEditUiState.Loading
            try {
                val request = UpdateUserProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    birthDate = birthDate,
                    bio = bio,
                    avatarUrl = avatarUrl
                )
                val result = profileRepository.updateUserProfile("", request)
                result
                    .onSuccess {
                        loadMyProfile()
                        _profileEditState.value = ProfileEditUiState.Success
                        _snackbarEvent.emit("Account settings updated successfully")
                    }
                    .onFailure { e ->
                        handleProfileEditError(e.message ?: "Failed to update account settings")
                    }
            } catch (e: Exception) {
                handleProfileEditError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun loadAddresses() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = profileRepository.getMyAddresses()
                result
                    .onSuccess { addresses ->
                        _addresses.value = addresses
                    }
                    .onFailure { e ->
                        emitErrorMessage(e.message ?: "Failed to load addresses")
                    }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAddress(
        streetAddress: String,
        city: String,
        postalCode: String,
        country: String,
        isPrimary: Boolean
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val request = CreateAddressRequest(
                    streetAddress = streetAddress,
                    city = city,
                    postalCode = postalCode,
                    country = country,
                    isPrimary = isPrimary
                )
                val result = profileRepository.addAddress(request)
                result
                    .onSuccess {
                        loadAddresses()
                        _snackbarEvent.emit("Address added successfully")
                    }
                    .onFailure { e ->
                        emitErrorMessage(e.message ?: "Failed to add address")
                    }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAddress(
        addressId: String,
        streetAddress: String? = null,
        city: String? = null,
        postalCode: String? = null,
        country: String? = null,
        isPrimary: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val request = UpdateAddressRequest(
                    streetAddress = streetAddress,
                    city = city,
                    postalCode = postalCode,
                    country = country,
                    isPrimary = isPrimary
                )
                val result = profileRepository.updateAddress(addressId, request)
                result
                    .onSuccess {
                        loadAddresses()
                        _snackbarEvent.emit("Address updated successfully")
                    }
                    .onFailure { e ->
                        emitErrorMessage(e.message ?: "Failed to update address")
                    }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = profileRepository.deleteAddress(addressId)
                result
                    .onSuccess {
                        loadAddresses()
                        _snackbarEvent.emit("Address deleted successfully")
                    }
                    .onFailure { e ->
                        emitErrorMessage(e.message ?: "Failed to delete address")
                    }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadProfileImage(context: Context, imageUri: Uri, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val mimeType = withContext(Dispatchers.IO) {
                    context.contentResolver.getType(imageUri) ?: "image/png"
                }

                val file = withContext(Dispatchers.IO) {
                    uriToFile(context, imageUri, mimeType)
                } ?: run {
                    emitErrorMessage("Failed to process image file")
                    return@launch
                }

                try {
                    val finalMimeType = when {
                        mimeType.isNotEmpty() && mimeType.startsWith("image/") -> mimeType
                        else -> {
                            val extension = file.name.substringAfterLast('.', "").lowercase()
                            when (extension) {
                                "jpg", "jpeg" -> "image/jpeg"
                                "png" -> "image/png"
                                "gif" -> "image/gif"
                                "webp" -> "image/webp"
                                else -> "image/png"
                            }
                        }
                    }

                    val requestFile = file.asRequestBody(finalMimeType.toMediaType())
                    val multipartBody =
                        MultipartBody.Part.createFormData("avatar", file.name, requestFile)

                    val result = uploadProfileImageUseCase(multipartBody)
                    result
                        .onSuccess { avatarUrl ->
                            onSuccess(avatarUrl)
                            _snackbarEvent.emit("Image uploaded successfully")
                            loadMyProfile()
                            try {
                                val profilesService = org.koin.core.context.GlobalContext.get()
                                    .get<com.example.booknest.data.service.ProfilesService>()
                                val response = profilesService.getMe()
                                if (response.isSuccessful) {
                                    response.body()?.let { user ->
                                        sessionManager.updateUser(user)
                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                        .onFailure { e ->
                            emitErrorMessage(e.message ?: "Failed to upload image")
                        }
                } finally {
                    withContext(Dispatchers.IO) {
                        try {
                            if (file.exists()) {
                                file.delete()
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeAvatar(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = profileRepository.removeAvatar()
                result
                    .onSuccess { user ->
                        sessionManager.updateUser(user)
                        loadMyProfile()
                        _snackbarEvent.emit("Avatar removed successfully")
                        onSuccess()
                    }
                    .onFailure { e ->
                        val errorMsg = e.message ?: "Failed to remove avatar"
                        emitErrorMessage(errorMsg)
                        onError(errorMsg)
                    }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                emitErrorMessage(errorMsg)
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun uriToFile(context: Context, uri: Uri, mimeType: String): File? =
        withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val extension = when {
                        mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
                        mimeType.contains("png") -> "png"
                        mimeType.contains("gif") -> "gif"
                        mimeType.contains("webp") -> "webp"
                        else -> {
                            val displayName = try {
                                context.contentResolver.query(uri, null, null, null, null)
                                    ?.use { cursor ->
                                        val nameIndex =
                                            cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                        if (nameIndex >= 0 && cursor.moveToFirst()) {
                                            cursor.getString(nameIndex)
                                        } else null
                                    }
                            } catch (e: Exception) {
                                null
                            }
                            displayName?.substringAfterLast('.', "")?.lowercase()
                                ?.takeIf { it in listOf("jpg", "jpeg", "png", "gif", "webp") }
                                ?: "png"
                        }
                    }

                    val file = File(
                        context.cacheDir,
                        "profile_image_${System.currentTimeMillis()}.$extension"
                    )
                    FileOutputStream(file).use { output ->
                        stream.copyTo(output)
                    }
                    file
                }
            } catch (e: Exception) {
                null
            }
        }

    fun loadAuthorBooks(authorId: String, authorName: String?) {
        viewModelScope.launch {
            try {
                _authorBooksLoading.value = true
                _error.value = null

                val result = browseBooksUseCase(
                    query = null,
                    genres = null,
                    title = null,
                    authorName = authorName,
                    authorId = authorId,
                    seriesName = null,
                    seriesId = null,
                    ageRating = null,
                    distributionType = null,
                    publishedFrom = null,
                    publishedTo = null,
                    createdFrom = null,
                    createdTo = null,
                    minAvgRating = null,
                    maxAvgRating = null,
                    skip = null,
                    take = 100,
                    status = null
                )

                result
                    .onSuccess { books ->
                        _authorBooks.value = books
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load author books"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                println("Author books exception: ${e.message}")
            } finally {
                _authorBooksLoading.value = false
            }
        }
    }

    private fun onProfileLoaded(profile: UserProfileResponse) {
        println("DEBUG: onProfileLoaded called with profile: username=${profile.username}, firstName=${profile.firstName}, lastName=${profile.lastName}")
        _myProfile.value = profile
        _currentProfile.value = profile
        _profileState.value = ProfileUiState.Success(profile)
        println("DEBUG: Profile state updated to Success")
    }

    private suspend fun handleProfileError(message: String) {
        _profileState.value = ProfileUiState.Error(message)
        emitErrorMessage(message)
    }

    private suspend fun handleProfileEditError(message: String) {
        _profileEditState.value = ProfileEditUiState.Error(message)
        emitErrorMessage(message)
    }

    private suspend fun handleStatsError(message: String) {
        _statsState.value = StatsUiState.Error(message)
        emitErrorMessage(message)
    }

    private suspend fun emitErrorMessage(message: String) {
        _error.value = message
        _snackbarEvent.emit(message)
    }
}

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfileResponse) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileEditUiState {
    object Idle : ProfileEditUiState()
    object Loading : ProfileEditUiState()
    object Success : ProfileEditUiState()
    data class Error(val message: String) : ProfileEditUiState()
}

sealed class StatsUiState {
    object Idle : StatsUiState()
    object Loading : StatsUiState()
    data class Success(val stats: UserStatsResponse) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}
