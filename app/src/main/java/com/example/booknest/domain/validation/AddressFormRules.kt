package com.example.booknest.domain.validation

/**
 * Mirrors [CreateAddressDto] / [UpdateAddressDto] constraints on the backend.
 */
object AddressFormRules {
    const val DEFAULT_COUNTRY = "Bulgaria"

    const val STREET_MIN = 1
    const val STREET_MAX = 255
    const val CITY_MIN = 1
    const val CITY_MAX = 100
    const val POSTAL_MIN = 1
    const val POSTAL_MAX = 20
    const val COUNTRY_MIN = 1
    const val COUNTRY_MAX = 100

    fun isStreetValid(value: String): Boolean =
        value.trim().length in STREET_MIN..STREET_MAX

    fun isCityValid(value: String): Boolean =
        value.trim().length in CITY_MIN..CITY_MAX

    fun isPostalValid(value: String): Boolean =
        value.trim().length in POSTAL_MIN..POSTAL_MAX

    fun isCountryValid(value: String): Boolean =
        value.trim().length in COUNTRY_MIN..COUNTRY_MAX

    fun isFormValid(
        streetAddress: String,
        city: String,
        postalCode: String,
        country: String
    ): Boolean = isStreetValid(streetAddress) &&
        isCityValid(city) &&
        isPostalValid(postalCode) &&
        isCountryValid(country)

    /** Used for create; backend defaults to Bulgaria when country is omitted. */
    fun normalizeCountryForCreate(country: String): String =
        country.trim().ifBlank { DEFAULT_COUNTRY }

    /** Used for update; omit field when blank so optional validation is skipped. */
    fun countryForUpdate(country: String): String? =
        country.trim().takeIf { it.isNotEmpty() }

    fun streetError(value: String): String? = when {
        value.trim().isEmpty() -> "Street address is required"
        value.trim().length > STREET_MAX -> "Max $STREET_MAX characters"
        else -> null
    }

    fun cityError(value: String): String? = when {
        value.trim().isEmpty() -> "City is required"
        value.trim().length > CITY_MAX -> "Max $CITY_MAX characters"
        else -> null
    }

    fun postalError(value: String): String? = when {
        value.trim().isEmpty() -> "Postal code is required"
        value.trim().length > POSTAL_MAX -> "Max $POSTAL_MAX characters"
        else -> null
    }

    fun countryError(value: String): String? = when {
        value.trim().isEmpty() -> "Country is required"
        value.trim().length > COUNTRY_MAX -> "Max $COUNTRY_MAX characters"
        else -> null
    }
}
