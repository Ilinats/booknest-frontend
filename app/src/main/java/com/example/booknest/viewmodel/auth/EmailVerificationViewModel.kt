package com.example.booknest.viewmodel.auth

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.usecase.auth.ResendVerificationCodeUseCase
import com.example.booknest.domain.usecase.auth.VerifyEmailUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmailVerificationUiState(
    val isLoading: Boolean = false,
    val isVerificationSuccessful: Boolean = false,
    val error: String? = null
)

class EmailVerificationViewModel(
    private val feedback: UserFeedback,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val resendVerificationCodeUseCase: ResendVerificationCodeUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sessionManager: SessionManager,
    private val userEmail: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    fun verifyEmail(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = verifyEmailUseCase(code)
                result.onSuccess { verifiedUser ->
                    sessionManager.updateUser(verifiedUser)
                    getCurrentUserUseCase()
                        .onSuccess { freshUser ->
                            sessionManager.updateUser(freshUser)
                        }
                    feedback.success("Email verified successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isVerificationSuccessful = true,
                        error = null
                    )
                }.onFailure { exception ->
                    val message = getErrorMessage(exception.message)
                    feedback.error(message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = message
                    )
                }
            } catch (e: Exception) {
                val message = getErrorMessage(e.message)
                feedback.error(message)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = message
                )
            }
        }
    }

    fun resendVerificationCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val email = userEmail
            if (email.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Email is required"
                )
                return@launch
            }

            try {
                val result = resendVerificationCodeUseCase(email)
                result.onSuccess {
                    feedback.success("Verification code sent")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                }.onFailure { exception ->
                    val message = getErrorMessage(exception.message)
                    feedback.error(message)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = message
                    )
                }
            } catch (e: Exception) {
                val message = getErrorMessage(e.message)
                feedback.error(message)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = message
                )
            }
        }
    }


    private fun getErrorMessage(error: String?): String {
        return when {
            error?.contains("Invalid or expired verification code", ignoreCase = true) == true ->
                "Code is invalid or expired. Please try again."

            error?.contains("Code must be exactly 6 digits", ignoreCase = true) == true ->
                "Please enter a 6-digit code"

            error?.contains("Email is already verified", ignoreCase = true) == true ->
                "Email is already verified"

            error?.contains("User not found", ignoreCase = true) == true ->
                "No account found with this email"

            error?.contains("Too many requests", ignoreCase = true) == true ->
                "Too many attempts. Please wait before trying again."

            else -> error ?: "An error occurred. Please try again."
        }
    }
}