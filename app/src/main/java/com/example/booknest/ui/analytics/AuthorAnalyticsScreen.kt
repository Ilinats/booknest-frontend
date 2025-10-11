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
import com.example.booknest.viewmodel.AuthorAnalyticsUiState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorAnalyticsScreen(
    navController: NavController,
    authManager: AuthManager,
    analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(authManager)
    )
) {
    val analyticsState by analyticsViewModel.authorAnalyticsState.collectAsState()
    val currentAnalytics by analyticsViewModel.currentAuthorAnalytics.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        analyticsViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        analyticsViewModel.loadAuthorAnalytics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Author Analytics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val currentState = analyticsState
        when (currentState) {
            is AuthorAnalyticsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is AuthorAnalyticsUiState.Error -> {
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
                            analyticsViewModel.loadAuthorAnalytics()
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is AuthorAnalyticsUiState.Success -> {
                AuthorAnalyticsContent(
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
fun AuthorAnalyticsContent(
    analytics: AuthorAnalytics,
    analyticsViewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Overview Section
        item {
            OverviewSection(overview = analytics.overview)
        }

        // Performance Section
        item {
            PerformanceSection(
                performance = analytics.performance,
                navController = null // We'll pass this when we have navigation
            )
        }

        // Trends Section
        item {
            TrendsSection(
                trends = analytics.trends,
                analyticsViewModel = analyticsViewModel
            )
        }
    }
}

@Composable
fun OverviewSection(overview: AuthorAnalyticsOverview) {
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
            
            // Overview Grid
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OverviewCard(
                        title = "Total Books",
                        value = overview.totalBooks.toString(),
                        icon = Icons.Default.Favorite,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    OverviewCard(
                        title = "Published Books",
                        value = overview.publishedBooks.toString(),
                        icon = Icons.Default.Check,
                        color = Color(0xFF4CAF50)
                    )
                }
                item {
                    OverviewCard(
                        title = "Draft Books",
                        value = overview.draftBooks.toString(),
                        icon = Icons.Default.Edit,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OverviewCard(
                        title = "Total Applications",
                        value = overview.totalApplications.toString(),
                        icon = Icons.Default.Menu,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                item {
                    OverviewCard(
                        title = "Approval Rate",
                        value = "${overview.overallApprovalRate}%",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF4CAF50)
                    )
                }
                item {
                    OverviewCard(
                        title = "Average Rating",
                        value = String.format("%.1f/5", overview.averageRating),
                        icon = Icons.Default.Star,
                        color = Color(0xFFFFC107)
                    )
                }
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OverviewCard(
                        title = "Total Reviews",
                        value = overview.totalReviews.toString(),
                        icon = Icons.Default.Star,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun OverviewCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.width(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
fun PerformanceSection(
    performance: AuthorPerformance,
    navController: NavController?
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
                text = "Performance",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Performance Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PerformanceMetric(
                    label = "Books with Reviews",
                    value = performance.booksWithReviews.toString()
                )
                PerformanceMetric(
                    label = "Average Rating",
                    value = String.format("%.1f/5", performance.averageRating)
                )
            }
            
            // Top Performing Books
            Text(
                text = "Top Performing Books",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (performance.topPerformingBooks.isEmpty()) {
                Text(
                    text = "No books with reviews yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                performance.topPerformingBooks.forEach { book ->
                    TopPerformingBookItem(book = book)
                }
            }
        }
    }
}

@Composable
fun PerformanceMetric(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
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

@Composable
fun TopPerformingBookItem(book: TopPerformingBook) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${book.reviewCount} reviews",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFC107)
                )
                Text(
                    text = String.format("%.1f", book.averageRating),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TrendsSection(
    trends: AuthorTrends,
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
                text = "Trends (Last 6 Months)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Monthly Applications Chart
            TrendChart(
                title = "Monthly Applications",
                data = trends.monthlyApplications,
                color = MaterialTheme.colorScheme.primary,
                analyticsViewModel = analyticsViewModel
            )
            
            // Monthly Reviews Chart
            TrendChart(
                title = "Monthly Reviews",
                data = trends.monthlyReviews,
                color = Color(0xFF4CAF50),
                analyticsViewModel = analyticsViewModel
            )
        }
    }
}

@Composable
fun TrendChart(
    title: String,
    data: List<MonthlyData>,
    color: Color,
    analyticsViewModel: AnalyticsViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        if (data.isEmpty()) {
            Text(
                text = "No data available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val maxCount = data.maxOfOrNull { it.count } ?: 1
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.take(6).forEach { monthData ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Bar
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(if (maxCount > 0) (monthData.count.toFloat() / maxCount * 80).dp else 0.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                        
                        // Month
                        Text(
                            text = analyticsViewModel.formatMonth(monthData.month),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Count
                        Text(
                            text = monthData.count.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}
