package com.example.booknest.viewmodel.profile

import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.usecase.profile.GetMyProfileUseCase
import com.example.booknest.domain.usecase.profile.GetPublicUserProfileUseCase
import com.example.booknest.domain.usecase.profile.GetUserProfileUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.presentation.common.UiState
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.testutil.mockLoggedInSessionManager
import com.example.booknest.testutil.mockLoggedOutSessionManager
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getMyProfileUseCase = mockk<GetMyProfileUseCase>()
    private val getUserProfileUseCase = mockk<GetUserProfileUseCase>()
    private val getPublicUserProfileUseCase = mockk<GetPublicUserProfileUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel(
        refreshBus: ProfileRefreshBus = ProfileRefreshBus(),
    ): ProfileViewModel = ProfileViewModel(
        feedback = feedback,
        sessionManager = mockLoggedInSessionManager(),
        getMyProfileUseCase = getMyProfileUseCase,
        getUserProfileUseCase = getUserProfileUseCase,
        getPublicUserProfileUseCase = getPublicUserProfileUseCase,
        profileRefreshBus = refreshBus,
    )

    private fun ProfileViewModel.close() {
        viewModelScope.cancel()
    }

    @Test
    fun loadMyProfile_successUpdatesState() = runTest(testDispatcher) {
        val profile = TestFixtures.userProfile()
        coEvery { getMyProfileUseCase() } returns Result.success(profile)

        val viewModel = createViewModel()
        try {
            viewModel.loadMyProfile()
            advanceUntilIdle()

            assertEquals(profile, viewModel.myProfile.value)
            assertEquals(UiState.Success(profile), viewModel.profileState.value)
        } finally {
            viewModel.close()
        }
    }

    @Test
    fun loadMyProfile_withoutToken_skipsUseCase() = runTest(testDispatcher) {
        val viewModel = ProfileViewModel(
            feedback = feedback,
            sessionManager = mockLoggedOutSessionManager(),
            getMyProfileUseCase = getMyProfileUseCase,
            getUserProfileUseCase = getUserProfileUseCase,
            getPublicUserProfileUseCase = getPublicUserProfileUseCase,
            profileRefreshBus = ProfileRefreshBus(),
        )
        try {
            viewModel.loadMyProfile()
            advanceUntilIdle()

            coVerify(exactly = 0) { getMyProfileUseCase() }
            assertEquals(UiState.Idle, viewModel.profileState.value)
        } finally {
            viewModel.close()
        }
    }

    @Test
    fun refreshBus_triggersProfileReload() = runTest(testDispatcher) {
        val profile = TestFixtures.userProfile()
        coEvery { getMyProfileUseCase() } returns Result.success(profile)
        val refreshBus = ProfileRefreshBus()

        val viewModel = createViewModel(refreshBus)
        try {
            advanceUntilIdle()
            viewModel.loadMyProfile()
            advanceUntilIdle()
            refreshBus.requestRefresh()
            advanceUntilIdle()

            coVerify(exactly = 2) { getMyProfileUseCase() }
            assertEquals(profile, viewModel.myProfile.value)
        } finally {
            viewModel.close()
        }
    }

    @Test
    fun loadUserProfile_mapsPublicProfile() = runTest(testDispatcher) {
        val publicProfile = TestFixtures.publicUserProfile()
        coEvery { getUserProfileUseCase("reader1") } returns Result.success(publicProfile)

        val viewModel = createViewModel()
        try {
            viewModel.loadUserProfile("reader1")
            advanceUntilIdle()

            assertEquals(publicProfile, viewModel.publicProfile.value)
            assertEquals(publicProfile.toFullProfile(), viewModel.profileState.value.getOrNull())
        } finally {
            viewModel.close()
        }
    }

    @Test
    fun loadPublicUserProfile_failureClearsProfile() = runTest(testDispatcher) {
        coEvery { getPublicUserProfileUseCase("reader1") } returns Result.failure(Exception("Not found"))

        val viewModel = createViewModel()
        try {
            viewModel.loadPublicUserProfile("reader1")
            advanceUntilIdle()

            assertNull(viewModel.publicProfile.value)
            assertEquals("Not found", viewModel.error.value)
        } finally {
            viewModel.close()
        }
    }
}

private fun <T> UiState<T>.getOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}
