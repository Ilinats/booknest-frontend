package com.example.booknest.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressFormRulesTest {

    @Test
    fun isFormValid_requiresAllFields() {
        assertTrue(
            AddressFormRules.isFormValid(
                streetAddress = "123 Main St",
                city = "Sofia",
                postalCode = "1000",
                country = "Bulgaria",
            ),
        )
        assertFalse(
            AddressFormRules.isFormValid(
                streetAddress = "",
                city = "Sofia",
                postalCode = "1000",
                country = "Bulgaria",
            ),
        )
    }

    @Test
    fun normalizeCountryForCreate_defaultsToBulgaria() {
        assertEquals("Bulgaria", AddressFormRules.normalizeCountryForCreate(""))
        assertEquals("Germany", AddressFormRules.normalizeCountryForCreate("Germany"))
    }

    @Test
    fun countryForUpdate_returnsNullWhenBlank() {
        assertNull(AddressFormRules.countryForUpdate("  "))
        assertEquals("France", AddressFormRules.countryForUpdate("France"))
    }

    @Test
    fun streetError_reportsRequiredAndMaxLength() {
        assertEquals("Street address is required", AddressFormRules.streetError(""))
        assertEquals("Max 255 characters", AddressFormRules.streetError("a".repeat(256)))
        assertNull(AddressFormRules.streetError("Valid Street"))
    }

    @Test
    fun cityError_reportsRequired() {
        assertEquals("City is required", AddressFormRules.cityError(""))
        assertNull(AddressFormRules.cityError("Sofia"))
    }
}
