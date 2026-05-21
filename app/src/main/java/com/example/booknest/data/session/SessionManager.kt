package com.example.booknest.data.session

import androidx.datastore.core.DataStore
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.port.SessionReader
import com.example.booknest.port.SessionWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SessionManager(
    private val dataStore: DataStore<AppSettings>,
    private val secureTokenStore: SecureTokenStore,
) : SessionReader, SessionWriter {

    private val appSettings: Flow<AppSettings> = dataStore.data
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var currentToken: String = ""
    private var currentRefreshToken: String = ""
    private var currentUserId: String = ""

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    override val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserResponse?>(null)
    override val currentUser = _currentUser.asStateFlow()

    init {
        scope.launch {
            hydrateFromDisk()
        }
    }

    private suspend fun hydrateFromDisk() {
        currentToken = secureTokenStore.getAccessToken()
        currentRefreshToken = secureTokenStore.getRefreshToken()
        currentUserId = fetchUserId()
        _isLoggedIn.value = currentToken.isNotEmpty()
    }

    override suspend fun logout(authRepository: AuthRepository?) {
        android.util.Log.d("SessionManager", "logout() called")

        val refreshToken = currentRefreshToken

        clearLocalSession()

        if (authRepository != null && refreshToken.isNotEmpty()) {
            try {
                authRepository.logout(refreshToken)
                android.util.Log.d("SessionManager", "Backend logout successful")
            } catch (e: Exception) {
                android.util.Log.e("SessionManager", "Backend logout failed: ${e.message}", e)
            }
        }

        android.util.Log.d("SessionManager", "logout() completed, isLoggedIn=${_isLoggedIn.value}")
    }

    override suspend fun logoutAll(authRepository: AuthRepository?) {
        android.util.Log.d("SessionManager", "logoutAll() called")

        if (authRepository != null && currentToken.isNotEmpty()) {
            try {
                authRepository.logoutAll()
                android.util.Log.d("SessionManager", "Backend logout-all successful")
            } catch (e: Exception) {
                android.util.Log.e("SessionManager", "Backend logout-all failed: ${e.message}", e)
            }
        }

        clearLocalSession()

        android.util.Log.d("SessionManager", "logoutAll() completed, isLoggedIn=${_isLoggedIn.value}")
    }

    private suspend fun clearLocalSession() {
        secureTokenStore.clearTokens()
        dataStore.updateData {
            it.copy(
                userId = "",
                username = "",
                email = "",
                userType = "",
            )
        }
        currentToken = ""
        currentRefreshToken = ""
        currentUserId = ""
        _isLoggedIn.emit(false)
        _currentUser.emit(null)
    }

    override suspend fun setLoggedIn() {
        _isLoggedIn.value = true
    }

    override suspend fun setAuthEntities(
        token: String,
        refreshToken: String,
        userId: String,
        username: String,
        email: String,
        userType: String,
    ) {
        secureTokenStore.saveTokens(token, refreshToken)
        dataStore.updateData {
            it.copy(
                userId = userId,
                username = username,
                email = email,
                userType = userType,
            )
        }
        currentToken = token
        currentRefreshToken = refreshToken
        currentUserId = userId
        if (token.isNotEmpty()) {
            setLoggedIn()
        }
    }

    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        secureTokenStore.saveTokens(accessToken, refreshToken)
        currentToken = accessToken
        currentRefreshToken = refreshToken
        if (accessToken.isNotEmpty()) {
            _isLoggedIn.value = true
        }
    }

    override suspend fun updateUser(user: UserResponse) {
        _currentUser.emit(user)
        dataStore.updateData {
            it.copy(
                userId = user.id,
                username = user.username,
                email = user.email ?: "",
                userType = user.userType ?: "",
            )
        }
        currentUserId = user.id
    }

    suspend fun fetchAuthToken(): String = secureTokenStore.getAccessToken()

    suspend fun fetchAuthRefreshToken(): String = secureTokenStore.getRefreshToken()

    suspend fun fetchUserId(): String {
        return appSettings.first().userId
    }

    suspend fun fetchUserType(): String {
        return appSettings.first().userType
    }

    override suspend fun setTokens() {
        currentToken = secureTokenStore.getAccessToken()
        currentRefreshToken = secureTokenStore.getRefreshToken()
        currentUserId = fetchUserId()
    }

    override fun getToken(): String = currentToken

    override fun getRefreshToken(): String = currentRefreshToken

    override fun getUserId(): String = currentUserId
}
