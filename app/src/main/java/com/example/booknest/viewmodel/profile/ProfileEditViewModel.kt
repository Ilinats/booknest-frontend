package com.example.booknest.viewmodel.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.UpdateProfileRequest
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.domain.usecase.files.UploadProfileImageUseCase
import com.example.booknest.domain.usecase.profile.DeleteAccountUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.domain.usecase.profile.RemoveAvatarUseCase
import com.example.booknest.domain.usecase.profile.UpdateMyProfileUseCase
import com.example.booknest.presentation.common.UiState
import com.example.booknest.presentation.effects.ProfileUiEffect
import com.example.booknest.utils.DebugLog
import com.example.booknest.viewmodel.common.UserFeedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileEditViewModel(
    private val feedback: UserFeedback,
    private val sessionManager: SessionManager,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
    private val removeAvatarUseCase: RemoveAvatarUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val authRepository: AuthRepository,
    private val profileRefreshBus: ProfileRefreshBus,
) : ViewModel() {

    private val _profileEditState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val profileEditState: StateFlow<UiState<Unit>> = _profileEditState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    private val _profileUiEffect = MutableSharedFlow<ProfileUiEffect>(replay = 0)
    val profileUiEffect: SharedFlow<ProfileUiEffect> = _profileUiEffect.asSharedFlow()

    private val _avatarRemovalState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val avatarRemovalState: StateFlow<UiState<Unit>> = _avatarRemovalState.asStateFlow()

    fun updateProfile(
        username: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        birthDate: String? = null,
        bio: String? = null,
        avatarUrl: String? = null,
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
                    avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
                )
                updateMyProfileUseCase(request)
                    .onSuccess {
                        _profileEditState.value = UiState.Success(Unit)
                        feedback.success("Profile updated successfully", _successMessage)
                        profileRefreshBus.requestRefresh()
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

                    uploadProfileImageUseCase(multipartBody)
                        .onSuccess {
                            feedback.success("Image uploaded successfully", _successMessage)
                            profileRefreshBus.requestRefresh()
                            getCurrentUserUseCase()
                                .onSuccess { user ->
                                    sessionManager.updateUser(user)
                                }
                                .onFailure { e ->
                                    DebugLog.w(
                                        "ProfileEditVM",
                                        "Avatar saved but refreshing session user failed",
                                        e,
                                    )
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
                            DebugLog.w("ProfileEditVM", "Failed to delete temp avatar file", e)
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

                removeAvatarUseCase()
                    .onSuccess { user ->
                        sessionManager.updateUser(user)
                        profileRefreshBus.requestRefresh()
                        feedback.success("Avatar removed successfully", _successMessage)
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

                deleteAccountUseCase()
                    .onSuccess {
                        feedback.success("Account deleted successfully", _successMessage)
                        sessionManager.logout(authRepository)
                        _profileUiEffect.emit(ProfileUiEffect.NavigateToLandingClearingStack)
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
                                        } else {
                                            null
                                        }
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
                        "profile_image_${System.currentTimeMillis()}.$extension",
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

    private suspend fun handleProfileEditError(message: String) {
        _profileEditState.value = UiState.Error(message)
        emitErrorMessage(message)
    }

    private fun emitErrorMessage(message: String) {
        feedback.error(message, _error)
    }
}
