package com.example.booknest.port

import com.example.booknest.domain.model.response.UserResponse
import kotlinx.coroutines.flow.StateFlow

/**
 * Read-only session surface for UI and in-app messaging (toasts, gates).
 * Mutable operations live on [SessionWriter].
 */
interface SessionReader : AuthTokenAccessor {
    val currentUser: StateFlow<UserResponse?>
    val isLoggedIn: StateFlow<Boolean?>
    fun getUserId(): String
}
