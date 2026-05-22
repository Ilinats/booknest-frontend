package com.example.booknest.viewmodel.author

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthorBooksCatalogRefresherTest {

    @Test
    fun requestRefresh_notifiesCollectors() = runTest {
        val refresher = AuthorBooksCatalogRefresher()
        val emission = async { refresher.refreshRequests.first() }
        advanceUntilIdle()

        refresher.requestRefresh()
        advanceUntilIdle()

        assertEquals(Unit, emission.await())
    }
}
