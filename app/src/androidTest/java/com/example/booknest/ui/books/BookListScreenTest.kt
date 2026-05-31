package com.example.booknest.ui.books

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.booknest.data.session.SearchHistoryManager
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import com.example.booknest.viewmodel.books.BookListBrowseUiState
import com.example.booknest.viewmodel.books.BrowseBooksViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test

class BookListScreenTest : Api24ComposeTest() {

    private val books = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    private val isLoading = MutableStateFlow(false)
    private val browseListHasMore = MutableStateFlow(false)
    private val bookListBrowseUi = MutableStateFlow(BookListBrowseUiState(showRecentSearches = true))
    private val browseListLoadingMore = MutableStateFlow(false)
    private val browseFilterGenres = MutableStateFlow(emptyList<com.example.booknest.domain.model.response.GenreResponse>())
    private val browseGenresLoading = MutableStateFlow(false)

    private lateinit var browseBooksViewModel: BrowseBooksViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var searchHistoryManager: SearchHistoryManager

    @Before
    fun setUp() {
        browseBooksViewModel = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        searchHistoryManager = mockk(relaxed = true)

        every { browseBooksViewModel.books } returns books
        every { browseBooksViewModel.isLoading } returns isLoading
        every { browseBooksViewModel.browseListHasMore } returns browseListHasMore
        every { browseBooksViewModel.bookListBrowseUi } returns bookListBrowseUi
        every { browseBooksViewModel.browseListLoadingMore } returns browseListLoadingMore
        every { browseBooksViewModel.browseFilterGenres } returns browseFilterGenres
        every { browseBooksViewModel.browseGenresLoading } returns browseGenresLoading
        every { searchHistoryManager.recentSearches } returns flowOf(listOf("fantasy"))
    }

    @Test
    fun browseScreenShowsSearchAndRecentSearches() {
        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                BookListScreen(
                    navController = null,
                    sessionManager = sessionManager,
                    browseBooksViewModel = browseBooksViewModel,
                    searchHistoryManager = searchHistoryManager,
                    searchQuery = null,
                    category = null,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.BROWSE_SEARCH_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.BROWSE_RECENT_SEARCHES).assertIsDisplayed()
        composeTestRule.onNodeWithText("fantasy").assertIsDisplayed()
    }

    @Test
    fun showsEmptyStateWhenNoBooks() {
        isLoading.value = false
        books.value = emptyList()

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                BookListScreen(
                    navController = null,
                    sessionManager = sessionManager,
                    browseBooksViewModel = browseBooksViewModel,
                    searchHistoryManager = searchHistoryManager,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.BROWSE_EMPTY_MESSAGE).assertIsDisplayed()
        composeTestRule.onNodeWithText("No books available").assertIsDisplayed()
    }

    @Test
    fun filterButtonTogglesFiltersPanel() {
        bookListBrowseUi.value = BookListBrowseUiState(showFilters = false)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                BookListScreen(
                    navController = null,
                    sessionManager = sessionManager,
                    browseBooksViewModel = browseBooksViewModel,
                    searchHistoryManager = searchHistoryManager,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.BROWSE_FILTER_BUTTON).performClick()
        verify { browseBooksViewModel.setBookListShowFilters(true) }
    }
}
