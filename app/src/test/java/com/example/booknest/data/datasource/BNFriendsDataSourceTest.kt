package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.FriendsService
import com.example.booknest.testutil.DataSourceJsonFixtures
import com.example.booknest.testutil.MockWebServerDataSourceTest
import com.example.booknest.testutil.RetrofitTestSupport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BNFriendsDataSourceTest : MockWebServerDataSourceTest() {

    private val dataSource: BNFriendsDataSource
        get() = BNFriendsDataSource(RetrofitTestSupport.service<FriendsService>(mockWebServer))

    @Test
    fun getFriends_successReturnsList() = runTest {
        enqueueJson(200, "[${DataSourceJsonFixtures.USER}]")

        val result = dataSource.getFriends()

        assertTrue(result.isSuccess)
        assertEquals("testuser", result.getOrNull()?.first()?.username)
        assertTrue(mockWebServer.takeRequest().path!!.startsWith("/api/friends"))
    }

    @Test
    fun getFriendshipStatus_successReturnsStatus() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.FRIENDSHIP_STATUS)

        val result = dataSource.getFriendshipStatus("user-2")

        assertTrue(result.isSuccess)
        assertEquals("accepted", result.getOrNull()?.status)
        assertEquals("/api/friends/status/user-2", mockWebServer.takeRequest().path)
    }

    @Test
    fun sendFriendRequest_successReturnsResponse() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.friendRequest)

        val result = dataSource.sendFriendRequest("reader2")

        assertTrue(result.isSuccess)
        assertEquals("pending", result.getOrNull()?.status)
        assertEquals("/api/friends/request/reader2", mockWebServer.takeRequest().path)
    }

    @Test
    fun searchUsers_successMapsUsersFromSearchItems() = runTest {
        val body = """
            [{"user":${DataSourceJsonFixtures.USER},"friendshipStatus":null,"isRequester":false}]
        """.trimIndent()
        enqueueJson(200, body)

        val result = dataSource.searchUsers("test", limit = 10)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("testuser", result.getOrNull()?.first()?.username)
    }

    @Test
    fun declineFriendRequest_failureMapsBnError() = runTest {
        enqueueJson(404, DataSourceJsonFixtures.errorBody("Request not found", 404))

        val result = dataSource.declineFriendRequest("user-9")

        assertTrue(result.isFailure)
        assertEquals(
            "Request not found",
            (result.exceptionOrNull() as BNError.Generic).messageString,
        )
    }
}
