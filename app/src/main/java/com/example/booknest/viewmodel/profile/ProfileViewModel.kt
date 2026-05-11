package com.example.booknest.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.UpdateProfileRequest
import com.example.booknest.domain.model.response.PublicUserProfileResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.domain.usecase.files.UploadProfileImageUseCase
import com.example.booknest.domain.usecase.profile.DeleteAccountUseCase
import com.example.booknest.domain.usecase.profile.GetMyProfileUseCase
import com.example.booknest.domain.usecase.profile.GetMyRecentActivityUseCase
import com.example.booknest.domain.usecase.profile.GetPublicUserProfileUseCase
import com.example.booknest.domain.usecase.profile.GetUserProfileUseCase
import com.example.booknest.domain.usecase.profile.GetUserRecentActivityUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.domain.usecase.profile.RemoveAvatarUseCase
import com.example.booknest.domain.usecase.profile.UpdateMyProfileUseCase
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
import com.example.booknest.ui.state.UiState
import com.example.booknest.navigation.NavigationEvent
import com.example.booknest.navigation.Screen

class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getMyRecentActivityUseCase: GetMyRecentActivityUseCase,
    private val getUserRecentActivityUseCase: GetUserRecentActivityUseCase,
    private val getPublicUserProfileUseCase: GetPublicUserProfileUseCase,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
    private val removeAvatarUseCase: RemoveAvatarUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _myProfile = MutableStateFlow<UserProfileResponse?>(null)
    val myProfile: StateFlow<UserProfileResponse?> = _myProfile.asStateFlow()

    private val _currentProfile = MutableStateFlow<UserProfileResponse?>(null)
    val currentProfile: StateFlow<UserProfileResponse?> = _currentProfile.asStateFlow()

    private val _profileState = MutableStateFlow<UiState<UserProfileResponse>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserProfileResponse>> = _profileState.asStateFlow()

    private val _myRecentActivity = MutableStateFlow<List<UserActivityResponse>>(emptyList())
    val myRecentActivity: StateFlow<List<UserActivityResponse>> = _myRecentActivity.asStateFlow()

    private val _publicProfile = MutableStateFlow<PublicUserProfileResponse?>(null)
    val publicProfile: StateFlow<PublicUserProfileResponse?> = _publicProfile.asStateFlow()

    private val _profileEditState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val profileEditState: StateFlow<UiState<Unit>> = _profileEditState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(replay = 0)
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private val _avatarRemovalState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val avatarRemovalState: StateFlow<UiState<Unit>> = _avatarRemovalState.asStateFlow()

    fun loadMyProfile() {
        viewModelScope.launch {
            val token = sessionManager.getToken()
            if (token.isEmpty()) {
                return@launch
            }

            _profileState.value = UiState.Loading
            try {
                _isLoading.value = true
                _error.value = null

                val result = getMyProfileUseCase()
                result
                    .onSuccess { profile ->
                        onProfileLoaded(profile)
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

    fun loadPublicUserProfile(username: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = getPublicUserProfileUseCase(username)
                result
                    .onSuccess { profile ->
                        _publicProfile.value = profile
                    }
                    .onFailure { e ->
                        _publicProfile.value = null
                        _error.value = e.message ?: "Failed to load profile"
                    }
            } catch (e: Exception) {
                _publicProfile.value = null
                _error.value = e.message ?: "Unknown error occurred"
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
                        _successMessage.value = "Profile updated successfully"
                    }
                    .onFailure { e ->
                        handleProfileEditError(e.message ?: "Failed to update profile")
                    }
            } catch (e: Exception) {
                handleProfileEditError(e.message ?: "Unknown error occurred")
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
                            _successMessage.value = "Image uploaded successfully"
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
                        _successMessage.value = "Avatar removed successfully"
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
                        _successMessage.value = "Account deleted successfully"
                        sessionManager.logout(authRepository)

                        _navigationEvent.emit(
                            NavigationEvent.NavigateAndClearStack(Screen.Landing.route)
                        )
                    }
                    .onFailure { e ->
                        emitErrorMessage(e.message ?: "Failed to delete account")
                    }
            } catch (e: Exception) {
                emitErrorMessage(e.message ?: "Unknown error occurred")
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

    private fun onProfileLoaded(profile: UserProfileResponse) {
        _myProfile.value = profile
        _currentProfile.value = profile
        _profileState.value = UiState.Success(profile)
    }

    private suspend fun handleProfileError(message: String) {
        _profileState.value = UiState.Error(message)
        emitErrorMessage(message)
    }

    private suspend fun handleProfileEditError(message: String) {
        _profileEditState.value = UiState.Error(message)
        emitErrorMessage(message)
    }

    private fun emitErrorMessage(message: String) {
        _error.value = message
    }
}
