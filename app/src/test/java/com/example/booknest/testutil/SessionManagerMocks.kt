package com.example.booknest.testutil

import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.UserResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

fun mockLoggedInSessionManager(
    token: String = "test-token",
    user: UserResponse? = TestFixtures.user(),
): SessionManager {
    val sessionManager = mockk<SessionManager>(relaxed = true)
    every { sessionManager.isLoggedIn } returns MutableStateFlow(true)
    every { sessionManager.getToken() } returns token
    every { sessionManager.currentUser } returns MutableStateFlow(user)
    return sessionManager
}

fun mockLoggedOutSessionManager(): SessionManager {
    val sessionManager = mockk<SessionManager>(relaxed = true)
    every { sessionManager.isLoggedIn } returns MutableStateFlow(false)
    every { sessionManager.getToken() } returns ""
    every { sessionManager.currentUser } returns MutableStateFlow(null)
    return sessionManager
}
