package com.example.booknest.ui.myapplications

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import com.example.booknest.viewmodel.applications.ApplicationSortOption
import com.example.booknest.viewmodel.applications.ApplicationStats
import com.example.booknest.viewmodel.applications.ApplicationViewModel
import com.example.booknest.viewmodel.files.FileUiState
import com.example.booknest.viewmodel.files.FileViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class MyApplicationsScreenTest : Api24ComposeTest() {

    private val isLoading = MutableStateFlow(false)
    private val filteredApplications = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    private val approvedApplicationsBySub =
        MutableStateFlow(Pair<List<ApplicationResponse>, List<ApplicationResponse>>(emptyList(), emptyList()))
    private val stats = MutableStateFlow(ApplicationStats(0, 0.0, 0, 0))
    private val tabCounts = MutableStateFlow(mapOf(0 to 0, 1 to 2, 2 to 0, 3 to 0, 4 to 0))
    private val searchQuery = MutableStateFlow("")
    private val selectedTab = MutableStateFlow(0)
    private val sortOption = MutableStateFlow(ApplicationSortOption.APPLICATION_DATE)
    private val fileUiState = MutableStateFlow(FileUiState())

    private lateinit var applicationViewModel: ApplicationViewModel
    private lateinit var fileViewModel: FileViewModel

    @Before
    fun setUp() {
        applicationViewModel = mockk(relaxed = true)
        fileViewModel = mockk(relaxed = true)

        every { applicationViewModel.isLoading } returns isLoading
        every { applicationViewModel.filteredApplications } returns filteredApplications
        every { applicationViewModel.approvedApplicationsBySub } returns approvedApplicationsBySub
        every { applicationViewModel.applicationStats } returns stats
        every { applicationViewModel.tabCounts } returns tabCounts
        every { applicationViewModel.searchQuery } returns searchQuery
        every { applicationViewModel.selectedTab } returns selectedTab
        every { applicationViewModel.sortOption } returns sortOption
        every { fileViewModel.uiState } returns fileUiState
    }

    @Test
    fun showsQuickStatsAndTabCounts() {
        stats.value = ApplicationStats(total = 5, approvalRate = 40.0, reviewsThisMonth = 1, pendingReviews = 0)
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)
        val sessionManager = mockk<SessionManager>(relaxed = true)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                MyApplicationsScreen(
                    navController = navController,
                    sessionManager = sessionManager,
                    applicationViewModel = applicationViewModel,
                    fileViewModel = fileViewModel,
                )
            }
        }

        composeTestRule.onNodeWithText("Quick Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pending (2)").assertIsDisplayed()
    }

    @Test
    fun searchFieldUpdatesViewModel() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)
        val sessionManager = mockk<SessionManager>(relaxed = true)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                MyApplicationsScreen(
                    navController = navController,
                    sessionManager = sessionManager,
                    applicationViewModel = applicationViewModel,
                    fileViewModel = fileViewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.APPLICATIONS_SEARCH_FIELD).performTextInput("lottery")
        composeTestRule.waitForIdle()
        verify(atLeast = 1) { applicationViewModel.updateSearchQuery(any()) }
    }

    @Test
    fun tabClickUpdatesSelectedTab() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)
        val sessionManager = mockk<SessionManager>(relaxed = true)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                MyApplicationsScreen(
                    navController = navController,
                    sessionManager = sessionManager,
                    applicationViewModel = applicationViewModel,
                    fileViewModel = fileViewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag("${UiTestTags.APPLICATIONS_TAB_PREFIX}1").performClick()
        verify { applicationViewModel.updateSelectedTab(1) }
    }

    @Test
    fun pendingTabShowsEmptyStateMessage() {
        selectedTab.value = 1
        filteredApplications.value = emptyList()
        isLoading.value = false

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)
        val sessionManager = mockk<SessionManager>(relaxed = true)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                MyApplicationsScreen(
                    navController = navController,
                    sessionManager = sessionManager,
                    applicationViewModel = applicationViewModel,
                    fileViewModel = fileViewModel,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.EMPTY_APPLICATIONS_MESSAGE).assertIsDisplayed()
        composeTestRule.onNodeWithText("No pending applications").assertIsDisplayed()
    }
}
