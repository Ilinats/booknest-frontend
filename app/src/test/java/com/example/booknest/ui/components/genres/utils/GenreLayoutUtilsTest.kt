package com.example.booknest.ui.components.genres.utils

import com.example.booknest.testutil.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenreLayoutUtilsTest {

    @Test
    fun estimateButtonWidth_scalesWithTextLength() {
        val short = estimateButtonWidth("Fi", horizontalPadding = 32f)
        val long = estimateButtonWidth("Science Fiction", horizontalPadding = 32f)
        assertTrue(long > short)
    }

    @Test
    fun createSmartRows_fitsThreeShortGenresInOneRow() {
        val genres = listOf(
            TestFixtures.genre(1, "Fi"),
            TestFixtures.genre(2, "Ho"),
            TestFixtures.genre(3, "Dr"),
        )

        val rows = createSmartRows(
            genres = genres,
            availableWidth = 1000f,
            minButtonWidth = 50f,
            buttonSpacing = 8f,
            horizontalPadding = 32f,
        )

        assertEquals(1, rows.size)
        assertEquals(3, rows.first().size)
    }

    @Test
    fun createSmartRows_splitsLongGenresIntoSeparateRows() {
        val genres = listOf(
            TestFixtures.genre(1, "Very Long Genre Name That Cannot Fit"),
            TestFixtures.genre(2, "Another Extremely Long Genre Label"),
        )

        val rows = createSmartRows(
            genres = genres,
            availableWidth = 200f,
            minButtonWidth = 80f,
            buttonSpacing = 8f,
            horizontalPadding = 32f,
        )

        assertEquals(2, rows.size)
        assertEquals(1, rows[0].size)
        assertEquals(1, rows[1].size)
    }
}
