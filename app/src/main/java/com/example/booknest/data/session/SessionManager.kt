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

/**
 * Holds auth session state and persists credentials via [dataStore].
 * Single instance is provided by Koin; do not construct manually.
 */
class SessionManager(
    private val dataStore: DataStore<AppSettings>
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
        currentToken = fetchAuthToken()
        currentRefreshToken = fetchAuthRefreshToken()
        currentUserId = fetchUserId()
        _isLoggedIn.value = currentToken.isNotEmpty()
    }

    override suspend fun logout(authRepository: AuthRepository?) {
        android.util.Log.d("SessionManager", "logout() called")

        val refreshToken = currentRefreshToken

        setAuthEntities("", "", "", "", "", "")
        _isLoggedIn.emit(false)
        _currentUser.emit(null)

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

        setAuthEntities("", "", "", "", "", "")
        _isLoggedIn.emit(false)
        _currentUser.emit(null)

        android.util.Log.d("SessionManager", "logoutAll() completed, isLoggedIn=${_isLoggedIn.value}")
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
        userType: String
    ) {
        dataStore.updateData {
            it.copy(
                token = token,
                refreshToken = refreshToken,
                userId = userId,
                username = username,
                email = email,
                userType = userType
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
        dataStore.updateData {
            it.copy(
                token = accessToken,
                refreshToken = refreshToken
            )
        }
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
                userType = user.userType ?: ""
            )
        }
    }

    suspend fun fetchAuthToken(): String {
        return appSettings.first().token
    }

    suspend fun fetchAuthRefreshToken(): String {
        return appSettings.first().refreshToken
    }

    suspend fun fetchUserId(): String {
        return appSettings.first().userId
    }

    suspend fun fetchUserType(): String {
        return appSettings.first().userType
    }

    /** Refreshes in-memory tokens from disk (e.g. after process start). */
    override suspend fun setTokens() {
        currentToken = fetchAuthToken()
        currentRefreshToken = fetchAuthRefreshToken()
        currentUserId = fetchUserId()
    }

    override fun getToken(): String {
        return currentToken
    }

    override fun getRefreshToken(): String {
        return currentRefreshToken
    }

    override fun getUserId(): String {
        return currentUserId
    }
}
