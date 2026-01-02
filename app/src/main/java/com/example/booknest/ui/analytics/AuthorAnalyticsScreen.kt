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
import com.example.booknest.ui.theme.DarkNavyBlue
import com.example.booknest.ui.theme.SkyBluePeriwinkle
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
import com.example.booknest.viewmodel.AnalyticsViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.viewmodel.AuthorAnalyticsUiState
import kotlinx.coroutines.flow.collectLatest

enum class DateRangeOption(val displayName: String, val apiValue: String) {
    LAST_7_DAYS("Last 7 Days", "last_7_days"),
    LAST_30_DAYS("Last 30 Days", "last_30_days"),
    LAST_90_DAYS("Last 90 Days", "last_90_days"),
    YEAR("Year", "year"),
    ALL_TIME("All Time", "all_time")
}

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
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedDateRange by remember { mutableStateOf(DateRangeOption.ALL_TIME) }

    LaunchedEffect(Unit) {
        analyticsViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
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
                            color = DarkNavyBlue
                        )
                    },
                    navigationIcon = {
                        BackButton(onClick = { navController.popBackStack() })
                    }
                )
            }
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
    bookPerformanceComparison: List<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse>,
    selectedDateRange: DateRangeOption,
    onDateRangeChange: (DateRangeOption) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1E9EE))
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

