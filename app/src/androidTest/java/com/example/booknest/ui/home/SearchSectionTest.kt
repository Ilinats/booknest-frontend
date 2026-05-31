package com.example.booknest.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.home.components.sections.SearchSection
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchSectionTest : Api24ComposeTest() {

    @Test
    fun searchFieldAcceptsInput() {
        var query by mutableStateOf("")
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                SearchSection(
                    searchQuery = query,
                    onSearchQueryChange = { query = it },
                    searchResults = emptyList(),
                    isSearching = false,
                    navController = navController,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.HOME_SEARCH_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.HOME_SEARCH_FIELD).performTextInput("dune")
        composeTestRule.waitForIdle()
        assertEquals("dune", query)
    }
}
