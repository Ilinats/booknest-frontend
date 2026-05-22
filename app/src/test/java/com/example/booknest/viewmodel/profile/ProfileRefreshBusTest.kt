package com.example.booknest.viewmodel.profile

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileRefreshBusTest {

    @Test
    fun requestRefresh_notifiesActiveCollectors() = runTest {
        val bus = ProfileRefreshBus()
        val first = async { bus.refreshRequests.first() }
        advanceUntilIdle()

        bus.requestRefresh()
        advanceUntilIdle()

        assertEquals(Unit, first.await())
    }
}
