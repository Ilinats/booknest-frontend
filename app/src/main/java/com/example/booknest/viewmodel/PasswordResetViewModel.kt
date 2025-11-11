package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
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
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()
    
    fun verifyResetCode(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // For now, we'll just store the code and proceed
                // In a real implementation, you might want to verify the code first
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
    
    fun resetPassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = authManager.resetPassword(newPassword)
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
    
    fun resendResetCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = authManager.requestPasswordReset()
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

class PasswordResetViewModelFactory(
    private val authManager: AuthManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordResetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PasswordResetViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
