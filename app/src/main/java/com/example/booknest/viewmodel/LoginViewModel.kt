package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
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

class LoginViewModel(private val authManager: AuthManager) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun loginUser(identifier: String, password: String, onLoginComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val loginRequest = LoginRequest(identifier = identifier, password = password)
                val response = RetrofitInstance.api.login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    println("DEBUG: Login response received - success: ${loginResponse.success}, hasAccessToken: ${!loginResponse.data.accessToken.isNullOrEmpty()}")
                    println("DEBUG: User data: ${loginResponse.data.user}")
                    println("DEBUG: Access token: ${loginResponse.data.accessToken?.take(20)}...")
                    
                    if (loginResponse.success && !loginResponse.data.accessToken.isNullOrEmpty()) {
                        println("DEBUG: Calling authManager.login()")
                        authManager.login(loginResponse)
                        val userName = loginResponse.data.user.username
                        _loginState.value = LoginUiState.Success(loginResponse.message ?: "Welcome $userName! Logged in successfully!")
                        onLoginComplete(true)
                    } else {
                        val errorMessage = loginResponse.message ?: "Login failed"
                        println("DEBUG: Login failed - success: ${loginResponse.success}, message: $errorMessage")
                        _loginState.value = LoginUiState.Error(errorMessage)
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

class LoginViewModelFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
