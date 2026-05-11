package com.example.booknest.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.usecase.auth.RegisterUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.domain.usecase.genres.SaveUserGenrePreferenceUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.booknest.domain.model.request.AddressDto
import com.example.booknest.navigation.NavigationEvent
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.state.UiState

data class SignupData(
    val userType: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val username: String? = null,
    val birthDate: String? = null,
    val address: AddressDto? = null,
    val bio: String? = null,
    val profilePicture: String? = null,
    val genres: List<String>? = null
)

data class SignupResult(
    val message: String? = null
)

class SignupViewModel(
    private val sessionManager: SessionManager,
    private val registerUseCase: RegisterUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val saveUserGenrePreferenceUseCase: SaveUserGenrePreferenceUseCase
) : ViewModel() {
    private val _signupData = MutableStateFlow(SignupData())
    val signupData: StateFlow<SignupData> = _signupData.asStateFlow()

    private val _signupState = MutableStateFlow<UiState<SignupResult>>(UiState.Idle)
    val signupState: StateFlow<UiState<SignupResult>> = _signupState.asStateFlow()

    private val _availableGenres = MutableStateFlow<List<GenreResponse>>(emptyList())
    val availableGenres: StateFlow<List<GenreResponse>> = _availableGenres.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(replay = 0)
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        fetchAvailableGenres()
    }

    fun fetchAvailableGenres() {
        viewModelScope.launch {
            val result = getGenresUseCase()
            result
                .onSuccess { genres ->
                    _availableGenres.value = genres
                }
                .onFailure { _ ->
                    _availableGenres.value = emptyList()
                }
        }
    }

    fun updateAccountType(type: String) {
        _signupData.update { it.copy(userType = type) }
    }

    fun updatePersonalInfo(first: String, last: String, email: String, password: String) {
        _signupData.update { it.copy(firstName = first, lastName = last, email = email.trim(), password = password) }
    }

    fun updateUsername(username: String) {
        _signupData.update { it.copy(username = username) }
    }

    fun updateBirthDate(birthDate: String?) {
        _signupData.update { it.copy(birthDate = birthDate) }
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
        _signupData.update { it.copy(birthDate = birthDate, address = address) }
    }

    fun updateBio(bio: String?, picture: String?) {
        _signupData.update { it.copy(bio = bio, profilePicture = picture) }
    }

    fun updateGenres(genres: List<String>) {
        _signupData.update { it.copy(genres = genres) }
    }

    fun submitSignup() {
        viewModelScope.launch {
            _signupState.value = UiState.Loading
            try {
                val data = _signupData.value
                val email = data.email?.trim()?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("Email is required")

                val result = registerUseCase(
                    username = data.username ?: "",
                    email = email,
                    password = data.password ?: "",
                    userType = data.userType ?: "reader",
                    firstName = data.firstName ?: "",
                    lastName = data.lastName ?: "",
                    birthDate = data.birthDate,
                    bio = data.bio,
                    avatarUrl = data.profilePicture,
                    address = data.address
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

                    saveGenres()

                    _signupState.value = UiState.Success(SignupResult("Registration successful!"))

                    val userEmail = data.email
                    _navigationEvent.emit(
                        NavigationEvent.NavigateTo(
                            route = Screen.EmailVerification.createRoute(userEmail),
                            popUpTo = Screen.ProfileDetails.route,
                            inclusive = true
                        )
                    )
                }.onFailure { exception ->
                    val errorMsg = exception.message ?: "Registration failed"
                    _signupState.value = UiState.Error(errorMsg, exception)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.localizedMessage}"
                _signupState.value = UiState.Error(errorMsg, e)
            }
        }
    }

    fun saveGenres() {
        viewModelScope.launch {
            val selectedGenreNames = _signupData.value.genres ?: emptyList()
            if (selectedGenreNames.isEmpty()) {
                return@launch
            }

            val token = sessionManager.getToken()
            if (token.isEmpty()) {
                kotlinx.coroutines.delay(500)
                if (sessionManager.getToken().isEmpty()) {
                    return@launch
                }
            }

            val allGenreDtos = _availableGenres.value

            try {
                val results = selectedGenreNames.map { genreName ->
                    async {
                        val genreDto = allGenreDtos.find { it.name == genreName }
                        if (genreDto != null) {
                            saveUserGenrePreferenceUseCase(UpsertPreferenceRequest(genreId = genreDto.id))
                        } else {
                            Result.failure<Unit>(IllegalArgumentException("Selected genre not found in available list."))
                        }
                    }
                }.awaitAll()

                val firstError = results.firstNotNullOfOrNull { r ->
                    r.exceptionOrNull()?.message
                }
                if (firstError != null) {
                    _signupState.value = UiState.Error(firstError)
                }
            } catch (e: Exception) {
                // Genre saving is a non-critical background step; ignore silently
            }
        }
    }
}

sealed class ImageUploadState
