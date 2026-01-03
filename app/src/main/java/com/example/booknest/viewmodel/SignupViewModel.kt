package com.example.booknest.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.usecase.auth.RegisterUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.domain.usecase.genres.SaveUserGenrePreferenceUseCase
import com.example.booknest.domain.usecase.files.UploadProfileImageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.booknest.domain.model.request.AddressDto

@Serializable
data class SignupData(
    var userType: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var password: String? = null,
    var username: String? = null,
    var birthDate: String? = null,
    var address: AddressDto? = null,
    var bio: String? = null,
    @SerialName("avatarUrl")
    var profilePicture: String? = null,
    var genres: List<String>? = null
)

sealed class SignupUiState {
    object Idle : SignupUiState()
    object Loading : SignupUiState()
    data class Success(val message: String?) : SignupUiState()
    data class Error(val error: String) : SignupUiState()
}

class SignupViewModel(
    private val sessionManager: SessionManager,
    private val registerUseCase: RegisterUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val saveUserGenrePreferenceUseCase: SaveUserGenrePreferenceUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase
) : ViewModel() {
    var signupData = SignupData()
    var pendingImageUri: Uri? = null

    private val _signupState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val signupState: StateFlow<SignupUiState> = _signupState

    private val _availableGenres = MutableStateFlow<List<GenreResponse>>(emptyList())
    val availableGenres: StateFlow<List<GenreResponse>> = _availableGenres

    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState

    init {
        fetchAvailableGenres()
    }

    fun fetchAvailableGenres() {
        viewModelScope.launch {
            val result = getGenresUseCase()
            result
                .onSuccess { genres ->
                    _availableGenres.value = genres
                    println("Genres loaded successfully: ${genres.size} genres")
                }
                .onFailure { e ->
                    _availableGenres.value = emptyList()
                    println("Error fetching genres: ${e.localizedMessage}")
                }
        }
    }

    fun updateAccountType(type: String) {
        signupData = signupData.copy(userType = type)
    }

    fun updatePersonalInfo(first: String, last: String, email: String, password: String) {
        signupData =
            signupData.copy(firstName = first, lastName = last, email = email.trim(), password = password)
    }

    fun updateUsername(username: String) {
        signupData = signupData.copy(username = username)
    }

    fun updateBirthDate(birthDate: String?) {
        signupData = signupData.copy(birthDate = birthDate)
    }

    fun updateProfileDetails(
        birthDate: String?,
        streetAddress: String,
        city: String,
        postalCode: String,
        country: String?,
        isPrimary: Boolean?
    ) {
        val address =
            if (streetAddress.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()) {
                AddressDto(
                    streetAddress = streetAddress,
                    city = city,
                    postalCode = postalCode,
                    country = country,
                    isPrimary = isPrimary
                )
            } else null
        signupData = signupData.copy(
            birthDate = birthDate,
            address = address
        )
    }

    fun updateBio(bio: String?, picture: String?) {
        signupData = signupData.copy(bio = bio, profilePicture = picture)
    }

    fun updateGenres(genres: List<String>) {
        signupData = signupData.copy(genres = genres)
    }

    fun submitSignup(onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _signupState.value = SignupUiState.Loading
            try {
                val email = signupData.email?.trim()?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("Email is required")
                
                val result = registerUseCase(
                    username = signupData.username ?: "",
                    email = email,
                    password = signupData.password ?: "",
                    userType = signupData.userType ?: "reader",
                    firstName = signupData.firstName ?: "",
                    lastName = signupData.lastName ?: "",
                    birthDate = signupData.birthDate,
                    bio = signupData.bio,
                    avatarUrl = signupData.profilePicture,
                    address = signupData.address
                )
                result.onSuccess { response ->
                    sessionManager.setAuthEntities(
                        token = response.accessToken,
                        refreshToken = response.refreshToken,
                        userId = response.user.id,
                        username = response.user.username,
                        email = response.user.email ?: "",
                        userType = response.user.userType ?: ""
                    )
                    sessionManager.updateUser(response.user)

                    _signupState.value = SignupUiState.Success("Registration successful!")
                    onComplete(true, null)
                }.onFailure { exception ->
                    val errorMsg = exception.message ?: "Registration failed"
                    _signupState.value = SignupUiState.Error(errorMsg)
                    onComplete(false, errorMsg)
                }
            } catch (e: Exception) {
                _signupState.value = SignupUiState.Error("Network error: ${e.localizedMessage}")
                onComplete(false, e.localizedMessage)
            }
        }
    }

    fun saveGenres(onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val selectedGenreNames = signupData.genres ?: emptyList()
            if (selectedGenreNames.isEmpty()) {
                onComplete(true, "No genres selected to save.")
                return@launch
            }

            val defaultPreferenceLevel = 3
            var allSucceeded = true
            var firstErrorMessage: String? = null

            val allGenreDtos = _availableGenres.value

            try {
                val deferredResults = selectedGenreNames.map { genreName ->
                    async {
                        val genreDto = allGenreDtos.find { it.name == genreName }
                        if (genreDto != null) {
                            val request = UpsertPreferenceRequest(
                                genreId = genreDto.id
                            )
                            saveUserGenrePreferenceUseCase(request)
                        } else {
                            Result.failure<Unit>(
                                IllegalArgumentException("Selected genre not found in available list.")
                            )
                        }
                    }
                }

                val results = deferredResults.awaitAll()

                results.forEach { result ->
                    result.onFailure { e ->
                        allSucceeded = false
                        if (firstErrorMessage == null) {
                            firstErrorMessage = e.message ?: "Failed to save a genre preference."
                        }
                    }
                }

                if (allSucceeded) {
                    onComplete(true, "All genre preferences saved successfully.")
                } else {
                    onComplete(false, firstErrorMessage ?: "Failed to save some genre preferences.")
                }
            } catch (e: Exception) {
                onComplete(false, "Network error while saving genres: ${e.localizedMessage}")
            }
        }
    }

    fun uploadProfileImage(context: Context, imageUri: Uri, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _imageUploadState.value = ImageUploadState.Uploading

                val file = withContext(Dispatchers.IO) {
                    uriToFile(context, imageUri)
                } ?: run {
                    _imageUploadState.value = ImageUploadState.Error("Failed to process image file")
                    return@launch
                }

                try {
                    val requestFile = file.asRequestBody("image/*".toMediaType())
                    val multipartBody =
                        MultipartBody.Part.createFormData("file", file.name, requestFile)

                    val result = uploadProfileImageUseCase(multipartBody)
                    result
                        .onSuccess { avatarUrl ->
                            onSuccess(avatarUrl)
                            _imageUploadState.value = ImageUploadState.Success(avatarUrl)
                        }
                        .onFailure { e ->
                            _imageUploadState.value = ImageUploadState.Error(
                                e.message ?: "Failed to upload image"
                            )
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
                _imageUploadState.value =
                    ImageUploadState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private suspend fun uriToFile(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { output ->
                    stream.copyTo(output)
                }
                file
            }
        } catch (e: Exception) {
            null
        }
    }

    fun resetState() {
        _signupState.value = SignupUiState.Idle
    }
}

sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Uploading : ImageUploadState()
    data class Success(val url: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}
