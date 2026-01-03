package com.example.booknest.data.session

import androidx.datastore.core.DataStore
import com.example.booknest.domain.model.response.UserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object SessionManager {
    private var currentDataStore: DataStore<AppSettings>? = null
    private var appSettings: Flow<AppSettings>? = null

    private var currentToken: String = ""
    private var currentRefreshToken: String = ""
    private var currentUserId: String = ""

    private var sessionManager: SessionManager? = null

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserResponse?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun getInstance(dataStore: DataStore<AppSettings>): SessionManager {
        if (sessionManager == null) {
            currentDataStore = dataStore
            appSettings = dataStore.data
            sessionManager = SessionManager

            CoroutineScope(Dispatchers.Main).launch {
                val token = fetchAuthToken()
                currentToken = token
                currentRefreshToken = fetchAuthRefreshToken()
                currentUserId = fetchUserId()
                _isLoggedIn.value = token.isNotEmpty()
            }
        }
        return sessionManager as SessionManager
    }

    suspend fun logout() {
        android.util.Log.d("SessionManager", "logout() called")
        setAuthEntities("", "", "", "", "", "")
        _isLoggedIn.emit(false)
        _currentUser.emit(null)
        android.util.Log.d("SessionManager", "logout() completed, isLoggedIn=${_isLoggedIn.value}")
    }

    suspend fun setLoggedIn() {
        _isLoggedIn.value = true
    }

    suspend fun setAuthEntities(
        token: String,
        refreshToken: String,
        userId: String = "",
        username: String = "",
        email: String = "",
        userType: String = ""
    ) {
        currentDataStore?.updateData {
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

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        currentDataStore?.updateData {
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

    suspend fun updateUser(user: UserResponse) {
        _currentUser.emit(user)
        currentDataStore?.updateData {
            it.copy(
                userId = user.id,
                username = user.username,
                email = user.email ?: "",
                userType = user.userType ?: ""
            )
        }
    }

    suspend fun fetchAuthToken(): String {
        return appSettings?.first()?.token ?: ""
    }

    suspend fun fetchAuthRefreshToken(): String {
        return appSettings?.first()?.refreshToken ?: ""
    }

    suspend fun fetchUserId(): String {
        return appSettings?.first()?.userId ?: ""
    }

    suspend fun fetchUserType(): String {
        return appSettings?.first()?.userType ?: ""
    }

    suspend fun setTokens() {
        currentToken = fetchAuthToken()
        currentRefreshToken = fetchAuthRefreshToken()
        currentUserId = fetchUserId()
    }

    fun getToken(): String {
        return currentToken
    }

    fun getRefreshToken(): String {
        return currentRefreshToken
    }

    fun getUserId(): String {
        return currentUserId
    }
}

