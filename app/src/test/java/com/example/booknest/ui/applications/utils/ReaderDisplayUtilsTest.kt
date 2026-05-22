package com.example.booknest.ui.applications.utils

import com.example.booknest.domain.model.response.BookLeakFingerprintResponse
import com.example.booknest.testutil.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderDisplayUtilsTest {

    @Test
    fun formatApplicationReaderDisplay_prefersFullNameAndUsername() {
        val reader = TestFixtures.applicationReader(
            firstName = "Jane",
            lastName = "Doe",
            username = "janedoe",
        )

        assertEquals("Jane Doe (@janedoe)", formatApplicationReaderDisplay(reader))
    }

    @Test
    fun formatApplicationReaderDisplay_fallsBackToUsernameOnly() {
        val reader = TestFixtures.applicationReader(
            firstName = "",
            lastName = "",
            username = "janedoe",
        )

        assertEquals("@janedoe", formatApplicationReaderDisplay(reader))
    }

    @Test
    fun formatApplicationReaderDisplay_fallsBackToEmail() {
        val reader = TestFixtures.applicationReader(
            firstName = "",
            lastName = "",
            username = "",
            email = "jane@example.com",
        )

        assertEquals("jane@example.com", formatApplicationReaderDisplay(reader))
    }

    @Test
    fun readerDisplayLabel_usesApplicationReaderWhenMatched() {
        val reader = TestFixtures.applicationReader(id = "r-1", username = "matched")
        val apps = listOf(
            TestFixtures.application(id = "a-1").copy(reader = reader),
        )
        val fingerprint = BookLeakFingerprintResponse(
            readerId = "r-1",
            bookId = "book-1",
            issuedAt = 1L,
            format = "epub",
            readerFirstName = "Ignored",
            readerLastName = null,
            readerUsername = null,
            readerEmail = null,
        )

        assertEquals("Jane Doe (@matched)", fingerprint.readerDisplayLabel(apps))
    }
}
