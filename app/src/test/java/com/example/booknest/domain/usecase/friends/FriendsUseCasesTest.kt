package com.example.booknest.domain.usecase.friends

import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.repository.FriendsRepository
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FriendsUseCasesTest {

    private val repository = mockk<FriendsRepository>()

    @Test
    fun getFriendsUseCase_delegatesToRepository() = runTest {
        val friends = listOf(TestFixtures.user(id = "f-1"))
        coEvery { repository.getFriends() } returns Result.success(friends)

        assertEquals(friends, GetFriendsUseCase(repository)().getOrNull())
    }

    @Test
    fun sendFriendRequestUseCase_delegatesToRepository() = runTest {
        val response = FriendRequestResponse(
            id = "req-1",
            requesterId = "user-1",
            addresseeId = "user-2",
            status = "pending",
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = "2024-01-01T00:00:00.000Z",
        )
        coEvery { repository.sendFriendRequest("alice") } returns Result.success(response)

        val result = SendFriendRequestUseCase(repository)("alice")

        assertEquals(response, result.getOrNull())
        coVerify { repository.sendFriendRequest("alice") }
    }

    @Test
    fun searchUsersUseCase_delegatesToRepository() = runTest {
        val users = listOf(TestFixtures.user(username = "alice"))
        coEvery { repository.searchUsers("alice") } returns Result.success(users)

        assertEquals(users, SearchUsersUseCase(repository)("alice").getOrNull())
    }
}
