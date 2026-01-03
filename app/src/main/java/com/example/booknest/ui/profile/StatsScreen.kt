package com.example.booknest.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.viewmodel.ProfileViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.viewmodel.StatsUiState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    authorId: String? = null,
    profileViewModel: ProfileViewModel = getViewModel()
) {
    val currentUser by sessionManager.currentUser.collectAsState()
    val statsState by profileViewModel.statsState.collectAsState()
    val currentStats by profileViewModel.currentStats.collectAsState()

    LaunchedEffect(authorId) {
        if (authorId != null) {
            profileViewModel.loadAuthorStats(authorId)
        } else {
            profileViewModel.loadMyStats()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        },
    ) { paddingValues ->
        val currentStatsState = statsState
        when (currentStatsState) {
            is StatsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is StatsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = currentStatsState.message,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            if (authorId != null) {
                                profileViewModel.loadAuthorStats(authorId)
                            } else {
                                profileViewModel.loadMyStats()
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is StatsUiState.Success -> {
                StatsContent(
                    stats = currentStatsState.stats,
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {}
        }
    }
}

@Composable
fun StatsContent(
    stats: com.example.booknest.domain.model.response.UserStatsResponse,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        UserStatsHeader(user = stats.user)

        StatsGrid(stats = stats.stats)

        if (stats.stats.userType == "author") {
            AuthorAnalyticsLink(navController = navController)
        } else {
            ReadingStatisticsSection(stats = stats.stats)
            GenresBreakdownSection(stats = stats.stats)
        }

        AdditionalStatsInfo(stats = stats.stats)
    }
}

@Composable
fun UserStatsHeader(
    user: com.example.booknest.domain.model.response.UserResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Statistics for",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim()
                    .ifEmpty { user.username },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@${user.username} • ${user.userType?.replaceFirstChar { it.uppercase() }}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatsGrid(
    stats: com.example.booknest.domain.model.response.UserStatsDataResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (stats.userType == "author") {
                AuthorStatsGrid(stats = stats)
            } else {
                ReaderStatsGrid(stats = stats)
            }
        }
    }
}

@Composable
fun AuthorStatsGrid(
    stats: com.example.booknest.domain.model.response.UserStatsDataResponse
) {
    val statItems = listOf(
        StatItem("Total Books", (stats.totalBooks ?: 0).toString(), Icons.Default.Menu),
        StatItem("Published Books", (stats.publishedBooks ?: 0).toString(), Icons.Default.Check),
        StatItem("Draft Books", (stats.draftBooks ?: 0).toString(), Icons.Default.Edit),
        StatItem("Total Applications", stats.totalApplications.toString(), Icons.Default.DateRange),
        StatItem(
            "Approved Applications",
            stats.approvedApplications.toString(),
            Icons.Default.CheckCircle
        ),
        StatItem("Pending Applications", stats.pendingApplications.toString(), Icons.Default.Face),
        StatItem("Total Reviews", (stats.totalReviews ?: 0).toString(), Icons.Default.Star),
        StatItem(
            "Average Rating",
            String.format("%.1f", stats.averageRating ?: 0.0),
            Icons.Default.Star
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(400.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statItems) { statItem ->
            StatCard(
                title = statItem.title,
                value = statItem.value,
                icon = statItem.icon
            )
        }
    }
}

@Composable
fun ReaderStatsGrid(
    stats: com.example.booknest.domain.model.response.UserStatsDataResponse
) {
    val statItems = listOf(
        StatItem("Total Applications", stats.totalApplications.toString(), Icons.Default.Menu),
        StatItem(
            "Approved Applications",
            stats.approvedApplications.toString(),
            Icons.Default.CheckCircle
        ),
        StatItem(
            "Pending Applications",
            stats.pendingApplications.toString(),
            Icons.Default.DateRange
        ),
        StatItem("Completed Reads", (stats.completedReads ?: 0).toString(), Icons.Default.Done),
        StatItem("Reviews Written", (stats.totalReviews ?: 0).toString(), Icons.Default.Star)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(250.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statItems) { statItem ->
            StatCard(
                title = statItem.title,
                value = statItem.value,
                icon = statItem.icon
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdditionalStatsInfo(
    stats: com.example.booknest.domain.model.response.UserStatsDataResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Additional Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (stats.userType == "author") {
                val approvalRate = if (stats.totalApplications > 0) {
                    (stats.approvedApplications.toDouble() / stats.totalApplications * 100)
                } else 0.0

                val publishRate = if (stats.totalBooks ?: 0 > 0) {
                    ((stats.publishedBooks ?: 0).toDouble() / (stats.totalBooks ?: 1) * 100)
                } else 0.0

                AdditionalInfoItem(
                    label = "Application Approval Rate",
                    value = String.format("%.1f%%", approvalRate)
                )

                AdditionalInfoItem(
                    label = "Publication Rate",
                    value = String.format("%.1f%%", publishRate)
                )

                if (stats.averageRating != null && stats.averageRating > 0) {
                    AdditionalInfoItem(
                        label = "Average Rating",
                        value = String.format("%.1f/5.0", stats.averageRating)
                    )
                }
            } else {
                val completionRate = if (stats.approvedApplications > 0) {
                    ((stats.completedReads ?: 0).toDouble() / stats.approvedApplications * 100)
                } else 0.0

                val reviewRate = if (stats.completedReads ?: 0 > 0) {
                    ((stats.totalReviews ?: 0).toDouble() / (stats.completedReads ?: 1) * 100)
                } else 0.0

                AdditionalInfoItem(
                    label = "Reading Completion Rate",
                    value = String.format("%.1f%%", completionRate)
                )

                AdditionalInfoItem(
                    label = "Review Submission Rate",
                    value = String.format("%.1f%%", reviewRate)
                )
            }
        }
    }
}

@Composable
fun AdditionalInfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

data class StatItem(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun ReadingStatisticsSection(
    stats: com.example.booknest.domain.model.response.UserStatsDataResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reading Statistics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReadingStatCard(
                    title = "This Month",
                    value = (stats.completedReadsThisMonth ?: 0).toString(),
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
                ReadingStatCard(
                    title = "This Year",
                    value = (stats.completedReadsThisYear ?: 0).toString(),
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
                ReadingStatCard(
                    title = "All Time",
                    value = (stats.completedReads ?: 0).toString(),
                    icon = Icons.Default.History,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (stats.pagesRead != null && stats.pagesRead > 0) {
                    ReadingStatCard(
                        title = "Pages Read",
                        value = formatNumber(stats.pagesRead),
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (stats.averageReadingTime != null && stats.averageReadingTime > 0) {
                    ReadingStatCard(
                        title = "Avg Reading Time",
                        value = formatReadingTime(stats.averageReadingTime),
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (stats.reviewCompletionRate != null) {
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Review Completion Rate",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Percentage of completed reads with reviews",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = String.format("%.1f%%", stats.reviewCompletionRate),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ReadingStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GenresBreakdownSection(
    stats: com.example.booknest.domain.model.response.UserStatsDataResponse
) {
    val genresBreakdown = stats.genresBreakdown
    if (genresBreakdown != null && genresBreakdown.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Genres Breakdown",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Books read by genre",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val sortedGenres =
                    genresBreakdown.toList().sortedByDescending { (_, count) -> count }
                val maxCount = sortedGenres.maxOfOrNull { (_, count) -> count } ?: 1

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sortedGenres.take(10).forEach { (genre, count) ->
                        GenreBreakdownItem(
                            genre = genre,
                            count = count,
                            maxCount = maxCount
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenreBreakdownItem(
    genre: String,
    count: Int,
    maxCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = genre,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count ${if (count == 1) "book" else "books"}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (maxCount > 0) count.toFloat() / maxCount else 0f)
                    .fillMaxHeight()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun AuthorAnalyticsLink(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Detailed Analytics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Get comprehensive insights into your performance, trends, and reader demographics",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { navController.navigate("author_analytics") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Author Analytics")
            }
        }
    }
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}

private fun formatReadingTime(hours: Double): String {
    return when {
        hours >= 24 -> String.format("%.1f days", hours / 24.0)
        hours >= 1 -> String.format("%.1f hrs", hours)
        else -> String.format("%.0f min", hours * 60)
    }
}
