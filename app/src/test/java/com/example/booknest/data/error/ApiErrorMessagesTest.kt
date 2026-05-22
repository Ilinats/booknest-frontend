package com.example.booknest.data.error

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorMessagesTest {

    @Test
    fun forCode_mapsApplicationErrors() {
        assertEquals(
            "You have already applied for this book.",
            ApiErrorMessages.forCode("APPLICATION_ALREADY_EXISTS"),
        )
        assertEquals(
            "The application deadline for this book has passed.",
            ApiErrorMessages.forCode("APPLICATION_DEADLINE_PASSED"),
        )
    }

    @Test
    fun forCode_mapsAuthErrors() {
        assertEquals(
            "Email/username or password is incorrect.",
            ApiErrorMessages.forCode("INVALID_CREDENTIALS"),
        )
        assertEquals(
            "Please sign in to continue.",
            ApiErrorMessages.forCode("MISSING_TOKEN"),
        )
    }

    @Test
    fun resolve_prefersHumanMessageOverCode() {
        assertEquals(
            "Title is required",
            ApiErrorMessages.resolve(message = "Title is required", errorCode = "BOOK_NOT_FOUND"),
        )
    }

    @Test
    fun resolve_mapsCodeInMessageField() {
        assertEquals(
            "Book not found.",
            ApiErrorMessages.resolve(message = "BOOK_NOT_FOUND", errorCode = null),
        )
    }

    @Test
    fun looksLikeMachineCode_detectsSnakeCaseCodes() {
        assertTrue(ApiErrorMessages.looksLikeMachineCode("APPLICATION_NOT_FOUND"))
        assertFalse(ApiErrorMessages.looksLikeMachineCode("Please verify your email"))
    }
}
