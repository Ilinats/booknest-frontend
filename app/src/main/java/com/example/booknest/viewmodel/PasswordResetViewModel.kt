package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.usecase.auth.RequestPasswordResetUseCase
import com.example.booknest.domain.usecase.auth.ResetPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PasswordResetUiState(
    val isLoading: Boolean = false,
    val isPasswordResetSuccessful: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null
)

class PasswordResetViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()

    private var verificationCode: String = ""

    fun verifyResetCode(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                verificationCode = code
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Code verification failed"
                )
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = getErrorMessage(e.message)
                )
            }
        }
    }

    fun resetPassword(code: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = resetPasswordUseCase(code, newPassword)
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPasswordResetSuccessful = true
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Password reset failed"
                    )
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = getErrorMessage(exception.message)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Password reset failed"
                )
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = getErrorMessage(e.message)
                )
            }
        }
    }

    fun resendResetCode(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = requestPasswordResetUseCase(email)
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = "Reset code sent to your email"
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to resend code"
                    )
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = getErrorMessage(exception.message)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to resend code"
                )
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = getErrorMessage(e.message)
                )
            }
        }
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            snackbarMessage = message
        )
    }

    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    private fun getErrorMessage(error: String?): String {
        return when {
            error?.contains("Invalid or expired verification code", ignoreCase = true) == true ->
                "Code is invalid or expired. Please try again."

            error?.contains("Code must be exactly 6 digits", ignoreCase = true) == true ->
                "Please enter a 6-digit code"

            error?.contains("User not found", ignoreCase = true) == true ->
                "No account found with this email"

            error?.contains("Too many requests", ignoreCase = true) == true ->
                "Too many attempts. Please wait before trying again."

            error?.contains("Password too weak", ignoreCase = true) == true ->
                "Password must be at least 6 characters long"

            else -> error ?: "An error occurred. Please try again."
        }
    }
}
