package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.ProfilesService
import com.example.booknest.testutil.DataSourceJsonFixtures
import com.example.booknest.testutil.MockWebServerDataSourceTest
import com.example.booknest.testutil.RetrofitTestSupport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BNProfilesDataSourceTest : MockWebServerDataSourceTest() {

    private val dataSource: BNProfilesDataSource
        get() = BNProfilesDataSource(RetrofitTestSupport.service<ProfilesService>(mockWebServer))

    @Test
    fun getMe_successReturnsUser() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.USER)

        val result = dataSource.getMe()

        assertTrue(result.isSuccess)
        assertEquals("testuser", result.getOrNull()?.username)
        assertEquals("/api/users/me", mockWebServer.takeRequest().path)
    }

    @Test
    fun getMyStats_successReturnsStats() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.userStats)

        val result = dataSource.getMyStats()

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()?.stats?.totalApplications)
        assertEquals("/api/users/me/stats", mockWebServer.takeRequest().path)
    }

    @Test
    fun getMyProfile_combinesStatsAndProfile() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.userStats)
        enqueueJson(200, DataSourceJsonFixtures.userProfile)

        val result = dataSource.getMyProfile()

        assertTrue(result.isSuccess)
        assertEquals("testuser", result.getOrNull()?.username)
        assertEquals(5, result.getOrNull()?.stats?.totalApplications)
        assertEquals("profile-1", result.getOrNull()?.id)
    }

    @Test
    fun getMe_failureMapsBnError() = runTest {
        enqueueJson(401, DataSourceJsonFixtures.errorBody("Unauthorized", 401))

        val result = dataSource.getMe()

        assertTrue(result.isFailure)
        assertEquals("Unauthorized", (result.exceptionOrNull() as BNError.Generic).messageString)
    }
}
