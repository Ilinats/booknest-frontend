package com.example.booknest.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.GoogleAuthDataResponse
import com.example.booknest.domain.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoogleAuthUiState())
    val uiState: StateFlow<GoogleAuthUiState> = _uiState.asStateFlow()

    fun authenticateWithGoogle(
        account: GoogleSignInAccount,
        userType: String,
        onSuccess: (GoogleAuthDataResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val idToken = account.idToken
                if (idToken != null) {
                    val result = authRepository.googleLogin(idToken, userType)
                    result.fold(
                        onSuccess = { response ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSignedIn = true,
                                successMessage = "Successfully signed in with Google",
                                currentAccount = account
                            )

                            sessionManager.setAuthEntities(
                                token = response.accessToken,
                                refreshToken = response.refreshToken,
                                userId = response.user.id,
                                username = response.user.username,
                                email = response.user.email ?: "",
                                userType = response.user.userType ?: ""
                            )
                            sessionManager.updateUser(response.user)

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

    fun signOut(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                getGoogleSignInClient(context).signOut()
                sessionManager.logout()

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

    companion object {
        fun getGoogleSignInClient(context: Context): GoogleSignInClient {
            val webClientId = try {
                com.example.booknest.BuildConfig.GOOGLE_WEB_CLIENT_ID
            } catch (e: Exception) {
                null
            }
            
            val gso = if (webClientId != null && webClientId.isNotBlank() && !webClientId.contains("YOUR_")) {
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .requestProfile()
                    .build()
            } else {
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build()
            }

            return GoogleSignIn.getClient(context, gso)
        }
    }
}
