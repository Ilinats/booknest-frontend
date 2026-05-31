package com.example.booknest.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class BookDateUtilsTest {

    @Test
    fun dateOnlyToApiDeadlineEndOfDay_setsEndOfUtcDay() {
        assertEquals(
            "2025-06-15T23:59:59.999Z",
            BookDateUtils.dateOnlyToApiDeadlineEndOfDay("2025-06-15"),
        )
    }

    @Test
    fun apiDeadlineToDateOnly_extractsUtcCalendarDate() {
        assertEquals(
            "2025-06-15",
            BookDateUtils.apiDeadlineToDateOnly("2025-06-15T23:59:59.999Z"),
        )
    }

    @Test
    fun pickerMillisToDateOnly_usesUtcCalendarDay() {
        val instant = LocalDate.of(2025, 6, 15).atStartOfDay(ZoneOffset.UTC).toInstant()
        assertEquals("2025-06-15", BookDateUtils.pickerMillisToDateOnly(instant.toEpochMilli()))
    }

    @Test
    fun isApplicationDeadlinePassed_falseBeforeEndOfDay() {
        val future = BookDateUtils.dateOnlyToApiDeadlineEndOfDay(
            LocalDate.now().plusDays(2).toString(),
        )
        assertFalse(BookDateUtils.isApplicationDeadlinePassed(future))
    }

    @Test
    fun isApplicationDeadlinePassed_trueForPastDate() {
        assertTrue(BookDateUtils.isApplicationDeadlinePassed("2020-01-01T23:59:59.999Z"))
    }

    @Test
    fun formatEndsInText_tomorrowWhenOneDayAway() {
        val tomorrow = LocalDate.now().plusDays(1)
        val iso = BookDateUtils.dateOnlyToApiDeadlineEndOfDay(tomorrow.toString())
        assertEquals("Ends in 1 day", BookDateUtils.formatEndsInText(iso))
    }

    @Test
    fun normalizeDeadlineForApi_leavesIsoUnchanged() {
        val iso = "2025-06-01T12:00:00.000Z"
        assertEquals(iso, BookDateUtils.normalizeDeadlineForApi(iso))
    }
}