@Composable
fun DateRangeSelector(
    selectedDateRange: DateRangeOption,
    onDateRangeChange: (DateRangeOption) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
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
                text = "Date Range",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val sortedDateRanges = remember(selectedDateRange) {
                DateRangeOption.values().toList().sortedBy { range ->
                    if (range == selectedDateRange) 0 else 1
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedDateRanges) { range ->
                    FilterChip(
                        selected = selectedDateRange == range,
                        onClick = { onDateRangeChange(range) },
                        label = { Text(range.displayName) }
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun OverviewSection(overview: AuthorAnalyticsOverviewResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statItems = listOf(
                    "Total Books" to overview.totalBooks,
                    "Published Books" to overview.publishedBooks,
                    "Draft Books" to overview.draftBooks,
                    "Total Applications" to overview.totalApplications,
                    "Approval Rate" to overview.overallApprovalRate,
                    "Average Rating" to overview.averageRating,
                    "Total Reviews" to overview.totalReviews
                )

                statItems.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { (title, value) ->
                            OverviewStatCard(
                                title = title,
                                value = when {
                                    title == "Average Rating" -> String.format(
                                        "%.1f",
                                        (value as? Number)?.toDouble() ?: 0.0
                                    )

                                    title == "Approval Rate" -> "${(value as? Number)?.toInt() ?: 0}%"
                                    else -> value.toString()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5EDE8)
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
                textAlign = TextAlign.Center
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
    performance: AuthorPerformanceResponse,
    navController: NavController?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Performance",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

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

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        performance.topPerformingBooks.forEach { book ->
                            TopPerformingBookItem(book = book)
                        }
                    }
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
fun TopPerformingBookItem(book: TopPerformingBookResponse) {
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
    trends: AuthorTrendsResponse,
    analyticsViewModel: AnalyticsViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Trends (Last 6 Months)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            TrendChart(
                title = "Monthly Applications",
                data = trends.monthlyApplications,
                color = MaterialTheme.colorScheme.primary,
                analyticsViewModel = analyticsViewModel
            )

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
    data: List<MonthlyDataResponse>,
    color: Color,
    analyticsViewModel: AnalyticsViewModel,
    isPercentage: Boolean = false
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
            val maxCount = data.maxOfOrNull { it.numericValue } ?: 1

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.take(6).forEach { monthData ->
                    val value = monthData.numericValue
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(if (maxCount > 0) (value.toFloat() / maxCount * 80).dp else 0.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )

                        Text(
                            text = analyticsViewModel.formatMonth(monthData.month),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (isPercentage) "$value%" else value.toString(),
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

@Composable
fun ReaderAnalyticsSection(readerAnalytics: ReaderAnalyticsResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reader Analytics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReaderMetric(
                    label = "Total Readers",
                    value = readerAnalytics.totalUniqueReaders.toString()
                )
                ReaderMetric(
                    label = "New This Month",
                    value = readerAnalytics.newReadersThisMonth.toString()
                )
                ReaderMetric(
                    label = "Repeat Readers",
                    value = readerAnalytics.repeatReaders.toString()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReaderMetric(
                    label = "Engagement Rate",
                    value = "${readerAnalytics.engagementRate}%"
                )
                ReaderMetric(
                    label = "With Reviews",
                    value = readerAnalytics.readersWithReviews.toString()
                )
            }

            readerAnalytics.demographics?.let { demographics ->
                Divider()

                Text(
                    text = "Reader Demographics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )


                demographics.age?.let { ageDemographics ->
                    AgeDemographicsSection(ageDemographics = ageDemographics)
                }

                demographics.countries?.let { countryDemographics ->
                    CountryDemographicsSection(countryDemographics = countryDemographics)
                }

                demographics.genrePreferences?.let { genrePreferences ->
                    GenrePreferencesSection(
                        title = "Reader Genre Preferences",
                        genreDemographics = genrePreferences
                    )
                }

                demographics.appliedBookGenres?.let { appliedGenres ->
                    GenrePreferencesSection(
                        title = "Genres of Applied Books",
                        genreDemographics = appliedGenres
                    )
                }
            }
        }
    }
}

@Composable
fun ReaderMetric(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun AgeDemographicsSection(ageDemographics: AgeDemographicsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5EDE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Age Demographics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Average Age",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${ageDemographics.averageAge ?: "N/A"}${if (ageDemographics.averageAge != null) " years" else ""}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${ageDemographics.totalWithAge ?: 0} readers",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (ageDemographics.ageRanges.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ageDemographics.ageRanges.forEach { ageRange ->
                        AgeRangeBar(
                            range = ageRange.range,
                            count = ageRange.count,
                            percentage = ageRange.percentage,
                            maxCount = ageDemographics.ageRanges.maxOfOrNull { it.count } ?: 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgeRangeBar(
    range: String,
    count: Int,
    percentage: Int,
    maxCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = range,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count ($percentage%)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (maxCount > 0) count.toFloat() / maxCount else 0f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun CountryDemographicsSection(countryDemographics: CountryDemographicsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5EDE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Country Distribution",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${countryDemographics.totalWithCountry} readers",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (countryDemographics.countries.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    countryDemographics.countries.take(10).forEach { country ->
                        CountryItem(
                            country = country.country,
                            count = country.count,
                            percentage = country.percentage
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountryItem(
    country: String,
    count: Int,
    percentage: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$count ($percentage%)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GenrePreferencesSection(
    title: String,
    genreDemographics: GenreDemographicsResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5EDE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            genreDemographics.totalWithPreferences?.let { total ->
                Text(
                    text = "$total readers",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (genreDemographics.genres.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    genreDemographics.genres.take(10).forEach { genre ->
                        GenreItem(
                            genre = genre.genre,
                            count = genre.count,
                            percentage = genre.percentage
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenreItem(
    genre: String,
    count: Int,
    percentage: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = genre,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$count ($percentage%)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BookPerformanceComparisonSection(
    books: List<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse>,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Book Performance Comparison",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (books.isEmpty()) {
                Text(
                    text = "No books to compare",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val sortedBooks = books.sortedWith(
                    compareByDescending<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse> { it.reviews.averageRating }
                        .thenByDescending { it.applications.totalApplications }
                )

                val bestPerformer = sortedBooks.firstOrNull()
                val worstPerformer = sortedBooks.lastOrNull()

                if (bestPerformer != null && worstPerformer != null && bestPerformer != worstPerformer) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PerformanceHighlightCard(
                            title = "Best Performer",
                            book = bestPerformer,
                            isBest = true,
                            onClick = {
                                navController.navigate(
                                    com.example.booknest.navigation.Screen.BookDetails.createRoute(
                                        bestPerformer.bookId
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PerformanceHighlightCard(
                            title = "Needs Improvement",
                            book = worstPerformer,
                            isBest = false,
                            onClick = {
                                navController.navigate(
                                    com.example.booknest.navigation.Screen.BookDetails.createRoute(
                                        worstPerformer.bookId
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "All Books",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                sortedBooks.forEach { book ->
                    BookPerformanceRow(
                        book = book,
                        onClick = {
                            navController.navigate(
                                com.example.booknest.navigation.Screen.BookDetails.createRoute(
                                    book.bookId
                                )
                            )
                        }
                    )
                    if (book != sortedBooks.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceHighlightCard(
    title: String,
    book: BookPerformanceComparisonResponse,
    isBest: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isBest)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isBest)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = book.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "Rating",
                        fontSize = 10.sp,
                        color = if (isBest)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = String.format("%.1f", book.reviews.averageRating),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "Apps",
                        fontSize = 10.sp,
                        color = if (isBest)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = book.applications.totalApplications.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BookPerformanceRow(
    book: com.example.booknest.domain.model.response.BookPerformanceComparisonResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.coverImageUrl,
                contentDescription = book.title,
                modifier = Modifier.size(48.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PerformanceMetricSmall(
                        label = "Rating",
                        value = String.format("%.1f", book.reviews.averageRating),
                        icon = Icons.Default.Star
                    )
                    PerformanceMetricSmall(
                        label = "Reviews",
                        value = book.reviews.reviewCount.toString(),
                        icon = Icons.Default.Comment
                    )
                    PerformanceMetricSmall(
                        label = "Applications",
                        value = book.applications.totalApplications.toString(),
                        icon = Icons.Default.Menu
                    )
                    PerformanceMetricSmall(
                        label = "Approval",
                        value = "${book.approvalRate}%",
                        icon = Icons.Default.CheckCircle
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceMetricSmall(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
