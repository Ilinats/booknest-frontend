package com.example.booknest.ui.books

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.books.components.browse.BookListSearchHeader
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import com.example.booknest.viewmodel.books.BookListBrowseUiState
import com.example.booknest.viewmodel.books.BrowseBooksViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class BookListSearchHeaderTest : Api24ComposeTest() {

    private lateinit var browseBooksViewModel: BrowseBooksViewModel
    private val browseUi = BookListBrowseUiState(searchQuery = "")

    @Before
    fun setUp() {
        browseBooksViewModel = mockk(relaxed = true)
    }

    @Test
    fun searchFieldAcceptsInput() {
        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                BookListSearchHeader(
                    browseUi = browseUi,
                    recentSearches = emptyList(),
                    showFiltersForBrowse = true,
                    browseBooksViewModel = browseBooksViewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.BROWSE_SEARCH_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.BROWSE_SEARCH_FIELD).performTextInput("fantasy")
        composeTestRule.waitForIdle()
        verify(atLeast = 1) { browseBooksViewModel.updateBookListSearchInput(any()) }
    }

    @Test
    fun filterButtonTogglesFilters() {
        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                BookListSearchHeader(
                    browseUi = browseUi,
                    recentSearches = emptyList(),
                    showFiltersForBrowse = true,
                    browseBooksViewModel = browseBooksViewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.BROWSE_FILTER_BUTTON).performClick()
        verify { browseBooksViewModel.setBookListShowFilters(true) }
    }
}
