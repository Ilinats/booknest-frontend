package com.example.booknest.ui.author.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class DeadlineDisplayUtilsTest {

    @Test
    fun parseDeadlineInstant_parsesIsoString() {
        val instant = DeadlineDisplayUtils.parseDeadlineInstant("2025-12-01T12:00:00.000Z")
        assertNotNull(instant)
    }

    @Test
    fun parseDeadlineInstant_returnsNullForBlank() {
        assertNull(DeadlineDisplayUtils.parseDeadlineInstant(""))
    }

    @Test
    fun formatEndsInText_returnsEndsTodayForSameDay() {
        val today = Instant.now().plus(12, ChronoUnit.HOURS).toString()
        assertEquals("Ends today", DeadlineDisplayUtils.formatEndsInText(today))
    }

    @Test
    fun formatEndsInText_returnsNullForPastDeadline() {
        assertNull(DeadlineDisplayUtils.formatEndsInText("2020-01-01T00:00:00.000Z"))
    }

    @Test
    fun formatDaysLeftText_formatsFutureDeadline() {
        val future = Instant.now().plus(3, ChronoUnit.DAYS).toString()
        val text = DeadlineDisplayUtils.formatDaysLeftText(future)
        assertTrue(text == "3 days left" || text == "2 days left")
    }

    @Test
    fun hasDisplayableDeadline_returnsTrueForValidIso() {
        val future = Instant.now().plus(1, ChronoUnit.DAYS).toString()
        assertTrue(DeadlineDisplayUtils.hasDisplayableDeadline(future))
    }

    @Test
    fun hasDisplayableDeadline_returnsFalseForNull() {
        assertFalse(DeadlineDisplayUtils.hasDisplayableDeadline(null))
    }
}
