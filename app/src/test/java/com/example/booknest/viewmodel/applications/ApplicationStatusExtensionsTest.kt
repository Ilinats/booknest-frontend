package com.example.booknest.viewmodel.applications

import com.example.booknest.testutil.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationStatusExtensionsTest {

    @Test
    fun isCompletedApplication_trueWhenReviewSubmitted() {
        val app = TestFixtures.application(
            status = "approved",
            reviewSubmittedAt = "2024-07-01T00:00:00.000Z",
        )

        assertTrue(app.isCompletedApplication())
        assertFalse(app.isActiveApprovedApplication())
        assertEquals("completed", app.statusForDisplay())
    }

    @Test
    fun isCompletedApplication_trueWhenReadingStatusReviewed() {
        val app = TestFixtures.application(
            status = "approved",
            readingStatus = "reviewed",
        )

        assertTrue(app.isCompletedApplication())
    }

    @Test
    fun isActiveApprovedApplication_trueForApprovedWithoutCompletion() {
        val app = TestFixtures.application(status = "approved", readingStatus = "not_started")

        assertTrue(app.isActiveApprovedApplication())
        assertFalse(app.isCompletedApplication())
        assertEquals("approved", app.statusForDisplay())
    }

    @Test
    fun isPending_isCaseInsensitive() {
        assertTrue(TestFixtures.application(status = "pending").isPending())
        assertTrue(TestFixtures.application(status = "PENDING").isPending())
        assertFalse(TestFixtures.application(status = "approved").isPending())
    }

    @Test
    fun pendingCount_countsOnlyPendingApplications() {
        val apps = listOf(
            TestFixtures.application(id = "1", status = "pending"),
            TestFixtures.application(id = "2", status = "approved"),
            TestFixtures.application(id = "3", status = "pending"),
        )

        assertEquals(2, apps.pendingCount())
    }
}
