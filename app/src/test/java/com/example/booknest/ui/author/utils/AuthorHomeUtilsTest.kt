package com.example.booknest.ui.author.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class AuthorHomeUtilsTest {

    @Test
    fun formatDate_parsesIsoTimestamp() {
        assertEquals("Jun 15, 2024", AuthorHomeUtils.formatDate("2024-06-15T10:30:00.000Z"))
    }

    @Test
    fun isDeadlineApproaching_trueWithinSevenDays() {
        val soon = Instant.now().plus(5, ChronoUnit.DAYS).toString()
        assertTrue(AuthorHomeUtils.isDeadlineApproaching(soon))
    }

    @Test
    fun isDeadlineApproaching_falseForDistantDeadline() {
        val far = Instant.now().plus(30, ChronoUnit.DAYS).toString()
        assertFalse(AuthorHomeUtils.isDeadlineApproaching(far))
    }
}
