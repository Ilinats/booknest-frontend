package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.network.CreateGenreRequest
import com.example.booknest.network.GenreDto
import com.example.booknest.network.RetrofitInstance
import com.example.booknest.network.TokenStorage
import com.example.booknest.network.UpsertPreferenceRequest
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddressDto(
    @SerializedName("streetAddress")
    var streetAddress: String,
    @SerializedName("city")
    var city: String,
    @SerializedName("postalCode")
    var postalCode: String,
    @SerializedName("country")
    var country: String? = null,
    @SerializedName("isPrimary")
    var isPrimary: Boolean? = true
)

data class SignupData(
    @SerializedName("userType")
    var accountType: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var password: String? = null,
    var username: String? = null,
    var birthDate: String? = null,
    var address: AddressDto? = null,
    var bio: String? = null,
    @SerializedName("avatarUrl")
    var profilePicture: String? = null,
    var genres: List<String>? = null
)

sealed class SignupUiState {
    object Idle : SignupUiState()
    object Loading : SignupUiState()
    data class Success(val message: String?) : SignupUiState()
    data class Error(val error: String) : SignupUiState()
}

sealed class CreateGenreUiState {
    object Idle : CreateGenreUiState()
    object Loading : CreateGenreUiState()
    data class Success(val message: String) : CreateGenreUiState()
    data class Error(val error: String) : CreateGenreUiState()
}

class SignupViewModel : ViewModel() {
    var signupData = SignupData()

    private val _signupState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val signupState: StateFlow<SignupUiState> = _signupState

    private val _createGenreUiState = MutableStateFlow<CreateGenreUiState>(CreateGenreUiState.Idle)
    val createGenreUiState: StateFlow<CreateGenreUiState> = _createGenreUiState

    private val _availableGenres = MutableStateFlow<List<GenreDto>>(emptyList())
    val availableGenres: StateFlow<List<GenreDto>> = _availableGenres

    init {
        fetchAvailableGenres()
    }

    fun fetchAvailableGenres() {
        viewModelScope.launch {
            try {
                val genresResponse = RetrofitInstance.api.getGenres()
                _availableGenres.value = genresResponse
            } catch (e: Exception) {
                _availableGenres.value = emptyList()
                println("Error fetching genres: ${e.localizedMessage}")
            }
        }
    }

    fun createGenre(
        name: String,
        description: String?,
        colorCode: String?,
        icon: String?,
        isActive: Boolean?
    ) {
        viewModelScope.launch {
            _createGenreUiState.value = CreateGenreUiState.Loading
            try {
                val createGenreRequest = CreateGenreRequest(
                    name = name,
                    description = description,
                    colorCode = colorCode,
                    icon = icon,
                    isActive = isActive
                )
                val response = RetrofitInstance.api.addGenre(createGenreRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    _createGenreUiState.value = CreateGenreUiState.Success(response.body()?.message ?: "Genre created successfully!")
                    fetchAvailableGenres()
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to create genre: ${response.code()}"
                    _createGenreUiState.value = CreateGenreUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _createGenreUiState.value = CreateGenreUiState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun resetCreateGenreState() {
        _createGenreUiState.value = CreateGenreUiState.Idle
    }

    fun updateAccountType(type: String) {
        signupData = signupData.copy(accountType = type)
    }

    fun updatePersonalInfo(first: String, last: String, email: String, password: String) {
        signupData = signupData.copy(firstName = first, lastName = last, email = email, password = password)
    }

    fun updateProfileDetails(
        username: String,
        birthDate: String,
        streetAddress: String,
        city: String,
        postalCode: String,
        country: String?,
        isPrimary: Boolean?
    ) {
        val address = if (streetAddress.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()) {
            AddressDto(
                streetAddress = streetAddress,
                city = city,
                postalCode = postalCode,
                country = country,
                isPrimary = isPrimary
            )
        } else null
        signupData = signupData.copy(
            username = username,
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
                val response = RetrofitInstance.api.register(signupData)
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()?.accessToken
                    if (!token.isNullOrEmpty()) {
                        TokenStorage.saveToken(token)
                    }
                    _signupState.value = SignupUiState.Success(response.body()?.message)
                    onComplete(true, null)
                } else {
                    _signupState.value = SignupUiState.Error("Server error: ${response.code()}")
                    onComplete(false, "Server error")
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

            try {
                val allGenreDtos = _availableGenres.value
                val deferredResponses = selectedGenreNames.map {
                    genreName ->
                    async { 
                        val genreDto = allGenreDtos.find { it.name == genreName }
                        if (genreDto != null) {
                            val request = UpsertPreferenceRequest(genreId = genreDto.id, preferenceLevel = defaultPreferenceLevel)
                            RetrofitInstance.api.saveUserGenres(request)
                        } else {
                           null
                        }
                    }
                }

                val responses = deferredResponses.awaitAll()

                for (response in responses) {
                    if (response == null) { 
                        allSucceeded = false
                        firstErrorMessage = firstErrorMessage ?: "Selected genre not found in available list."
                        continue
                    }
                    if (!response.isSuccessful) {
                        allSucceeded = false
                        val errorBodyMessage = response.body()?.message
                        firstErrorMessage = firstErrorMessage ?: errorBodyMessage ?: "Failed to save a genre preference: ${response.code()}"
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
}
