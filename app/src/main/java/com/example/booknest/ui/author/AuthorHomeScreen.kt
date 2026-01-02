package com.example.booknest.ui.author

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import com.example.booknest.ui.theme.DarkNavyBlue
import com.example.booknest.ui.theme.BackgroundWhite
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.booknest.domain.model.response.UserResponse
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.AuthorBottomBarScreen
import com.example.booknest.navigation.Screen
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.viewmodel.AuthorViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class BookStatus(val value: String) {
    DRAFT("draft"),
    ACTIVE("active"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    ARCHIVED("archived")
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorHomeScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    authorViewModel: AuthorViewModel = getViewModel()
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
                    .shadow(elevation = 4.dp)
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
                    sessionManager = sessionManager
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-175).dp, y = (-175).dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-135).dp, y = (-135).dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 175.dp, y = 175.dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 135.dp, y = 135.dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle)
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
                            book.applicationDeadline != null && isDeadlineApproaching(book.applicationDeadline)
                        },
                        onPendingApplicationsClick = {
                            val activeBook =
                                myBooks.firstOrNull { it.status == BookStatus.ACTIVE.value }
                            activeBook?.let { navController.navigate("book_applications/${it.id}") }
                        },
                        onOverdueReviewsClick = {
                            val activeBook =
                                myBooks.firstOrNull { it.status == BookStatus.ACTIVE.value }
                            activeBook?.let { navController.navigate("book_applications/${it.id}") }
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
                                    navController.navigate(Screen.BookDetails.createRoute(bookId))
                                }
                            }
                        },
                        onReviewClick = { review ->
                            review.application?.bookId?.let { bookId ->
                                navController.navigate(Screen.BookDetails.createRoute(bookId))
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

@Composable
fun WelcomeSection(
    authorName: String,
    pendingApplications: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Welcome back, $authorName!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = DarkNavyBlue
        )
    }
}

