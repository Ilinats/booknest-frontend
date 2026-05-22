package com.example.booknest.ui.auth.components.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountUtilsTest {

    @Test
    fun isValidUrl_acceptsHttpAndHttps() {
        assertTrue(isValidUrl("https://example.com"))
        assertTrue(isValidUrl("http://example.com/path"))
    }

    @Test
    fun isValidUrl_rejectsInvalidUrls() {
        assertFalse(isValidUrl(""))
        assertFalse(isValidUrl("ftp://example.com"))
        assertFalse(isValidUrl("not-a-url"))
    }

    @Test
    fun getErrorMessage_mapsKnownAuthErrors() {
        assertEquals(
            "Invalid username/email or password. Please check your credentials and try again.",
            getErrorMessage("Invalid credentials"),
        )
        assertEquals(
            "Network error. Please check your internet connection and try again.",
            getErrorMessage("Network connection failed"),
        )
        assertEquals(
            "Request timed out. Please try again.",
            getErrorMessage("Request timeout"),
        )
    }

    @Test
    fun getErrorMessage_returnsOriginalForUnknown() {
        assertEquals("Custom server error", getErrorMessage("Custom server error"))
    }

    @Test
    fun getErrorMessage_returnsDefaultForNull() {
        assertEquals("An error occurred", getErrorMessage(null))
    }
}
