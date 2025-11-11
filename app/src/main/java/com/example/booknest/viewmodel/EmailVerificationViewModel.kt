package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.VerifyEmailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmailVerificationUiState(
    val isLoading: Boolean = false,
    val isVerificationSuccessful: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null
)

class EmailVerificationViewModel(
    private val authManager: AuthManager,
    private val userEmail: String? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()
    
    fun verifyEmail(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = authManager.verifyEmail(code)
                result.onSuccess { response ->
                    println("DEBUG: Email verification successful in ViewModel")
                    println("DEBUG: Setting isVerificationSuccessful to true")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isVerificationSuccessful = true
                    )
                    println("DEBUG: State updated - isVerificationSuccessful: ${_uiState.value.isVerificationSuccessful}")
                    println("DEBUG: EmailVerificationViewModel - SUCCESS STATE SET")
                }.onFailure { exception ->
                    println("DEBUG: Email verification failed: ${exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Verification failed"
                    )
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = getErrorMessage(exception.message)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Verification failed"
                )
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = getErrorMessage(e.message)
                )
            }
        }
    }
    
    fun resendVerificationCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = authManager.resendVerificationCode(userEmail)
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = "Verification code sent to your email"
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
    
    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
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

class EmailVerificationViewModelFactory(
    private val authManager: AuthManager,
    private val userEmail: String? = null
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmailVerificationViewModel(authManager, userEmail) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}