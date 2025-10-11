package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.data.GoogleAuthManager
import com.example.booknest.network.GoogleAuthResponse
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GoogleAuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentAccount: GoogleSignInAccount? = null
)

class GoogleAuthViewModel(
    private val googleAuthManager: GoogleAuthManager,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GoogleAuthUiState())
    val uiState: StateFlow<GoogleAuthUiState> = _uiState.asStateFlow()
    
    init {
        checkSignInStatus()
    }
    
    private fun checkSignInStatus() {
        val account = googleAuthManager.getLastSignedInAccount()
        _uiState.value = _uiState.value.copy(
            isSignedIn = googleAuthManager.isSignedIn(),
            currentAccount = account
        )
    }
    
    fun authenticateWithGoogle(
        account: GoogleSignInAccount,
        userType: String,
        onSuccess: (GoogleAuthResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val idToken = account.idToken
                if (idToken != null) {
                    val result = googleAuthManager.authenticateWithBackend(idToken, userType)
                    result.fold(
                        onSuccess = { response ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSignedIn = true,
                                successMessage = response.message ?: "Successfully signed in with Google",
                                currentAccount = account
                            )
                            
                            // Store user data and tokens in AuthManager
                            if (response.data != null) {
                                authManager.login(
                                    com.example.booknest.network.LoginSuccessResponse(
                                        success = response.success,
                                        message = response.message,
                                        data = com.example.booknest.network.LoginData(
                                            user = response.data.user,
                                            accessToken = response.data.accessToken,
                                            refreshToken = response.data.refreshToken
                                        )
                                    )
                                )
                            }
                            
                            onSuccess(response)
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "Authentication failed"
                            )
                            onError(exception.message ?: "Authentication failed")
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No ID token received from Google"
                    )
                    onError("No ID token received from Google")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred during authentication"
                )
                onError(e.message ?: "An error occurred during authentication")
            }
        }
    }
    
    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                googleAuthManager.signOut()
                authManager.logout()
                
                _uiState.value = _uiState.value.copy(
                    isSignedIn = false,
                    currentAccount = null,
                    successMessage = "Successfully signed out"
                )
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Sign out failed"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    fun getCurrentAccount(): GoogleSignInAccount? {
        return _uiState.value.currentAccount
    }
}

class GoogleAuthViewModelFactory(
    private val googleAuthManager: GoogleAuthManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoogleAuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoogleAuthViewModel(googleAuthManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
