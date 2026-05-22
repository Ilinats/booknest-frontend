package com.example.booknest.ui.books

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.books.components.browse.BookListActiveFiltersBanner
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import com.example.booknest.viewmodel.books.BrowseBooksViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class BookListActiveFiltersBannerTest : Api24ComposeTest() {

    @Test
    fun clearAllInvokesViewModel() {
        val browseBooksViewModel = mockk<BrowseBooksViewModel>(relaxed = true)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                BookListActiveFiltersBanner(
                    activeFilters = listOf("Search: dune"),
                    browseFilterGenres = emptyList(),
                    browseBooksViewModel = browseBooksViewModel,
                )
            }
        }

        composeTestRule.onNodeWithText("Active filters").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search: dune").assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.BROWSE_ACTIVE_FILTERS_CLEAR).performClick()
        verify { browseBooksViewModel.clearBookListSearchImmediate() }
    }
}