@Composable
fun ActionNeededSection(
    pendingApplications: Int,
    overdueReviews: Int,
    booksWithDeadline: List<BookResponse>,
    onPendingApplicationsClick: () -> Unit,
    onOverdueReviewsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE5E5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Action Needed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }

            if (pendingApplications > 0 || overdueReviews > 0 || booksWithDeadline.isNotEmpty()) {
                if (pendingApplications > 0) {
                    ActionItem(
                        title = "Pending Applications",
                        count = pendingApplications,
                        onClick = onPendingApplicationsClick
                    )
                }

                if (overdueReviews > 0) {
                    ActionItem(
                        title = "Overdue Reviews",
                        count = overdueReviews,
                        onClick = onOverdueReviewsClick
                    )
                }

                if (booksWithDeadline.isNotEmpty()) {
                    ActionItem(
                        title = "Books with Deadline Approaching",
                        count = booksWithDeadline.size,
                        onClick = {
                            booksWithDeadline.firstOrNull()?.id?.let { bookId ->
                            }
                        }
                    )
                }
            } else {
                Text(
                    text = "All caught up! No actions needed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun ActionItem(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActiveCampaignsSection(
    books: List<BookResponse>,
    bookStats: Map<String, com.example.booknest.domain.model.response.BookStatsResponse>,
    isLoading: Boolean,
    onBookClick: (String) -> Unit,
    onEditClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Active Campaigns",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkNavyBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (books.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8DFE4)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No active campaigns",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkNavyBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create a new book to start accepting applications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575)
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(books) { book ->
                    ActiveCampaignCard(
                        book = book,
                        stats = bookStats[book.id],
                        onBookClick = { onBookClick(book.id) },
                        onEditClick = { onEditClick(book.id) }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActiveCampaignCard(
    book: BookResponse,
    stats: com.example.booknest.domain.model.response.BookStatsResponse?,
    onBookClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onBookClick() }
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SkyBluePeriwinkle.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverImageUrl != null) {
                    AsyncImage(
                        model = book.coverImageUrl,
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "📖",
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DarkNavyBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp
            )

            Text(
                text = "Applications: ${stats?.approvedReaders ?: 0} / ${book.totalCopies ?: "∞"}",
                style = MaterialTheme.typography.bodySmall,
                color = DarkNavyBlue
            )

            book.applicationDeadline?.let { deadline ->
                val daysLeft: Long = calculateDaysUntilDeadline(deadline)
                Surface(
                    color = Color(0xFFFFE5E5),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (daysLeft < 0) "Deadline passed" else "$daysLeft ${if (daysLeft == 1L) "day" else "days"} left",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onBookClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8DFE4)
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text(
                        "View",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkNavyBlue
                    )
                }
                IconButton(
                    onClick = { onEditClick() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFE8DFE4),
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = DarkNavyBlue
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStatsSection(quickStats: AuthorViewModel.QuickStats) {
    Column {
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkNavyBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Books",
                value = quickStats.totalBooks.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Active Books",
                value = quickStats.activeBooks.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Pending Applications",
                value = quickStats.pendingApplications.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Avg. Response Time",
                value = quickStats.avgResponseTime,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Reviews",
                value = quickStats.totalReviews.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Average Rating",
                value = String.format("%.1f", quickStats.averageRating),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PerformanceSummarySection(
    applicationsThisMonth: Int,
    approvalRate: Int,
    reviewCompletionRate: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Performance Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkNavyBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PerformanceMetric(
                    label = "This Month's Applications",
                    value = applicationsThisMonth.toString(),
                    modifier = Modifier.weight(1f)
                )
                PerformanceMetric(
                    label = "Approval Rate",
                    value = "$approvalRate%",
                    modifier = Modifier.weight(1f)
                )
                PerformanceMetric(
                    label = "Review Completion",
                    value = "$reviewCompletionRate%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PerformanceMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = DarkNavyBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF757575),
            modifier = Modifier.padding(top = 2.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun RecentReviewsSection(
    reviews: List<ReviewResponse>,
    onViewAllClick: () -> Unit,
    onReviewClick: (ReviewResponse) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Reviews",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkNavyBlue
            )
            if (reviews.isNotEmpty()) {
                TextButton(onClick = onViewAllClick) {
                    Text("View All", color = DarkNavyBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (reviews.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reviews yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                reviews.take(3).forEach { review ->
                    ReviewCard(
                        review = review,
                        onClick = { onReviewClick(review) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: ReviewResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    review.application?.bookTitle?.let { bookTitle ->
                        Text(
                            text = bookTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavyBlue,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                if (index < review.rating) Icons.Filled.Star else Icons.Default.Star,
                                contentDescription = "Star",
                                modifier = Modifier.size(18.dp),
                                tint = if (index < review.rating) Color(0xFFFFB300) else Color(
                                    0xFFE0E0E0
                                )
                            )
                        }
                    }
                }
            }

            review.reviewContent?.takeIf { it.isNotBlank() }?.let { content ->
                Text(
                    text = if (content.length > 150) {
                        content.take(150) + "..."
                    } else {
                        content
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(review.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                review.application?.reader?.let { reader ->
                    val readerName = listOfNotNull(reader.firstName, reader.lastName)
                        .joinToString(" ")
                        .ifBlank { reader.username }
                    Text(
                        text = "— $readerName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onCreateBookClick: () -> Unit,
    onViewAnalyticsClick: () -> Unit,
    onManageSeriesClick: () -> Unit
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkNavyBlue,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = onCreateBookClick,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color(0xFFE8DFE4), shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE8DFE4)
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = DarkNavyBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Book", color = DarkNavyBlue)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onViewAnalyticsClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkNavyBlue
                )
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = DarkNavyBlue
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Analytics", color = DarkNavyBlue)
            }
            OutlinedButton(
                onClick = onManageSeriesClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkNavyBlue
                )
            ) {
                Icon(
                    Icons.Default.Collections,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = DarkNavyBlue
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Manage Series", color = DarkNavyBlue)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkNavyBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DarkNavyBlue
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateDaysUntilDeadline(deadline: String): Long {
    return try {
        val deadlineDate = Instant.parse(deadline)
        val now = Instant.now()
        ChronoUnit.DAYS.between(now, deadlineDate)
    } catch (e: Exception) {
        0L
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun isDeadlineApproaching(deadline: String): Boolean {
    return try {
        val deadlineDate = Instant.parse(deadline)
        val now = Instant.now()
        val daysUntil = ChronoUnit.DAYS.between(now, deadlineDate)
        daysUntil in 0..7
    } catch (e: Exception) {
        false
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorTopBar(
    currentUser: UserResponse?,
    onSeriesManagementClick: () -> Unit,
    onViewAnalyticsClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSignOut: () -> Unit,
    sessionManager: SessionManager
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.shadow(elevation = 4.dp),
        color = BackgroundWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BookNest",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DarkNavyBlue
            )

            Box {
                val avatarUrl = currentUser?.profilePictureUrl ?: currentUser?.avatarUrl
                val initials = remember(currentUser) {
                    val source = when {
                        !currentUser?.firstName.isNullOrBlank() -> currentUser?.firstName
                        !currentUser?.username.isNullOrBlank() -> currentUser?.username
                        else -> null
                    }
                    source?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { menuExpanded = true }
                        .background(SkyBluePeriwinkle),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials,
                            color = DarkNavyBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Series Management", color = DarkNavyBlue) },
                        onClick = {
                            menuExpanded = false
                            onSeriesManagementClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View Analytics", color = DarkNavyBlue) },
                        onClick = {
                            menuExpanded = false
                            onViewAnalyticsClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sign out", color = Color(0xFFD32F2F)) },
                        onClick = {
                            menuExpanded = false
                            coroutineScope.launch {
                                sessionManager.logout()
                                onSignOut()
                            }
                        }
                    )
                }
            }
        }
    }
}
