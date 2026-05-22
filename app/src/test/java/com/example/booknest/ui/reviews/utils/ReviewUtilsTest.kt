package com.example.booknest.ui.reviews.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewUtilsTest {

    @Test
    fun isValidUrl_acceptsHttpAndHttps() {
        assertTrue(isValidUrl("https://review.example.com/post"))
        assertFalse(isValidUrl("example.com"))
    }

    @Test
    fun formatDate_parsesIsoTimestamp() {
        assertEquals("Jan 15, 2024", formatDate("2024-01-15T10:00:00.000Z"))
    }

    @Test
    fun formatDateNullable_returnsUnknownForNull() {
        assertEquals("Unknown date", formatDateNullable(null))
    }
}
