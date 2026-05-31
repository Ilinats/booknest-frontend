package com.example.booknest.ui.myapplications.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class ApplicationUtilsTest {

    @Test
    fun formatDate_parsesIsoTimestamp() {
        assertEquals("Jun 15, 2024", formatDate("2024-06-15T10:30:00.000Z"))
    }

    @Test
    fun parseDate_returnsDateForValidIso() {
        val date = parseDate("2024-06-15T10:30:00.000Z")
        assertNotNull(date)
        val cal = Calendar.getInstance().apply { time = date!! }
        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH))
    }

    @Test
    fun parseDate_returnsNullForInvalid() {
        assertNull(parseDate("invalid"))
    }
}
