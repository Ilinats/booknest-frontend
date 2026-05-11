package com.example.booknest.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.usecase.auth.LoginUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.navigation.NavigationEvent
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.state.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class LoginResult(
    val message: String? = null
)

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _loginState = MutableStateFlow<UiState<LoginResult>>(UiState.Idle)
    val loginState: StateFlow<UiState<LoginResult>> = _loginState

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(replay = 0)
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    fun loginUser(identifier: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
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

                            getCurrentUserUseCase()
                                .onSuccess { user ->
                                    sessionManager.updateUser(user)
                                    _loginState.value = UiState.Success(LoginResult("Logged in successfully!"))
                                    _navigationEvent.emit(
                                        NavigationEvent.NavigateAndClearStack(Screen.Main.route)
                                    )
                                }
                                .onFailure { throwable ->
                                    val errorMessage = throwable.message ?: "Failed to fetch user data"
                                    _loginState.value = UiState.Error(errorMessage, throwable)
                                }
                        } else {
                            val errorMessage = "Login failed: empty access token"
                            _loginState.value = UiState.Error(errorMessage)
                        }
                    }
                    .onFailure { throwable ->
                        val errorMessage = throwable.message ?: "Login failed"
                        _loginState.value = UiState.Error(errorMessage, throwable)
                    }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.localizedMessage}"
                _loginState.value = UiState.Error(errorMessage, e)
            }
        }
    }
}
