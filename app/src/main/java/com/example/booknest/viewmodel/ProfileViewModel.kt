package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.CreateAddressRequest
import com.example.booknest.domain.model.request.CustomSocialLink
import com.example.booknest.domain.model.enums.NotificationType
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
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.domain.repository.ProfileRepository
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.files.UploadProfileImageUseCase
import com.example.booknest.domain.usecase.profile.AddAddressUseCase
import com.example.booknest.domain.usecase.profile.DeleteAccountUseCase
import com.example.booknest.domain.usecase.profile.DeleteAddressUseCase
import com.example.booknest.domain.usecase.profile.GetAuthorStatsUseCase
import com.example.booknest.domain.usecase.profile.GetMyActivityUseCase
import com.example.booknest.domain.usecase.profile.GetMyAddressesUseCase
import com.example.booknest.domain.usecase.profile.GetMyProfileUseCase
import com.example.booknest.domain.usecase.profile.GetMyRecentActivityUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.domain.usecase.profile.GetPublicUserProfileUseCase
import com.example.booknest.domain.usecase.profile.GetUserProfileUseCase
import com.example.booknest.domain.usecase.profile.GetUserRecentActivityUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.domain.usecase.profile.RemoveAvatarUseCase
import com.example.booknest.domain.usecase.profile.UpdateAddressUseCase
import com.example.booknest.domain.usecase.profile.UpdateMyProfileUseCase
import com.example.booknest.domain.usecase.profile.UpdateNotificationSettingsUseCase
import com.example.booknest.domain.usecase.profile.UpdatePrivacySettingsUseCase
import com.example.booknest.domain.usecase.profile.UpdateSocialMediaUseCase
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
import com.example.booknest.ui.toast.GlobalToastHandler
import com.example.booknest.ui.state.UiState
import com.example.booknest.navigation.NavigationEvent
import com.example.booknest.navigation.Screen

