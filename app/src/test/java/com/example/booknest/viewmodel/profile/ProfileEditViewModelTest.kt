package com.example.booknest.viewmodel.profile

import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.domain.usecase.files.UploadProfileImageUseCase
import com.example.booknest.domain.usecase.profile.DeleteAccountUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.domain.usecase.profile.RemoveAvatarUseCase
import com.example.booknest.domain.usecase.profile.UpdateMyProfileUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.presentation.common.UiState
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.testutil.mockLoggedInSessionManager
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val updateMyProfileUseCase = mockk<UpdateMyProfileUseCase>()
    private val removeAvatarUseCase = mockk<RemoveAvatarUseCase>()
    private val deleteAccountUseCase = mockk<DeleteAccountUseCase>()
    private val uploadProfileImageUseCase = mockk<UploadProfileImageUseCase>()
    private val getCurrentUserUseCase = mockk<GetCurrentUserUseCase>()
    private val authRepository = mockk<AuthRepository>()
    private val profileRefreshBus = ProfileRefreshBus()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = ProfileEditViewModel(
        feedback = feedback,
        sessionManager = mockLoggedInSessionManager(),
        updateMyProfileUseCase = updateMyProfileUseCase,
        removeAvatarUseCase = removeAvatarUseCase,
        deleteAccountUseCase = deleteAccountUseCase,
        uploadProfileImageUseCase = uploadProfileImageUseCase,
        getCurrentUserUseCase = getCurrentUserUseCase,
        authRepository = authRepository,
        profileRefreshBus = profileRefreshBus,
    )

    @Test
    fun updateProfile_successUpdatesState() = runTest(testDispatcher) {
        coEvery { updateMyProfileUseCase(any()) } returns Result.success(TestFixtures.userProfile())

        val viewModel = createViewModel()
        viewModel.updateProfile(username = "reader1", bio = "Hello")
        advanceUntilIdle()

        assertEquals(UiState.Success(Unit), viewModel.profileEditState.value)
        assertEquals("Profile updated successfully", viewModel.successMessage.value)
        coVerify { updateMyProfileUseCase(any()) }
    }

    @Test
    fun updateProfile_failureSetsError() = runTest(testDispatcher) {
        coEvery { updateMyProfileUseCase(any()) } returns Result.failure(Exception("Bad request"))

        val viewModel = createViewModel()
        viewModel.updateProfile(username = "reader1")
        advanceUntilIdle()

        assertEquals("Bad request", viewModel.error.value)
    }

    @Test
    fun clearError_resetsErrorState() {
        val viewModel = createViewModel()
        viewModel.clearError()
        assertNull(viewModel.error.value)
    }
}
