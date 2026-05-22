package com.example.booknest.viewmodel.auth

import com.example.booknest.domain.usecase.auth.RegisterUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.domain.usecase.genres.SaveUserGenrePreferenceUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.presentation.common.UiState
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.testutil.mockLoggedInSessionManager
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val registerUseCase = mockk<RegisterUseCase>()
    private val getGenresUseCase = mockk<GetGenresUseCase>()
    private val saveUserGenrePreferenceUseCase = mockk<SaveUserGenrePreferenceUseCase>(relaxed = true)
    private val sessionManager = mockLoggedInSessionManager()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel(): SignupViewModel {
        coEvery { getGenresUseCase() } returns Result.success(
            listOf(TestFixtures.genre(1, "Fantasy"), TestFixtures.genre(2, "Sci-Fi")),
        )
        return SignupViewModel(
            feedback = feedback,
            sessionManager = sessionManager,
            registerUseCase = registerUseCase,
            getGenresUseCase = getGenresUseCase,
            saveUserGenrePreferenceUseCase = saveUserGenrePreferenceUseCase,
        )
    }

    @Test
    fun updatePersonalInfo_updatesSignupData() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updatePersonalInfo("Jane", "Doe", "jane@example.com", "password123")

        val data = viewModel.signupData.value
        assertEquals("Jane", data.firstName)
        assertEquals("jane@example.com", data.email)
    }

    @Test
    fun updateProfileDetails_buildsAddressWhenComplete() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateProfileDetails(
            birthDate = "2000-01-01",
            streetAddress = "123 Main",
            city = "Sofia",
            postalCode = "1000",
            country = "",
            isPrimary = true,
        )

        val address = viewModel.signupData.value.address
        assertEquals("123 Main", address?.streetAddress)
        assertEquals("Bulgaria", address?.country)
    }

    @Test
    fun updateGenres_storesSelection() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateGenres(listOf("Fantasy"))
        assertEquals(listOf("Fantasy"), viewModel.signupData.value.genres)
    }
}
