package com.example.booknest.ui.author.components.books

import org.junit.Assert.assertEquals
import org.junit.Test

class BookUtilsTest {

    @Test
    fun formatSelectionMethod_mapsKnownValues() {
        assertEquals("Author Selects", formatSelectionMethod("author_selects"))
        assertEquals("First Come First Served", formatSelectionMethod("first_come"))
        assertEquals("Random Selection", formatSelectionMethod("lottery"))
    }

    @Test
    fun formatSelectionMethod_returnsUnknownForNull() {
        assertEquals("Unknown", formatSelectionMethod(null))
    }

    @Test
    fun formatSelectionMethod_titleCasesUnknownSnakeCase() {
        assertEquals("Custom Rule", formatSelectionMethod("custom_rule"))
    }

    @Test
    fun formatDate_parsesIsoTimestamp() {
        val formatted = formatDate("2024-06-15T10:30:00.000Z")
        assertEquals("Jun 15, 2024", formatted)
    }

    @Test
    fun formatDate_returnsOriginalOnParseFailure() {
        assertEquals("not-a-date", formatDate("not-a-date"))
    }

    @Test
    fun formatDate_returnsEmptyForNull() {
        assertEquals("", formatDate(null))
    }
}
