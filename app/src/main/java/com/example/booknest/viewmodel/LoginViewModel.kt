package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val message: String?) : LoginUiState()
    data class Error(val error: String) : LoginUiState()
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun loginUser(identifier: String, password: String, onLoginComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val result = loginUseCase(identifier, password)
                result
                    .onSuccess { loginResponse ->
                        val accessToken = loginResponse.accessToken
                        if (accessToken.isNotEmpty()) {
                            sessionManager.updateTokens(
                                accessToken = accessToken,
                                refreshToken = loginResponse.refreshToken
                            )

                            try {
                                val profilesService = org.koin.core.context.GlobalContext.get()
                                    .get<com.example.booknest.data.service.ProfilesService>()
                                val userResponse = profilesService.getMe()
                                if (userResponse.isSuccessful) {
                                    userResponse.body()?.let { user ->
                                        println("DEBUG LoginViewModel: Got user after login, userType=${user.userType}")
                                        sessionManager.updateUser(user)
                                    }
                                } else {
                                    println("DEBUG LoginViewModel: Failed to fetch user, response code=${userResponse.code()}")
                                }
                            } catch (e: Exception) {
                                println("DEBUG LoginViewModel: Exception fetching user after login: ${e.message}")
                                e.printStackTrace()
                            }

                            _loginState.value = LoginUiState.Success(
                                "Logged in successfully!"
                            )
                            onLoginComplete(true)
                        } else {
                            _loginState.value =
                                LoginUiState.Error("Login failed: empty access token")
                            onLoginComplete(false)
                        }
                    }
                    .onFailure { throwable ->
                        _loginState.value = LoginUiState.Error(throwable.message ?: "Login failed")
                        onLoginComplete(false)
                    }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error("Network error: ${e.localizedMessage}")
                onLoginComplete(false)
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
}
