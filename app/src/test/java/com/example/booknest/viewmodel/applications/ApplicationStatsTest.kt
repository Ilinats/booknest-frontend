package com.example.booknest.viewmodel.applications

import com.example.booknest.testutil.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplicationStatsTest {

    @Test
    fun approvalRate_calculatesFromStatuses() {
        val apps = listOf(
            TestFixtures.application(id = "1", status = "approved"),
            TestFixtures.application(id = "2", status = "pending"),
            TestFixtures.application(id = "3", status = "rejected"),
        )

        val total = apps.size
        val approved = apps.count { it.status == "approved" }
        val approvalRate = if (total > 0) approved.toDouble() / total * 100 else 0.0

        assertEquals(3, total)
        assertEquals(33.333333333333336, approvalRate, 0.001)
    }

    @Test
    fun tabCounts_matchViewModelTabDefinitions() {
        val apps = listOf(
            TestFixtures.application(id = "1", status = "pending"),
            TestFixtures.application(id = "2", status = "approved", readingStatus = "not_started"),
            TestFixtures.application(id = "3", status = "approved", readingStatus = "reviewed"),
            TestFixtures.application(id = "4", status = "rejected"),
            TestFixtures.application(id = "5", status = "withdrawn"),
        )

        val counts = mapOf(
            0 to apps.size,
            1 to apps.count { it.status == "pending" },
            2 to apps.count { it.isActiveApprovedApplication() },
            3 to apps.count { it.isCompletedApplication() },
            4 to apps.count { it.status == "rejected" || it.status == "withdrawn" },
        )

        assertEquals(5, counts[0])
        assertEquals(1, counts[1])
        assertEquals(1, counts[2])
        assertEquals(1, counts[3])
        assertEquals(2, counts[4])
    }
}
