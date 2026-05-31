package com.example.booknest.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookFormRulesTest {

    @Test
    fun validateTitle_blankRequired() {
        assertEquals("Title is required", BookFormRules.validateTitle(""))
    }

    @Test
    fun validateTitle_exceededOnlyWhenOverLimit() {
        assertNull(BookFormRules.validateTitle("a".repeat(BookFormRules.TITLE_MAX)))
        assertTrue(
            BookFormRules.validateTitle("a".repeat(BookFormRules.TITLE_MAX + 1))!!
                .contains("255"),
        )
    }

    @Test
    fun validateShortDescription_nullWithinLimit() {
        assertNull(BookFormRules.validateShortDescription("hello"))
        assertTrue(
            BookFormRules.validateShortDescription("x".repeat(BookFormRules.SHORT_DESCRIPTION_MAX + 1))!!
                .contains("500"),
        )
    }

    @Test
    fun validateSelectionCriteria_exceededMessage() {
        assertNull(BookFormRules.validateSelectionCriteria("ok"))
        assertTrue(
            BookFormRules.validateSelectionCriteria("x".repeat(BookFormRules.SELECTION_CRITERIA_MAX + 1))!!
                .contains("2,000"),
        )
    }
}
