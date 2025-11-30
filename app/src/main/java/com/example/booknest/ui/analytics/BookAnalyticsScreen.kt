package com.example.booknest.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.*
import com.example.booknest.viewmodel.AnalyticsViewModel
import com.example.booknest.viewmodel.AnalyticsViewModelFactory
import com.example.booknest.viewmodel.BookAnalyticsUiState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAnalyticsScreen(
    navController: NavController,
    authManager: AuthManager,
    bookId: String,
    analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(authManager)
    )
) {
    val analyticsState by analyticsViewModel.bookAnalyticsState.collectAsState()
    val currentAnalytics by analyticsViewModel.currentBookAnalytics.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        analyticsViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(bookId) {
        analyticsViewModel.loadDetailedBookAnalytics(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Analytics") },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

@Composable
fun BookAnalyticsContent(
    analytics: DetailedBookAnalytics,
    analyticsViewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Summary Cards
        item {
            SummaryCardsSection(summary = analytics.summary)
        }

        // Review Analytics
        item {
            ReviewAnalyticsSection(
                reviewAnalytics = analytics.reviewAnalytics,
                analyticsViewModel = analyticsViewModel
            )
        }

        // Application Analytics
        item {
            ApplicationAnalyticsSection(applicationAnalytics = analytics.applicationAnalytics)
        }

        // Recent Reviews
        item {
            RecentReviewsSection(recentReviews = analytics.recentReviews)
        }
    }
}

@Composable
fun SummaryCardsSection(summary: BookAnalyticsSummary) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummaryCard(
                    title = "Total Reviews",
                    value = summary.totalReviews.toString(),
                    icon = Icons.Default.Star,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                SummaryCard(
                    title = "Average Rating",
                    value = String.format("%.1f/5", summary.averageRating),
                    icon = Icons.Default.Star,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            item {
                SummaryCard(
                    title = "Positive Feedback",
                    value = "${summary.positiveFeedback}%",
                    icon = Icons.Default.ThumbUp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.width(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                tint = color
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
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
fun ReviewAnalyticsSection(
    reviewAnalytics: ReviewAnalytics,
    analyticsViewModel: AnalyticsViewModel
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
                text = "Review Analytics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Rating Distribution
            RatingDistributionChart(
                ratingDistribution = reviewAnalytics.ratingDistribution,
                analyticsViewModel = analyticsViewModel
            )
            
            // Review Types
            ReviewTypesSection(reviewTypes = reviewAnalytics.reviewTypes)
            
            // Additional Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Average Word Count",
                    value = "${reviewAnalytics.averageWordCount} words"
                )
                MetricItem(
                    label = "Total Reviews",
                    value = reviewAnalytics.totalReviews.toString()
                )
            }
        }
    }
}

@Composable
fun RatingDistributionChart(
    ratingDistribution: RatingDistribution,
    analyticsViewModel: AnalyticsViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Rating Distribution",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        val ratingList = analyticsViewModel.getRatingDistributionList(ratingDistribution)
        val maxCount = analyticsViewModel.getTopRatingCount(ratingDistribution)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ratingList.forEach { (rating, count) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Bar
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(if (maxCount > 0) (count.toFloat() / maxCount * 100).dp else 0.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (rating) {
                                    5 -> Color(0xFF4CAF50) // Green
                                    4 -> Color(0xFF8BC34A) // Light Green
                                    3 -> Color(0xFFFFC107) // Amber
                                    2 -> Color(0xFFFF9800) // Orange
                                    else -> Color(0xFFF44336) // Red
                                }
                            )
                    )
                    
                    // Rating number
                    Text(
                        text = rating.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Count
                    Text(
                        text = count.toString(),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewTypesSection(reviewTypes: ReviewTypes) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Review Types",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ReviewTypeItem(
                type = "Text Reviews",
                count = reviewTypes.text,
                icon = Icons.Default.Email
            )
            ReviewTypeItem(
                type = "Link Reviews",
                count = reviewTypes.link,
                icon = Icons.Default.Email
            )
        }
    }
}

@Composable
fun ReviewTypeItem(
    type: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "$type: $count",
            fontSize = 14.sp
        )
    }
}

@Composable
fun ApplicationAnalyticsSection(applicationAnalytics: ApplicationAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Application Analytics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Application Stats Grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ApplicationStatCard(
                        title = "Total Applications",
                        value = applicationAnalytics.totalApplications.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    ApplicationStatCard(
                        title = "Approved",
                        value = applicationAnalytics.approvedApplications.toString(),
                        color = Color(0xFF4CAF50)
                    )
                }
                item {
                    ApplicationStatCard(
                        title = "Pending",
                        value = applicationAnalytics.pendingApplications.toString(),
                        color = Color(0xFFFF9800)
                    )
                }
                item {
                    ApplicationStatCard(
                        title = "Rejected",
                        value = applicationAnalytics.rejectedApplications.toString(),
                        color = Color(0xFFF44336)
                    )
                }
            }
            
            // Approval/Rejection Rates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Approval Rate",
                    value = "${applicationAnalytics.approvalRate}%"
                )
                MetricItem(
                    label = "Rejection Rate",
                    value = "${applicationAnalytics.rejectionRate}%"
                )
            }
        }
    }
}

@Composable
fun ApplicationStatCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
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
fun RecentReviewsSection(recentReviews: List<RecentReview>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recent Reviews",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
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

@Composable
fun RecentReviewItem(review: RecentReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                    text = "${review.application.reader.firstName ?: ""} ${review.application.reader.lastName ?: ""}".trim().ifEmpty { review.application.reader.username },
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
                            tint = Color(0xFFFFC107)
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
