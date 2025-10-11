package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.EmailVerificationResponse
import com.example.booknest.network.ResendVerificationResponse
import com.example.booknest.network.VerificationStatusResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmailVerificationUiState(
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class EmailVerificationViewModel(
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()
    
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    
    fun checkVerificationStatus(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authManager.getVerificationStatus(userId)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isVerified = response.emailVerified
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to check verification status"
                    )
                }
        }
    }
    
    fun verifyEmail(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authManager.verifyEmail(token)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isVerified = true,
                        message = response.message
                    )
                    _snackbarMessage.value = response.message ?: "Email verified successfully!"
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Email verification failed"
                    )
                    _snackbarMessage.value = exception.message ?: "Email verification failed"
                }
        }
    }
    
    fun resendVerificationEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authManager.resendVerificationEmail(email)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = response.message
                    )
                    _snackbarMessage.value = response.message ?: "Verification email sent!"
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to resend verification email"
                    )
                    _snackbarMessage.value = exception.message ?: "Failed to resend verification email"
                }
        }
    }
    
    fun clearMessage() {
        _snackbarMessage.value = null
    }
}

class EmailVerificationViewModelFactory(
    private val authManager: AuthManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmailVerificationViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}