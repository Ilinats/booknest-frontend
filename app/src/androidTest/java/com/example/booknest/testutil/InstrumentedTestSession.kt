package com.example.booknest.testutil

import com.example.booknest.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.core.context.GlobalContext

object InstrumentedTestSession {

    suspend fun ensureLoggedOut() {
        val sessionManager = GlobalContext.get().get<SessionManager>()
        sessionManager.logout(null)
        withTimeout(5_000) {
            sessionManager.isLoggedIn.first { it == false }
        }
    }
}

class LoggedOutSessionRule : TestWatcher() {
    override fun starting(description: Description) {
        runBlocking { InstrumentedTestSession.ensureLoggedOut() }
    }
}
