package com.example.booknest.ui.notifications.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class NotificationUtilsTest {

    @Test
    fun formatNotificationTime_returnsJustNowForRecentTimestamp() {
        val now = Instant.now().toString()
        assertEquals("Just now", formatNotificationTime(now))
    }

    @Test
    fun formatNotificationTime_returnsMinutesAgo() {
        val fiveMinutesAgo = Instant.now().minusSeconds(5 * 60).toString()
        val formatted = formatNotificationTime(fiveMinutesAgo)
        assertTrue(formatted.contains("minute"))
    }

    @Test
    fun formatNotificationTime_returnsOriginalOnParseFailure() {
        assertEquals("not-a-date", formatNotificationTime("not-a-date"))
    }
}
