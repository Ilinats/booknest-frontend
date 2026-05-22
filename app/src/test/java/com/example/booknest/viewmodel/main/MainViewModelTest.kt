package com.example.booknest.viewmodel.main

import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.testutil.mockLoggedInSessionManager
import com.example.booknest.testutil.mockLoggedOutSessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getCurrentUserUseCase = mockk<GetCurrentUserUseCase>()

    @Test
    fun fetchCurrentUser_emptyToken_setsError() = runTest(testDispatcher) {
        val viewModel = MainViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            sessionManager = mockLoggedOutSessionManager(),
        )

        viewModel.fetchCurrentUser()
        advanceUntilIdle()

        assertEquals("No authentication token available", viewModel.userLoadError.value)
        coVerify(exactly = 0) { getCurrentUserUseCase() }
    }

    @Test
    fun fetchCurrentUser_updatesSessionOnSuccess() = runTest(testDispatcher) {
        val user = TestFixtures.user()
        val sessionManager = mockLoggedInSessionManager(user = null)
        every { sessionManager.currentUser } returns MutableStateFlow(null)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)

        val viewModel = MainViewModel(getCurrentUserUseCase, sessionManager)
        viewModel.fetchCurrentUser()
        advanceUntilIdle()

        coVerify { sessionManager.updateUser(user) }
        assertEquals(false, viewModel.isLoadingUser.value)
    }
}
