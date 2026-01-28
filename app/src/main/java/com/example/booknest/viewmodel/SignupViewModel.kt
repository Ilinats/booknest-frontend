package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.usecase.auth.RegisterUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.domain.usecase.genres.SaveUserGenrePreferenceUseCase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.example.booknest.domain.model.request.AddressDto
import com.example.booknest.navigation.NavigationEvent
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.toast.GlobalToastHandler
import com.example.booknest.ui.state.UiState

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

data class SignupResult(
    val message: String? = null
)

class SignupViewModel(
    private val sessionManager: SessionManager,
    private val registerUseCase: RegisterUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val saveUserGenrePreferenceUseCase: SaveUserGenrePreferenceUseCase
) : ViewModel() {
    var signupData = SignupData()

    private val _signupState = MutableStateFlow<UiState<SignupResult>>(UiState.Idle)
    val signupState: StateFlow<UiState<SignupResult>> = _signupState

    private val _availableGenres = MutableStateFlow<List<GenreResponse>>(emptyList())
    val availableGenres: StateFlow<List<GenreResponse>> = _availableGenres

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
            signupData.copy(
                firstName = first,
                lastName = last,
                email = email.trim(),
                password = password
            )
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

    fun submitSignup() {
        viewModelScope.launch {
            _signupState.value = UiState.Loading
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

                    saveGenres()

                    _signupState.value = UiState.Success(SignupResult("Registration successful!"))
                    
                    val userEmail = signupData.email
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
                    GlobalToastHandler.showError(exception)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.localizedMessage}"
                _signupState.value = UiState.Error(errorMsg, e)
                GlobalToastHandler.showError(e)
            }
        }
    }

    fun saveGenres() {
        viewModelScope.launch {
            val selectedGenreNames = signupData.genres ?: emptyList()
            if (selectedGenreNames.isEmpty()) {
                return@launch
            }

            val token = sessionManager.getToken()
            if (token.isEmpty()) {
                kotlinx.coroutines.delay(500)
                val tokenAfterDelay = sessionManager.getToken()
                if (tokenAfterDelay.isEmpty()) {
                    GlobalToastHandler.showError("Authentication required. Please complete signup first.")
                    return@launch
                }
            }

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

                if (!allSucceeded) {
                    val errorMsg = firstErrorMessage ?: "Failed to save some genre preferences."
                    GlobalToastHandler.showError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error while saving genres: ${e.localizedMessage}"
                GlobalToastHandler.showError(e)
            }
        }
    }
}

sealed class ImageUploadState
