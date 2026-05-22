package com.example.booknest.viewmodel.author

import com.example.booknest.domain.usecase.author.CheckIfFollowingAuthorUseCase
import com.example.booknest.domain.usecase.author.FollowAuthorUseCase
import com.example.booknest.domain.usecase.author.GetAuthorFollowersUseCase
import com.example.booknest.domain.usecase.author.GetBooksFromFollowedAuthorsUseCase
import com.example.booknest.domain.usecase.author.GetFollowedAuthorsUseCase
import com.example.booknest.domain.usecase.author.UnfollowAuthorUseCase
import com.example.booknest.port.ToastNotifier
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthorFollowViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getFollowedAuthorsUseCase = mockk<GetFollowedAuthorsUseCase>()
    private val getAuthorFollowersUseCase = mockk<GetAuthorFollowersUseCase>()
    private val getBooksFromFollowedAuthorsUseCase = mockk<GetBooksFromFollowedAuthorsUseCase>()
    private val followAuthorUseCase = mockk<FollowAuthorUseCase>()
    private val unfollowAuthorUseCase = mockk<UnfollowAuthorUseCase>()
    private val checkIfFollowingAuthorUseCase = mockk<CheckIfFollowingAuthorUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = AuthorFollowViewModel(
        feedback = feedback,
        getFollowedAuthorsUseCase = getFollowedAuthorsUseCase,
        getAuthorFollowersUseCase = getAuthorFollowersUseCase,
        getBooksFromFollowedAuthorsUseCase = getBooksFromFollowedAuthorsUseCase,
        followAuthorUseCase = followAuthorUseCase,
        unfollowAuthorUseCase = unfollowAuthorUseCase,
        checkIfFollowingAuthorUseCase = checkIfFollowingAuthorUseCase,
        sessionManager = mockLoggedInSessionManager(user = TestFixtures.user(id = "user-1")),
    )

    @Test
    fun loadFollowedAuthors_populatesList() = runTest(testDispatcher) {
        val authors = listOf(TestFixtures.authorFollow(authorId = "a-1"))
        coEvery { getFollowedAuthorsUseCase() } returns Result.success(authors)

        val viewModel = createViewModel()
        viewModel.loadFollowedAuthors()
        advanceUntilIdle()

        assertEquals(authors, viewModel.followedAuthors.value)
    }

    @Test
    fun followAuthor_optimisticallyAddsAuthor() = runTest(testDispatcher) {
        coEvery { followAuthorUseCase("author-1") } returns Result.success(TestFixtures.authorFollow())
        coEvery { getFollowedAuthorsUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.followAuthor("author-1")
        advanceUntilIdle()

        assertTrue(viewModel.followingStatus.value["author-1"] == true)
        coVerify { followAuthorUseCase("author-1") }
    }

    @Test
    fun followAuthor_failureRollsBackOptimisticUpdate() = runTest(testDispatcher) {
        coEvery { followAuthorUseCase("author-1") } returns Result.failure(
            IllegalStateException("Cannot follow"),
        )

        val viewModel = createViewModel()
        viewModel.followAuthor("author-1")
        advanceUntilIdle()

        assertFalse(viewModel.followingStatus.value["author-1"] == true)
        assertEquals("Cannot follow", viewModel.error.value)
    }

    @Test
    fun checkIfFollowingAuthor_updatesStatusMap() = runTest(testDispatcher) {
        coEvery { checkIfFollowingAuthorUseCase("author-1") } returns Result.success(
            mapOf("isFollowing" to true),
        )

        val viewModel = createViewModel()
        viewModel.checkIfFollowingAuthor("author-1")
        advanceUntilIdle()

        assertTrue(viewModel.followingStatus.value["author-1"] == true)
    }
}