class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val getMyStatsUseCase: GetMyStatsUseCase,
    private val getAuthorStatsUseCase: GetAuthorStatsUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getMyActivityUseCase: GetMyActivityUseCase,
    private val getMyRecentActivityUseCase: GetMyRecentActivityUseCase,
    private val getUserRecentActivityUseCase: GetUserRecentActivityUseCase,
    private val getPublicUserProfileUseCase: GetPublicUserProfileUseCase,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
    private val updateSocialMediaUseCase: UpdateSocialMediaUseCase,
    private val updatePrivacySettingsUseCase: UpdatePrivacySettingsUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    private val getMyAddressesUseCase: GetMyAddressesUseCase,
    private val addAddressUseCase: AddAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val removeAvatarUseCase: RemoveAvatarUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val browseBooksUseCase: BrowseBooksUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val authRepository: AuthRepository,
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

    private val _profileState = MutableStateFlow<UiState<UserProfileResponse>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserProfileResponse>> = _profileState.asStateFlow()

    private val _myActivity = MutableStateFlow<List<UserActivityResponse>>(emptyList())
    val myActivity: StateFlow<List<UserActivityResponse>> = _myActivity.asStateFlow()

    private val _myPublicActivity = MutableStateFlow<List<UserActivityResponse>>(emptyList())
    val myPublicActivity: StateFlow<List<UserActivityResponse>> = _myPublicActivity.asStateFlow()

    private val _myRecentActivity = MutableStateFlow<List<UserActivityResponse>>(emptyList())
    val myRecentActivity: StateFlow<List<UserActivityResponse>> = _myRecentActivity.asStateFlow()

    private val _activityStats = MutableStateFlow<ActivityStatsResponse?>(null)
    val activityStats: StateFlow<ActivityStatsResponse?> = _activityStats.asStateFlow()

    private val _statsState = MutableStateFlow<UiState<UserStatsResponse>>(UiState.Idle)
    val statsState: StateFlow<UiState<UserStatsResponse>> = _statsState.asStateFlow()

    private val _currentStats = MutableStateFlow<UserStatsResponse?>(null)
    val currentStats: StateFlow<UserStatsResponse?> = _currentStats.asStateFlow()

    private val _publicProfile = MutableStateFlow<PublicUserProfileResponse?>(null)
    val publicProfile: StateFlow<PublicUserProfileResponse?> = _publicProfile.asStateFlow()

    private val _profileEditState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val profileEditState: StateFlow<UiState<Unit>> = _profileEditState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(replay = 0)
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private val _avatarRemovalState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val avatarRemovalState: StateFlow<UiState<Unit>> = _avatarRemovalState.asStateFlow()

    private val _authorBooks = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val authorBooks: StateFlow<List<RecommendedBookResponse>> = _authorBooks.asStateFlow()

    private val _authorBooksLoading = MutableStateFlow(false)
    val authorBooksLoading: StateFlow<Boolean> = _authorBooksLoading.asStateFlow()

    fun loadMyProfile() {
        viewModelScope.launch {
            val token = sessionManager.getToken()
            if (token.isEmpty()) {
                println("DEBUG: Cannot load profile - token is empty")
                return@launch
            }

            _profileState.value = UiState.Loading
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
            _profileState.value = UiState.Loading
            try {
                _isLoading.value = true
                _error.value = null

                val result = getUserProfileUseCase(username)
                result
                    .onSuccess { publicProfile ->
                        _publicProfile.value = publicProfile
                        val combinedProfile = publicProfile.toFullProfile()
                        _profileState.value = UiState.Success(combinedProfile)
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

    fun loadMyRecentActivity(days: Int = 7) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = getMyRecentActivityUseCase(days = days, limit = 50)
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
                    getUserRecentActivityUseCase(username, days = days, limit = 50)
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

    fun loadMyStats() {
        viewModelScope.launch {
            _statsState.value = UiState.Loading
            try {
                val result = getMyStatsUseCase()
                result
                    .onSuccess { stats ->
                        _currentStats.value = stats
                        _statsState.value = UiState.Success(stats)
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
            _statsState.value = UiState.Loading
            try {
                val result = getAuthorStatsUseCase(authorId)
                result
                    .onSuccess { stats ->
                        val userStats = UserStatsResponse(
                            user = stats.author,
                            stats = stats.stats
                        )
                        _currentStats.value = userStats
                        _statsState.value = UiState.Success(userStats)
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

                val result = getPublicUserProfileUseCase(username)
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
            _profileEditState.value = UiState.Loading
            try {
                val request = UpdateProfileRequest(
                    username = username?.takeIf { it.isNotBlank() },
                    firstName = firstName?.takeIf { it.isNotBlank() },
                    lastName = lastName?.takeIf { it.isNotBlank() },
                    birthDate = birthDate?.takeIf { it.isNotBlank() },
                    bio = bio?.takeIf { it.isNotBlank() },
                    avatarUrl = avatarUrl?.takeIf { it.isNotBlank() }
                )
                val result = updateMyProfileUseCase(request)
                result
                    .onSuccess { profile ->
                        onProfileLoaded(profile)
                        _profileEditState.value = UiState.Success(Unit)
                        GlobalToastHandler.showSuccess("Profile updated successfully")
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
                val result = updateSocialMediaUseCase(request)
                result
                    .onSuccess { profile ->
                        onProfileLoaded(profile)
                        loadMyProfile()
                        GlobalToastHandler.showSuccess("Social media updated successfully")
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
                val result = updatePrivacySettingsUseCase(request)
                result
                    .onSuccess { profile ->
                        onProfileLoaded(profile)
                        GlobalToastHandler.showSuccess("Privacy settings updated")
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
        notificationPreferences: List<String>? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val request = UpdateNotificationSettingsRequest(
                    notificationsEnabled = notificationsEnabled,
                    emailNotifications = emailNotifications,
                    notificationPreferences = notificationPreferences
                )
                val result = updateNotificationSettingsUseCase(request)
                result
                    .onSuccess { profile ->
                        onProfileLoaded(profile)
                        GlobalToastHandler.showSuccess("Notification settings updated")
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
    
    fun convertNotificationTypesToBooleans(types: List<String>?): Map<String, Boolean> {
        if (types == null) {
            return mapOf(
                NotificationType.FRIEND_REQUEST_RECEIVED to true,
                NotificationType.FRIEND_REQUEST_ACCEPTED to true,
                NotificationType.FRIEND_REQUEST_DECLINED to true,
                NotificationType.APPLICATION_APPROVED to true,
                NotificationType.APPLICATION_REJECTED to true,
                NotificationType.REVIEW_DEADLINE_REMINDER to true,
                NotificationType.AUTHOR_BOOK_PUBLISHED to true
            )
        }
        return mapOf(
            NotificationType.FRIEND_REQUEST_RECEIVED to types.contains(NotificationType.FRIEND_REQUEST_RECEIVED),
            NotificationType.FRIEND_REQUEST_ACCEPTED to types.contains(NotificationType.FRIEND_REQUEST_ACCEPTED),
            NotificationType.FRIEND_REQUEST_DECLINED to types.contains(NotificationType.FRIEND_REQUEST_DECLINED),
            NotificationType.APPLICATION_APPROVED to types.contains(NotificationType.APPLICATION_APPROVED),
            NotificationType.APPLICATION_REJECTED to types.contains(NotificationType.APPLICATION_REJECTED),
            NotificationType.REVIEW_DEADLINE_REMINDER to types.contains(NotificationType.REVIEW_DEADLINE_REMINDER),
            NotificationType.AUTHOR_BOOK_PUBLISHED to types.contains(NotificationType.AUTHOR_BOOK_PUBLISHED)
        )
    }
    
    fun convertBooleansToNotificationTypes(prefs: Map<String, Boolean>): List<String> {
        return prefs.filter { it.value }.keys.toList()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = getMyAddressesUseCase()
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
                val result = addAddressUseCase(request)
                result
                    .onSuccess {
                        loadAddresses()
                        GlobalToastHandler.showSuccess("Address added successfully")
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
                val result = updateAddressUseCase(addressId, request)
                result
                    .onSuccess {
                        loadAddresses()
                        GlobalToastHandler.showSuccess("Address updated successfully")
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

                val result = deleteAddressUseCase(addressId)
                result
                    .onSuccess {
                        loadAddresses()
                        GlobalToastHandler.showSuccess("Address deleted successfully")
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

    fun uploadProfileImage(context: Context, imageUri: Uri) {
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
                            GlobalToastHandler.showSuccess("Image uploaded successfully")
                            loadMyProfile()
                            getCurrentUserUseCase()
                                .onSuccess { user ->
                                    sessionManager.updateUser(user)
                                }
                                .onFailure { e ->
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

    fun removeAvatar() {
        viewModelScope.launch {
            try {
                _avatarRemovalState.value = UiState.Loading
                _isLoading.value = true
                _error.value = null

                val result = removeAvatarUseCase()
                result
                    .onSuccess { user ->
                        sessionManager.updateUser(user)
                        loadMyProfile()
                        GlobalToastHandler.showSuccess("Avatar removed successfully")
                        _avatarRemovalState.value = UiState.Success(Unit)
                    }
                    .onFailure { e ->
                        val errorMsg = e.message ?: "Failed to remove avatar"
                        _avatarRemovalState.value = UiState.Error(errorMsg)
                        emitErrorMessage(errorMsg)
                    }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                _avatarRemovalState.value = UiState.Error(errorMsg)
                emitErrorMessage(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = deleteAccountUseCase()
                result
                    .onSuccess {
                        android.util.Log.d("ProfileViewModel", "Delete account success")
                        GlobalToastHandler.showSuccess("Account deleted successfully")
                        android.util.Log.d("ProfileViewModel", "Calling logout from viewModelScope")
                        sessionManager.logout(authRepository)
                        android.util.Log.d("ProfileViewModel", "Logout completed, emitting navigation event")

                        _navigationEvent.emit(
                            NavigationEvent.NavigateAndClearStack(Screen.Landing.route)
                        )
                    }
                    .onFailure { e ->
                        android.util.Log.e(
                            "ProfileViewModel",
                            "Delete account failure: ${e.message}",
                            e
                        )
                        val errorMsg = e.message ?: "Failed to delete account"
                        emitErrorMessage(errorMsg)
                    }
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Delete account exception", e)
                val errorMsg = e.message ?: "Unknown error occurred"
                emitErrorMessage(errorMsg)
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
        _profileState.value = UiState.Success(profile)
        println("DEBUG: Profile state updated to Success")
    }

    private suspend fun handleProfileError(message: String) {
        _profileState.value = UiState.Error(message)
        emitErrorMessage(message)
    }

    private suspend fun handleProfileEditError(message: String) {
        _profileEditState.value = UiState.Error(message)
        emitErrorMessage(message)
    }

    private suspend fun handleStatsError(message: String) {
        _statsState.value = UiState.Error(message)
        emitErrorMessage(message)
    }

    private suspend fun emitErrorMessage(message: String) {
        _error.value = message
        GlobalToastHandler.showError(message)
    }
}
