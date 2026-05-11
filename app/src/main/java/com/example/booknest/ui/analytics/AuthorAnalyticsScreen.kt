package com.example.booknest.ui.analytics

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.*
import com.example.booknest.ui.author.components.analytics.DateRangeOption
import com.example.booknest.ui.author.components.analytics.DateRangeSelector
import com.example.booknest.ui.author.components.analytics.comparison.BookPerformanceComparisonSection
import com.example.booknest.ui.author.components.analytics.demographics.ReaderAnalyticsSection
import com.example.booknest.ui.author.components.analytics.overview.OverviewSection
import com.example.booknest.ui.author.components.analytics.performance.PerformanceSection
import com.example.booknest.ui.author.components.analytics.trends.TrendsSection
import com.example.booknest.viewmodel.analytics.AnalyticsViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.ui.state.UiState
import com.example.booknest.ui.components.BackgroundDecoration
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorAnalyticsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    analyticsViewModel: AnalyticsViewModel = getViewModel()
) {
    val analyticsState by analyticsViewModel.authorAnalyticsState.collectAsState()
    val currentAnalytics by analyticsViewModel.currentAuthorAnalytics.collectAsState()
    val bookPerformanceComparison by analyticsViewModel.bookPerformanceComparison.collectAsState()
    var selectedDateRange by remember { mutableStateOf(DateRangeOption.ALL_TIME) }

    LaunchedEffect(Unit) {
        analyticsViewModel.loadAuthorAnalytics(selectedDateRange.apiValue)
        analyticsViewModel.loadBookPerformanceComparison()
    }

    LaunchedEffect(selectedDateRange) {
        analyticsViewModel.loadAuthorAnalytics(selectedDateRange.apiValue)
    }

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
                            "Author Analytics",
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
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
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
                            text = (currentState as UiState.Error).message,
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

            is UiState.Success -> {
                AuthorAnalyticsContent(
                    analytics = currentState.data,
                    analyticsViewModel = analyticsViewModel,
                    bookPerformanceComparison = bookPerformanceComparison,
                    selectedDateRange = selectedDateRange,
                    onDateRangeChange = { selectedDateRange = it },
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {}
        }
    }
}

@Composable
fun AuthorAnalyticsContent(
    analytics: AuthorAnalyticsResponse,
    analyticsViewModel: AnalyticsViewModel,
    bookPerformanceComparison: List<BookPerformanceComparisonResponse>,
    selectedDateRange: DateRangeOption,
    onDateRangeChange: (DateRangeOption) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundDecoration(modifier = Modifier.fillMaxSize())

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DateRangeSelector(
                selectedDateRange = selectedDateRange,
                onDateRangeChange = onDateRangeChange
            )

            OverviewSection(overview = analytics.overview)

            PerformanceSection(
                performance = analytics.performance,
                navController = navController
            )

            analytics.readerAnalytics?.let { readerAnalytics ->
                ReaderAnalyticsSection(readerAnalytics = readerAnalytics)
            }

            TrendsSection(
                trends = analytics.trends,
                analyticsViewModel = analyticsViewModel
            )

            if (bookPerformanceComparison.isNotEmpty()) {
                BookPerformanceComparisonSection(
                    books = bookPerformanceComparison,
                    navController = navController
                )
            }
        }
    }
}