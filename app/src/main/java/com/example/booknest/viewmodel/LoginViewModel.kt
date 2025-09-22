package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.network.LoginRequest
import com.example.booknest.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val message: String?) : LoginUiState()
    data class Error(val error: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun loginUser(identifier: String, password: String, onLoginComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val loginRequest = LoginRequest(identifier = identifier, password = password)
                val response = RetrofitInstance.api.login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    if (!response.body()!!.accessToken.isNullOrEmpty()) {
                        val userName = response.body()!!.user.username
                        _loginState.value = LoginUiState.Success("Welcome $userName! Logged in successfully!")
                        onLoginComplete(true)
                    } else {
                        _loginState.value = LoginUiState.Error("Login failed: Invalid response from server.")
                        onLoginComplete(false)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrEmpty()) {
                        "Server error: ${response.code()} - ${response.message()}\n${errorBody}"
                    } else {
                        "Server error: ${response.code()} - ${response.message()}"
                    }
                    _loginState.value = LoginUiState.Error(errorMessage)
                    onLoginComplete(false)
                }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error("Network error: ${e.localizedMessage}")
                onLoginComplete(false)
            }
        }
    }
}
