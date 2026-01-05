package com.example.booknest.ui.author

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.navigation.AuthorBottomBarScreen
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.author.components.ActiveCampaignsSection
import com.example.booknest.ui.author.components.ActionNeededSection
import com.example.booknest.ui.author.components.AuthorTopBar
import com.example.booknest.ui.author.components.QuickActionsSection
import com.example.booknest.ui.author.components.QuickStatsSection
import com.example.booknest.ui.author.components.RecentReviewsSection
import com.example.booknest.ui.author.components.profile.WelcomeSection
import com.example.booknest.ui.author.components.PerformanceSummarySection
import com.example.booknest.ui.author.utils.AuthorHomeUtils
import com.example.booknest.viewmodel.AuthorViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

enum class BookStatus(val value: String) {
    DRAFT("draft"),
    ACTIVE("active"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    ARCHIVED("archived")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AuthorHomeScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    authorViewModel: AuthorViewModel = getViewModel(),
    authRepository: AuthRepository = koinInject()
) {
    val myBooks by authorViewModel.myBooks.collectAsState()
    val isLoadingBooks by authorViewModel.isLoadingBooks.collectAsState()
    val quickStats by authorViewModel.quickStats.collectAsState()
    val authorStats by authorViewModel.authorStats.collectAsState()
    val recentReviews by authorViewModel.recentReviews.collectAsState()
    val overdueReviews by authorViewModel.overdueReviews.collectAsState()
    val bookStats by authorViewModel.bookStats.collectAsState()
    val currentUser by sessionManager.currentUser.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        authorViewModel.reloadHomeScreenData()
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute == AuthorBottomBarScreen.Home.route) {
            authorViewModel.reloadHomeScreenData()
        }
    }

    LaunchedEffect(myBooks) {
        myBooks.filter { it.status == BookStatus.ACTIVE.value }.forEach { book ->
            authorViewModel.getBookStats(book.id)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                AuthorTopBar(
                    currentUser = currentUser,
                    onSeriesManagementClick = { navController.navigate(Screen.SeriesManagement.route) },
                    onViewAnalyticsClick = { navController.navigate(Screen.AuthorAnalytics.route) },
                    onAccountClick = {
                        navController.navigate(Screen.Profile.createRoute(null))
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.PrivacySettings.route)
                    },
                    onSignOut = {},
                    sessionManager = sessionManager,
                    authRepository = authRepository
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Background decoration circles
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-175).dp, y = (-175).dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-135).dp, y = (-135).dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 175.dp, y = 175.dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 135.dp, y = 135.dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 32.dp,
                    end = 16.dp,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                item {
                    WelcomeSection(
                        authorName = currentUser?.firstName ?: currentUser?.username ?: "Author",
                        pendingApplications = quickStats.pendingApplications
                    )
                }

                item {
                    ActionNeededSection(
                        pendingApplications = quickStats.pendingApplications,
                        overdueReviews = overdueReviews.size,
                        booksWithDeadline = myBooks.filter { book ->
                            book.applicationDeadline != null && AuthorHomeUtils.isDeadlineApproaching(book.applicationDeadline)
                        }
                    )
                }

                item {
                    ActiveCampaignsSection(
                        books = myBooks.filter { it.status == BookStatus.ACTIVE.value },
                        bookStats = bookStats,
                        isLoading = isLoadingBooks,
                        onBookClick = { bookId ->
                            navController.navigate("book_applications/$bookId")
                        },
                        onEditClick = { bookId ->
                            navController.navigate(Screen.BookEdit.createRoute(bookId))
                        }
                    )
                }

                item {
                    QuickStatsSection(quickStats = quickStats)
                }

                item {
                    PerformanceSummarySection(
                        applicationsThisMonth = quickStats.applicationsThisMonth,
                        approvalRate = quickStats.approvalRate,
                        reviewCompletionRate = authorStats?.stats?.reviewCompletionRate?.toInt()
                            ?: 0
                    )
                }

                item {
                    RecentReviewsSection(
                        reviews = recentReviews,
                        onViewAllClick = {
                            if (recentReviews.isNotEmpty()) {
                                recentReviews.first().application?.bookId?.let { bookId ->
                                    navController.navigate(
                                        Screen.BookApplicationDetail.createRoute(
                                            bookId
                                        )
                                    )
                                }
                            }
                        },
                        onReviewClick = { review ->
                            review.application?.bookId?.let { bookId ->
                                navController.navigate(
                                    Screen.BookApplicationDetail.createRoute(
                                        bookId
                                    )
                                )
                            }
                        }
                    )
                }

                item {
                    QuickActionsSection(
                        onCreateBookClick = { navController.navigate(Screen.BookCreation.route) },
                        onViewAnalyticsClick = { navController.navigate(Screen.AuthorAnalytics.route) },
                        onManageSeriesClick = { navController.navigate(Screen.SeriesManagement.route) }
                    )
                }
            }
        }
    }
}


