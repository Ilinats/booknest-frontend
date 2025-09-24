package com.example.booknest.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.network.LoginSuccessResponse
import com.example.booknest.network.TokenCache
import com.example.booknest.network.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthManager(private val userRepository: UserRepository) {
    
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
        userRepository.saveUserData(
            userData = loginResponse.user,
            accessToken = loginResponse.accessToken,
            refreshToken = loginResponse.refreshToken
        )
    }
    
    suspend fun logout() {
        userRepository.clearUserData()
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
    
    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null
        
        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                val userRepository = UserRepository(context)
                val instance = AuthManager(userRepository)
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

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(AuthManager.getInstance(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
