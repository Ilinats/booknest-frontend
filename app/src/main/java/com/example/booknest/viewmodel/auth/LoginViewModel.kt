package com.example.booknest.viewmodel.auth

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.usecase.auth.LoginUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.presentation.common.UiState
import com.example.booknest.presentation.effects.AuthUiEffect
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
    private val feedback: UserFeedback,
    private val loginUseCase: LoginUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _loginState = MutableStateFlow<UiState<LoginResult>>(UiState.Idle)
    val loginState: StateFlow<UiState<LoginResult>> = _loginState

    private val _authUiEffect = MutableSharedFlow<AuthUiEffect>(replay = 0)
    val authUiEffect: SharedFlow<AuthUiEffect> = _authUiEffect.asSharedFlow()

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
                                    feedback.success("Logged in successfully")
                                    _loginState.value = UiState.Success(LoginResult("Logged in successfully!"))
                                    _authUiEffect.emit(AuthUiEffect.NavigateToMainClearingStack)
                                }
                                .onFailure { throwable ->
                                    val errorMessage = throwable.message ?: "Failed to fetch user data"
                                    feedback.error(throwable)
                                    _loginState.value = UiState.Error(errorMessage, throwable)
                                }
                        } else {
                            val errorMessage = "Login failed: empty access token"
                            feedback.error(errorMessage)
                            _loginState.value = UiState.Error(errorMessage)
                        }
                    }
                    .onFailure { throwable ->
                        val errorMessage = throwable.message ?: "Login failed"
                        feedback.error(throwable)
                        _loginState.value = UiState.Error(errorMessage, throwable)
                    }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.localizedMessage}"
                feedback.error(errorMessage)
                _loginState.value = UiState.Error(errorMessage, e)
            }
        }
    }
}
