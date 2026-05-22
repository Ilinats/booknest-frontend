package com.example.booknest.viewmodel.genres

import com.example.booknest.domain.model.response.GenrePreferenceResponse
import com.example.booknest.domain.usecase.genres.DeleteUserGenrePreferenceUseCase
import com.example.booknest.domain.usecase.genres.GetGenrePreferencesUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.domain.usecase.genres.SaveUserGenrePreferenceUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.coVerify
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
class FavoriteGenresViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getGenresUseCase = mockk<GetGenresUseCase>()
    private val getGenrePreferencesUseCase = mockk<GetGenrePreferencesUseCase>()
    private val saveUserGenrePreferenceUseCase = mockk<SaveUserGenrePreferenceUseCase>(relaxed = true)
    private val deleteUserGenrePreferenceUseCase = mockk<DeleteUserGenrePreferenceUseCase>(relaxed = true)
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = FavoriteGenresViewModel(
        feedback = feedback,
        getGenresUseCase = getGenresUseCase,
        getGenrePreferencesUseCase = getGenrePreferencesUseCase,
        saveUserGenrePreferenceUseCase = saveUserGenrePreferenceUseCase,
        deleteUserGenrePreferenceUseCase = deleteUserGenrePreferenceUseCase,
    )

    @Test
    fun loadGenres_populatesList() = runTest(testDispatcher) {
        val genres = listOf(TestFixtures.genre(1, "Fantasy"), TestFixtures.genre(2, "Sci-Fi"))
        coEvery { getGenresUseCase() } returns Result.success(genres)
        coEvery { getGenrePreferencesUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.loadGenres()
        advanceUntilIdle()

        assertEquals(genres, viewModel.genres.value)
    }

    @Test
    fun toggleGenre_updatesSelectedIds() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.toggleGenre(1)
        viewModel.toggleGenre(2)
        viewModel.toggleGenre(1)

        assertEquals(setOf(2), viewModel.selectedGenreIds.value)
    }

    @Test
    fun savePreferences_requiresAtLeastOneGenre() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.savePreferences()
        advanceUntilIdle()

        assertEquals("Select at least one genre.", viewModel.message.value)
    }

    @Test
    fun savePreferences_deletesRemovedGenres() = runTest(testDispatcher) {
        coEvery { getGenresUseCase() } returns Result.success(listOf(TestFixtures.genre(1, "Fantasy")))
        coEvery { getGenrePreferencesUseCase() } returns Result.success(
            listOf(
                GenrePreferenceResponse(id = "p1", genreId = 1),
                GenrePreferenceResponse(id = "p2", genreId = 2),
            ),
        )
        coEvery { deleteUserGenrePreferenceUseCase(any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.loadGenres()
        advanceUntilIdle()

        viewModel.toggleGenre(2)
        viewModel.savePreferences()
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteUserGenrePreferenceUseCase(match { it.genreId == 2 }) }
        coVerify(exactly = 0) { saveUserGenrePreferenceUseCase(any()) }
        assertEquals("Favorite genres saved.", viewModel.message.value)
    }
}
