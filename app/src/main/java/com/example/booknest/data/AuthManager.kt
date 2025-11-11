package com.example.booknest.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.network.LoginSuccessResponse
import com.example.booknest.network.TokenCache
import com.example.booknest.network.UserData
import com.example.booknest.network.EmailVerificationRequest
import com.example.booknest.network.EmailVerificationResponse
import com.example.booknest.network.VerificationStatusResponse
import com.example.booknest.network.ResendVerificationRequest
import com.example.booknest.network.ResendVerificationResponse
import com.example.booknest.network.GoogleAuthRequest
import com.example.booknest.network.GoogleAuthResponse
import com.example.booknest.network.VerifyEmailDto
import com.example.booknest.network.RequestPasswordResetDto
import com.example.booknest.network.ResetPasswordDto
import com.example.booknest.network.AuthResponse
import com.example.booknest.network.ApiService
import retrofit2.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthManager(private val userRepository: UserRepository, private val apiService: ApiService) {
    
    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    // Create a coroutine scope for the AuthManager
    private val authScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Token manager for handling refresh logic
    private val tokenManager = TokenManager()
    
    init {
        // Initialize current user from repository
        authScope.launch {
            userRepository.userData.collect { userData ->
                _currentUser.value = userData
            }
        }
        
        authScope.launch {
            userRepository.isLoggedIn.collect { loggedIn ->
                _isLoggedIn.value = loggedIn
            }
        }
        
        // Update TokenCache when access token changes
        authScope.launch {
            userRepository.accessToken.collect { token ->
                TokenCache.accessToken = token
            }
        }
    }
    
    suspend fun login(loginResponse: LoginSuccessResponse) {
        println("DEBUG: AuthManager.login() called with user: ${loginResponse.data.user.username}")
        println("DEBUG: Access token: ${loginResponse.data.accessToken?.take(20)}...")
        println("DEBUG: Refresh token: ${loginResponse.data.refreshToken?.take(20)}...")
        
        userRepository.saveUserData(
            userData = loginResponse.data.user,
            accessToken = loginResponse.data.accessToken,
            refreshToken = loginResponse.data.refreshToken
        )
        
        println("DEBUG: User data saved to repository")
    }
    
    suspend fun logout() {
        userRepository.clearUserData()
    }
    
    suspend fun updateCurrentUser(updatedUser: UserData) {
        userRepository.updateUserData(updatedUser)
    }
    
    fun getCurrentUser(): UserData? = _currentUser.value
    
    fun isUserLoggedIn(): Boolean = _isLoggedIn.value
    
    suspend fun refreshTokenIfNeeded(): Boolean {
        return tokenManager.refreshTokenIfNeeded(userRepository)
    }
    
    suspend fun isTokenExpired(): Boolean {
        val currentToken = userRepository.accessToken.first()
        return tokenManager.isTokenExpired(currentToken)
    }
    
    // Email Verification Methods (New 6-digit code system)
    suspend fun verifyEmail(code: String): Result<AuthResponse> {
        println("DEBUG: AuthManager.verifyEmail called with code: $code")
        return try {
            val request = VerifyEmailDto(code)
            println("DEBUG: Making API call to verify email")
            val response = apiService.verifyEmail(request)
            
            if (response.isSuccessful && response.body() != null) {
                println("DEBUG: API response successful")
                val verifyResponse = response.body()!!
                println("DEBUG: Response success: ${verifyResponse.success}")
                if (verifyResponse.success && verifyResponse.user != null) {
                    println("DEBUG: Email verification successful in AuthManager")
                    // Update user data with verified status
                    updateCurrentUser(verifyResponse.user!!)
                    
                    // Check if user has tokens from registration
                    val currentUser = getCurrentUser()
                    println("DEBUG: User after email verification: ${currentUser?.email}")
                    println("DEBUG: User is logged in: ${isUserLoggedIn()}")
                    
                    // Create AuthResponse - tokens should already be available from registration
                    val authResponse = AuthResponse(
                        user = verifyResponse.user!!,
                        accessToken = "",
                        refreshToken = ""
                    )
                    println("DEBUG: AuthManager returning success result")
                    Result.success(authResponse)
                } else {
                    val errorMessage = verifyResponse.message ?: "Email verification failed"
                    println("DEBUG: Email verification failed - success: ${verifyResponse.success}, user: ${verifyResponse.user}")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Email verification failed"
                println("DEBUG: API response not successful - code: ${response.code()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            println("DEBUG: Exception in AuthManager.verifyEmail: ${e.message}")
            Result.failure(e)
        }
    }
    
    
    suspend fun resendVerificationCode(email: String? = null): Result<Unit> {
        return try {
            val userEmail = email ?: getCurrentUser()?.email ?: return Result.failure(Exception("No user email found"))
            
            val request = mapOf("email" to userEmail)
            val response = apiService.resendVerification(request)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    Result.success(Unit)
                } else {
                    val errorMessage = apiResponse.message ?: "Failed to resend verification code"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to resend verification code"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Password Reset Methods
    suspend fun requestPasswordReset(): Result<Unit> {
        return try {
            val currentUser = getCurrentUser()
            val email = currentUser?.email ?: return Result.failure(Exception("No user email found"))
            
            val request = RequestPasswordResetDto(email)
            val response = apiService.requestPasswordReset(request)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    Result.success(Unit)
                } else {
                    val errorMessage = apiResponse.message ?: "Failed to request password reset"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to request password reset"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(newPassword: String): Result<AuthResponse> {
        return try {
            val request = ResetPasswordDto(code = "123456", newPassword = newPassword)
            val response = apiService.resetPassword(request)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    val authResponse = apiResponse.data!!
                    // Update user data and tokens
                    updateCurrentUser(authResponse.user)
                    saveTokens(authResponse.accessToken, authResponse.refreshToken)
                    Result.success(authResponse)
                } else {
                    val errorMessage = apiResponse.message ?: "Password reset failed"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Password reset failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        val currentUser = getCurrentUser() ?: return
        userRepository.saveUserData(
            userData = currentUser,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
    
    suspend fun authenticateWithGoogle(idToken: String, userType: String): Result<GoogleAuthResponse> {
        return try {
            val request = GoogleAuthRequest(idToken = idToken, userType = userType)
            val response = apiService.authenticateWithGoogle(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                if (authResponse.success) {
                    // Store user data and tokens if they exist
                    if (authResponse.data != null) {
                        userRepository.saveUserData(
                            userData = authResponse.data.user,
                            accessToken = authResponse.data.accessToken,
                            refreshToken = authResponse.data.refreshToken
                        )
                    }
                    Result.success(authResponse)
                } else {
                    // Backend returned success: false
                    val errorMessage = authResponse.message ?: "Google authentication failed"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // HTTP error or no response body
                val errorMessage = response.body()?.message ?: "Google authentication failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null
        
        fun getInstance(context: Context, apiService: ApiService): AuthManager {
            return INSTANCE ?: synchronized(this) {
                val userRepository = UserRepository(context)
                val instance = AuthManager(userRepository, apiService)
                INSTANCE = instance
                instance
            }
        }
    }
}

// ViewModel for AuthManager
class AuthViewModel(private val authManager: AuthManager) : ViewModel() {
    
    val currentUser: StateFlow<UserData?> = authManager.currentUser
    val isLoggedIn: StateFlow<Boolean> = authManager.isLoggedIn
    
    fun getCurrentUser(): UserData? = authManager.getCurrentUser()
    
    fun isUserLoggedIn(): Boolean = authManager.isUserLoggedIn()
    
    fun logout() {
        viewModelScope.launch {
            authManager.logout()
        }
    }
}

class AuthViewModelFactory(private val context: Context, private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(AuthManager.getInstance(context, apiService)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
