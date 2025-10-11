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
        println("DEBUG: AuthManager.login() called with user: ${loginResponse.user.username}")
        println("DEBUG: Access token: ${loginResponse.accessToken?.take(20)}...")
        println("DEBUG: Refresh token: ${loginResponse.refreshToken?.take(20)}...")
        
        userRepository.saveUserData(
            userData = loginResponse.user,
            accessToken = loginResponse.accessToken,
            refreshToken = loginResponse.refreshToken
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
    
    // Email Verification Methods
    suspend fun verifyEmail(token: String): Result<EmailVerificationResponse> {
        return try {
            val request = EmailVerificationRequest(token)
            val response = apiService.verifyEmail(request)
            
            if (response.isSuccessful && response.body() != null) {
                val verificationResponse = response.body()!!
                if (verificationResponse.success) {
                    // Update user data with verified status if user data is provided
                    verificationResponse.user?.let { user ->
                        updateCurrentUser(user)
                    }
                    Result.success(verificationResponse)
                } else {
                    // Backend returned success: false
                    val errorMessage = verificationResponse.message ?: "Email verification failed"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // HTTP error or no response body
                val errorMessage = response.body()?.message ?: "Email verification failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getVerificationStatus(userId: String): Result<VerificationStatusResponse> {
        return try {
            val response = apiService.getVerificationStatus(userId)
            
            if (response.isSuccessful && response.body() != null) {
                val statusResponse = response.body()!!
                if (statusResponse.success) {
                    Result.success(statusResponse)
                } else {
                    // Backend returned success: false
                    val errorMessage = statusResponse.message ?: "Failed to get verification status"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // HTTP error or no response body
                val errorMessage = response.body()?.message ?: "Failed to get verification status"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resendVerificationEmail(email: String): Result<ResendVerificationResponse> {
        return try {
            val request = ResendVerificationRequest(email)
            val response = apiService.resendVerificationEmail(request)
            
            if (response.isSuccessful && response.body() != null) {
                val resendResponse = response.body()!!
                if (resendResponse.success) {
                    Result.success(resendResponse)
                } else {
                    // Backend returned success: false
                    val errorMessage = resendResponse.message ?: "Failed to resend verification email"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // HTTP error or no response body
                val errorMessage = response.body()?.message ?: "Failed to resend verification email"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Google OAuth Methods
    suspend fun authenticateWithGoogle(idToken: String, userType: String): Result<GoogleAuthResponse> {
        return try {
            val request = GoogleAuthRequest(idToken = idToken, userType = userType)
            val response = apiService.authenticateWithGoogle(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                if (authResponse.success) {
                    // Store user data and tokens if they exist
                    if (authResponse.user != null && 
                        authResponse.accessToken != null && authResponse.refreshToken != null) {
                        userRepository.saveUserData(
                            userData = authResponse.user,
                            accessToken = authResponse.accessToken,
                            refreshToken = authResponse.refreshToken
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
