package com.example.booknest.viewmodel.books

import org.junit.Assert.assertEquals
import org.junit.Test

class BrowseFilterSnapshotTest {

    @Test
    fun from_mapsAllFilterFieldsFromUiState() {
        val ui = BookListBrowseUiState(
            debouncedSearchQuery = "fantasy",
            selectedGenres = setOf(1, 3),
            selectedAgeRating = "18+",
            selectedDistributionType = "digital",
            minRating = 2.5f,
            maxRating = 4f,
            selectedApplicationStatus = "open",
            selectedDeadlineFilter = "week",
            selectedSortBy = "rating",
        )

        val snapshot = BrowseFilterSnapshot.from(ui)

        assertEquals("fantasy", snapshot.debouncedSearch)
        assertEquals(setOf(1, 3), snapshot.genres)
        assertEquals("18+", snapshot.ageRating)
        assertEquals("digital", snapshot.distributionType)
        assertEquals(2.5f, snapshot.minRating)
        assertEquals(4f, snapshot.maxRating)
        assertEquals("open", snapshot.applicationStatus)
        assertEquals("week", snapshot.deadlineFilter)
        assertEquals("rating", snapshot.sortBy)
    }

    @Test
    fun from_usesDefaultsForEmptyUiState() {
        val snapshot = BrowseFilterSnapshot.from(BookListBrowseUiState())

        assertEquals("", snapshot.debouncedSearch)
        assertEquals(emptySet<Int>(), snapshot.genres)
        assertEquals(null, snapshot.ageRating)
        assertEquals(0f, snapshot.minRating)
        assertEquals(5f, snapshot.maxRating)
    }
}
