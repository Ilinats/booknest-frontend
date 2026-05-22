package com.example.booknest.ui.applications.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ApplicationDateUtilsTest {

    @Test
    fun formatDate_parsesIsoTimestamp() {
        assertEquals("06/15/2024", formatDate("2024-06-15T10:30:00.000Z"))
    }

    @Test
    fun formatDate_returnsEmptyForNullOrBlank() {
        assertEquals("", formatDate(null))
        assertEquals("", formatDate("   "))
    }

    @Test
    fun getApplicationDeadlineStatus_returnsNullForPastDeadline() {
        assertNull(getApplicationDeadlineStatus("2020-01-01T00:00:00.000Z"))
    }

    @Test
    fun getApplicationDeadlineStatus_returnsRemainingDaysForFutureDeadline() {
        val future = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 10)
        }
        val iso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(future.time)

        val status = getApplicationDeadlineStatus(iso)

        assertTrue(status == "10 days remaining" || status == "9 days remaining")
    }

    @Test
    fun getReviewDeadlineStatus_showsOverdueForPastDeadline() {
        val status = getReviewDeadlineStatus("2020-01-01T00:00:00.000Z")

        assertTrue(status?.startsWith("Overdue") == true)
    }
}
