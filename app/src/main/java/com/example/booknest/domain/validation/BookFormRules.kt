package com.example.booknest.domain.validation

/**
 * Text and numeric limits for author book create/edit flows (aligned with backend DTOs).
 */
object BookFormRules {
    const val TITLE_MAX = 255
    const val SHORT_DESCRIPTION_MAX = 500
    const val FULL_DESCRIPTION_MAX = 10_000
    const val SELECTION_CRITERIA_MAX = 2_000
    const val SERIES_NAME_MAX = 255
    const val SERIES_DESCRIPTION_MAX = 2_000

    const val PAGE_COUNT_MIN = 1
    const val PAGE_COUNT_MAX = 100_000
    const val TOTAL_COPIES_MIN = 1
    const val SERIES_ORDER_MIN = 1

    fun validateTitle(title: String): String? = when {
        title.isBlank() -> "Title is required"
        title.length > TITLE_MAX -> exceededMessage("Title", TITLE_MAX)
        else -> null
    }

    fun validateShortDescription(description: String): String? =
        if (description.length > SHORT_DESCRIPTION_MAX) {
            exceededMessage("Short description", SHORT_DESCRIPTION_MAX)
        } else {
            null
        }

    fun validateFullDescription(description: String): String? =
        if (description.length > FULL_DESCRIPTION_MAX) {
            exceededMessage("Full description", FULL_DESCRIPTION_MAX)
        } else {
            null
        }

    fun validateSelectionCriteria(criteria: String): String? =
        if (criteria.length > SELECTION_CRITERIA_MAX) {
            exceededMessage("Selection criteria", SELECTION_CRITERIA_MAX)
        } else {
            null
        }

    fun validatePageCount(pageCount: String): String? {
        val trimmed = pageCount.trim()
        if (trimmed.isBlank()) return null
        val parsed = trimmed.toIntOrNull() ?: return null
        return when {
            parsed < PAGE_COUNT_MIN -> "Page count must be at least $PAGE_COUNT_MIN"
            parsed > PAGE_COUNT_MAX -> "Page count must be ${formatLimit(PAGE_COUNT_MAX)} or less"
            else -> null
        }
    }

    fun validateTotalCopies(copies: String): String? {
        val trimmed = copies.trim()
        if (trimmed.isBlank()) return null
        val parsed = trimmed.toIntOrNull() ?: return null
        return when {
            parsed < TOTAL_COPIES_MIN -> "Total copies must be at least $TOTAL_COPIES_MIN"
            else -> null
        }
    }

    fun validateSeriesOrder(order: String): String? {
        val trimmed = order.trim()
        if (trimmed.isBlank()) return null
        val parsed = trimmed.toIntOrNull() ?: return null
        return when {
            parsed < SERIES_ORDER_MIN -> "Series order must be at least $SERIES_ORDER_MIN"
            else -> null
        }
    }

    fun validateSeriesName(name: String): String? = when {
        name.isBlank() -> "Series name is required"
        name.length > SERIES_NAME_MAX -> exceededMessage("Series name", SERIES_NAME_MAX)
        else -> null
    }

    fun validateSeriesDescription(description: String): String? =
        if (description.length > SERIES_DESCRIPTION_MAX) {
            exceededMessage("Description", SERIES_DESCRIPTION_MAX)
        } else {
            null
        }

    fun isBasicInfoValid(
        title: String,
        shortDescription: String,
        fullDescription: String,
        pageCount: String,
    ): Boolean = validateTitle(title) == null &&
        validateShortDescription(shortDescription) == null &&
        validateFullDescription(fullDescription) == null &&
        validatePageCount(pageCount) == null

    private fun exceededMessage(fieldLabel: String, max: Int): String =
        "$fieldLabel must be ${formatLimit(max)} characters or less"

    private fun formatLimit(value: Int): String = when (value) {
        FULL_DESCRIPTION_MAX -> "10,000"
        PAGE_COUNT_MAX -> "100,000"
        SELECTION_CRITERIA_MAX -> "2,000"
        SERIES_DESCRIPTION_MAX -> "2,000"
        else -> value.toString()
    }
}
