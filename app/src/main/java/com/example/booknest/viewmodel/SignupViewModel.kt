package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.network.GenreRequest
import com.example.booknest.network.RetrofitInstance
import com.google.gson.annotations.SerializedName
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

class SignupViewModel : ViewModel() {
    var signupData = SignupData()
    private val _signupState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val signupState: StateFlow<SignupUiState> = _signupState
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
                    _signupState.value = SignupUiState.Success(response.body()!!.message)
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
            try {
                val genres = signupData.genres ?: emptyList()
                val request = GenreRequest(username = signupData.username ?: "", genres = genres)
                val response = RetrofitInstance.api.saveUserGenres(request)

                if (response.isSuccessful && response.body() != null) {
                    onComplete(true, null)
                } else {
                    onComplete(false, "Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage)
            }
        }
    }
}

sealed class SignupUiState {
    object Idle : SignupUiState()
    object Loading : SignupUiState()
    data class Success(val message: String?) : SignupUiState()
    data class Error(val error: String) : SignupUiState()
}
