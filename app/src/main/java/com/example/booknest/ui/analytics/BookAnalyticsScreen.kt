package com.example.booknest.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.*
import com.example.booknest.viewmodel.AnalyticsViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.viewmodel.BookAnalyticsUiState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAnalyticsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    bookId: String,
    analyticsViewModel: AnalyticsViewModel = getViewModel()
) {
    val analyticsState by analyticsViewModel.bookAnalyticsState.collectAsState()
    val currentAnalytics by analyticsViewModel.currentBookAnalytics.collectAsState()

    LaunchedEffect(bookId) {
        analyticsViewModel.loadDetailedBookAnalytics(bookId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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

        Scaffold(
            topBar = {
                Surface(
                    shadowElevation = 4.dp,
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                "Book Analytics",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        navigationIcon = {
                            BackButton(onClick = { navController.popBackStack() })
                        }
                    )
                }
            }
        ) { paddingValues ->
            val currentState = analyticsState
            when (currentState) {
                is BookAnalyticsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is BookAnalyticsUiState.Error -> {
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
                                text = currentState.message,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = {
                                analyticsViewModel.loadDetailedBookAnalytics(bookId)
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is BookAnalyticsUiState.Success -> {
                    BookAnalyticsContent(
                        analytics = currentState.analytics,
                        analyticsViewModel = analyticsViewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun BookAnalyticsContent(
    analytics: DetailedBookAnalyticsResponse,
    analyticsViewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val reviewStats = analytics.reviewStatistics ?: analytics.reviewAnalytics
    val appStats = analytics.applicationStatistics ?: analytics.applicationAnalytics

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            SummaryCardsSection(summary = analytics.summary)
        }

        reviewStats?.let {
            item {
                ReviewStatisticsSection(
                    reviewStatistics = it,
                    analyticsViewModel = analyticsViewModel
                )
            }
        }

        appStats?.let {
            item {
                ApplicationStatisticsSection(applicationStatistics = it)
            }
        }

        analytics.reviewPerformance?.let {
            item {
                ReviewPerformanceSection(reviewPerformance = it)
            }
        }

        item {
            RecentReviewsSection(recentReviews = analytics.recentReviews)
        }
    }
}

@Composable
fun SummaryCardsSection(summary: BookAnalyticsSummaryResponse) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Reviews",
                value = summary.totalReviews.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Average Rating",
                value = String.format("%.1f", summary.averageRating),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Positive Feedback",
                value = "${summary.positiveFeedback}%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ReviewStatisticsSection(
    reviewStatistics: ReviewAnalyticsResponse,
    analyticsViewModel: AnalyticsViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review Statistics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Average Rating",
                        value = "${reviewStatistics.averageRating}/5",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Positive Feedback",
                        value = "${reviewStatistics.positiveFeedback}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Reviews",
                        value = reviewStatistics.totalReviews.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Avg Word Count",
                        value = "${reviewStatistics.averageWordCount}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                Text(
                    text = "Rating Distribution",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (reviewStatistics.ratingBreakdown != null && reviewStatistics.ratingBreakdown.isNotEmpty()) {
                    RatingBreakdownChart(ratingBreakdown = reviewStatistics.ratingBreakdown)
                } else {
                    RatingDistributionChart(
                        ratingDistribution = reviewStatistics.ratingDistribution,
                        analyticsViewModel = analyticsViewModel
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                ReviewTypesSection(reviewTypes = reviewStatistics.reviewTypes)
            }
        }
    }
}

@Composable
fun RatingBreakdownChart(ratingBreakdown: List<RatingBreakdownItemResponse>) {
    val maxCount = ratingBreakdown.maxOfOrNull { it.count } ?: 1
    val minBarHeight = 8.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ratingBreakdown.sortedByDescending { it.rating }.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val barHeight = if (maxCount > 0) {
                        maxOf(minBarHeight, (item.count.toFloat() / maxCount * 120).dp)
                    } else {
                        minBarHeight
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(barHeight)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (item.rating) {
                                    5 -> MaterialTheme.colorScheme.primary
                                    4 -> MaterialTheme.colorScheme.secondary
                                    3 -> MaterialTheme.colorScheme.tertiary
                                    2 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                    )

                    Text(
                        text = item.count.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.rating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingDistributionChart(
    ratingDistribution: RatingDistributionResponse,
    analyticsViewModel: AnalyticsViewModel
) {
    val ratingList = analyticsViewModel.getRatingDistributionList(ratingDistribution)
    val maxCount = analyticsViewModel.getTopRatingCount(ratingDistribution)
    val minBarHeight = 8.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ratingList.sortedByDescending { it.first }.forEach { (rating, count) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val barHeight = if (maxCount > 0) {
                        maxOf(minBarHeight, (count.toFloat() / maxCount * 120).dp)
                    } else {
                        minBarHeight
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(barHeight)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (rating) {
                                    5 -> MaterialTheme.colorScheme.primary
                                    4 -> MaterialTheme.colorScheme.secondary
                                    3 -> MaterialTheme.colorScheme.tertiary
                                    2 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                    )

                    Text(
                        text = count.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = rating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewTypesSection(reviewTypes: ReviewTypesResponse) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Review Types",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Text Reviews",
                value = reviewTypes.text.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Link Reviews",
                value = reviewTypes.link.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ApplicationStatisticsSection(applicationStatistics: ApplicationAnalyticsResponse) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Application Statistics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ApplicationStatCard(
                        title = "Total Applications",
                        value = applicationStatistics.totalApplications.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ApplicationStatCard(
                        title = "Approved",
                        value = applicationStatistics.approvedApplications.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ApplicationStatCard(
                        title = "Pending",
                        value = applicationStatistics.pendingApplications.toString(),
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    ApplicationStatCard(
                        title = "Rejected",
                        value = applicationStatistics.rejectedApplications.toString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (applicationStatistics.applicationsThisMonth != null ||
                    applicationStatistics.approvedApplicationsThisMonth != null
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "This Month",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        applicationStatistics.applicationsThisMonth?.let {
                            StatCard(
                                title = "Applications",
                                value = it.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        applicationStatistics.approvedApplicationsThisMonth?.let {
                            StatCard(
                                title = "Approved",
                                value = it.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        applicationStatistics.rejectedApplicationsThisMonth?.let {
                            StatCard(
                                title = "Rejected",
                                value = it.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Approval Rate",
                        value = "${applicationStatistics.approvalRate}%",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Rejection Rate",
                        value = "${applicationStatistics.rejectionRate}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    applicationStatistics.averageResponseTime?.let { time ->
                        StatCard(
                            title = "Avg Response Time",
                            value = if (time < 24) "$time hours" else "${time / 24} days",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    applicationStatistics.applicationConversionRate?.let { rate ->
                        StatCard(
                            title = "Conversion Rate",
                            value = "$rate%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewPerformanceSection(reviewPerformance: ReviewPerformanceResponse) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review Performance",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Submission Rate",
                        value = "${reviewPerformance.reviewSubmissionRate}%",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completion Rate",
                        value = "${reviewPerformance.reviewCompletionRate}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                reviewPerformance.averageReviewTime?.let { time ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Avg Review Time",
                            value = String.format("%.1f days", time),
                            modifier = Modifier.weight(1f)
                        )
                        reviewPerformance.averageWordCount?.let { wordCount ->
                            StatCard(
                                title = "Avg Word Count",
                                value = "$wordCount",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } ?: run {
                    reviewPerformance.averageWordCount?.let { wordCount ->
                        StatCard(
                            title = "Avg Word Count",
                            value = "$wordCount",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RecentReviewsSection(recentReviews: List<RecentReviewResponse>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Recent Reviews",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (recentReviews.isEmpty()) {
                    Text(
                        text = "No reviews yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    recentReviews.take(3).forEach { review ->
                        RecentReviewItem(review = review)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentReviewItem(review: RecentReviewResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${review.application.reader.firstName ?: ""} ${review.application.reader.lastName ?: ""}".trim()
                        .ifEmpty { review.application.reader.username },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(review.rating) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            review.reviewContent?.let { content ->
                Text(
                    text = content,
                    fontSize = 12.sp,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${review.wordCount} words",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = review.reviewType.replaceFirstChar { it.uppercase() },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
