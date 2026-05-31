package com.example.booknest.viewmodel.friends

import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.usecase.friends.AcceptFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.CancelFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.DeclineFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.GetFriendsActivityUseCase
import com.example.booknest.domain.usecase.friends.GetFriendsUseCase
import com.example.booknest.domain.usecase.friends.GetFriendshipStatusUseCase
import com.example.booknest.domain.usecase.friends.GetReceivedFriendRequestsUseCase
import com.example.booknest.domain.usecase.friends.GetSentFriendRequestsUseCase
import com.example.booknest.domain.usecase.friends.SearchUsersUseCase
import com.example.booknest.domain.usecase.friends.SendFriendRequestUseCase
import com.example.booknest.domain.usecase.friends.UnfriendUserUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getFriendsUseCase = mockk<GetFriendsUseCase>()
    private val getSentFriendRequestsUseCase = mockk<GetSentFriendRequestsUseCase>()
    private val getReceivedFriendRequestsUseCase = mockk<GetReceivedFriendRequestsUseCase>()
    private val getFriendsActivityUseCase = mockk<GetFriendsActivityUseCase>()
    private val searchUsersUseCase = mockk<SearchUsersUseCase>()
    private val sendFriendRequestUseCase = mockk<SendFriendRequestUseCase>()
    private val acceptFriendRequestUseCase = mockk<AcceptFriendRequestUseCase>()
    private val declineFriendRequestUseCase = mockk<DeclineFriendRequestUseCase>()
    private val cancelFriendRequestUseCase = mockk<CancelFriendRequestUseCase>()
    private val unfriendUserUseCase = mockk<UnfriendUserUseCase>()
    private val getFriendshipStatusUseCase = mockk<GetFriendshipStatusUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = FriendViewModel(
        feedback = feedback,
        getFriendsUseCase = getFriendsUseCase,
        getSentFriendRequestsUseCase = getSentFriendRequestsUseCase,
        getReceivedFriendRequestsUseCase = getReceivedFriendRequestsUseCase,
        getFriendsActivityUseCase = getFriendsActivityUseCase,
        searchUsersUseCase = searchUsersUseCase,
        sendFriendRequestUseCase = sendFriendRequestUseCase,
        acceptFriendRequestUseCase = acceptFriendRequestUseCase,
        declineFriendRequestUseCase = declineFriendRequestUseCase,
        cancelFriendRequestUseCase = cancelFriendRequestUseCase,
        unfriendUserUseCase = unfriendUserUseCase,
        getFriendshipStatusUseCase = getFriendshipStatusUseCase,
        sessionManager = mockLoggedInSessionManager(),
    )

    @Test
    fun loadFriends_populatesList() = runTest(testDispatcher) {
        val friends = listOf(TestFixtures.user(id = "f-1"), TestFixtures.user(id = "f-2"))
        coEvery { getFriendsUseCase() } returns Result.success(friends)

        val viewModel = createViewModel()
        viewModel.loadFriends()
        advanceUntilIdle()

        assertEquals(friends, viewModel.friends.value)
    }

    @Test
    fun searchUsers_blankQueryClearsResults() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.searchUsers("alice")
        advanceUntilIdle()
        viewModel.searchUsers("   ")
        advanceUntilIdle()

        assertTrue(viewModel.searchResults.value.isEmpty())
        coVerify(exactly = 1) { searchUsersUseCase("alice") }
    }

    @Test
    fun sendFriendRequest_reloadsSentRequests() = runTest(testDispatcher) {
        coEvery { sendFriendRequestUseCase("newfriend") } returns Result.success(
            FriendRequestResponse(
                id = "req-1",
                requesterId = "user-1",
                addresseeId = "user-2",
                status = "pending",
                createdAt = "2024-01-01T00:00:00.000Z",
                updatedAt = "2024-01-01T00:00:00.000Z",
            ),
        )
        coEvery { getSentFriendRequestsUseCase() } returns Result.success(
            listOf(TestFixtures.user(username = "newfriend")),
        )

        val viewModel = createViewModel()
        viewModel.sendFriendRequest("newfriend")
        advanceUntilIdle()

        coVerify { getSentFriendRequestsUseCase() }
    }

    @Test
    fun acceptFriendRequest_reloadsFriendsAndReceived() = runTest(testDispatcher) {
        coEvery { acceptFriendRequestUseCase("requester-1") } returns Result.success(
            FriendRequestResponse(
                id = "req-1",
                requesterId = "requester-1",
                addresseeId = "user-1",
                status = "accepted",
                createdAt = "2024-01-01T00:00:00.000Z",
                updatedAt = "2024-01-02T00:00:00.000Z",
            ),
        )
        coEvery { getFriendsUseCase() } returns Result.success(emptyList())
        coEvery { getReceivedFriendRequestsUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.acceptFriendRequest("requester-1")
        advanceUntilIdle()

        coVerify { getFriendsUseCase() }
        coVerify { getReceivedFriendRequestsUseCase() }
    }

    @Test
    fun unfriendUser_reloadsFriends() = runTest(testDispatcher) {
        coEvery { unfriendUserUseCase("friend-1") } returns Result.success(Unit)
        coEvery { getFriendsUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.unfriendUser("friend-1")
        advanceUntilIdle()

        coVerify(atLeast = 1) { getFriendsUseCase() }
    }
}
