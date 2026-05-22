package com.example.booknest.ui.books

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.books.components.browse.BookListRecentSearchesCard
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import com.example.booknest.viewmodel.books.BrowseBooksViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class BookListRecentSearchesCardTest : Api24ComposeTest() {

    @Test
    fun displaysRecentSearchesAndAppliesChip() {
        val browseBooksViewModel = mockk<BrowseBooksViewModel>(relaxed = true)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                BookListRecentSearchesCard(
                    recentSearches = listOf("fantasy", "mystery"),
                    browseBooksViewModel = browseBooksViewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.BROWSE_RECENT_SEARCHES).assertIsDisplayed()
        composeTestRule.onNodeWithText("Recent Searches").assertIsDisplayed()
        composeTestRule.onNodeWithText("fantasy").performClick()
        verify { browseBooksViewModel.applyBookListRecentSearch("fantasy") }
    }
}
