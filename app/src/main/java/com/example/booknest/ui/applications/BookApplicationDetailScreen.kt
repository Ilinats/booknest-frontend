package com.example.booknest.ui.applications

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.Screen
import com.example.booknest.domain.model.response.*
import com.example.booknest.ui.analytics.AgeDemographicsSection
import com.example.booknest.ui.analytics.AgeRangeBar
import com.example.booknest.ui.analytics.CountryDemographicsSection
import com.example.booknest.ui.analytics.CountryItem
import com.example.booknest.ui.analytics.GenreItem
import com.example.booknest.ui.analytics.GenrePreferencesSection
import com.example.booknest.ui.analytics.RatingBreakdownChart
import com.example.booknest.ui.analytics.RecentReviewItem
import com.example.booknest.ui.author.StatCard
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.BookViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.viewmodel.AnalyticsViewModel
import com.example.booknest.viewmodel.BookAnalyticsUiState
import com.example.booknest.viewmodel.ReviewViewModel
import com.example.booknest.viewmodel.ProfileViewModel
import com.example.booknest.viewmodel.ProfileUiState
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

data class ApplicationStats(
    val total: Int,
    val pending: Int,
    val approved: Int,
    val rejected: Int,
    val withdrawn: Int
)

enum class SortOption(val displayName: String) {
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
    RATING_DESC("Rating (High)"),
    READING_STATUS("Reading Status")
}

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun getDeadlineStatus(deadline: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(deadline) ?: SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).parse(deadline)
        date?.let {
            val now = Date()
            val daysUntil = ((it.time - now.time) / (1000 * 60 * 60 * 24)).toInt()
            when {
                daysUntil < 0 -> "Overdue (${-daysUntil} days ago)"
                daysUntil == 0 -> "Today"
                daysUntil == 1 -> "Tomorrow"
                else -> "$daysUntil days remaining"
            }
        } ?: deadline
    } catch (e: Exception) {
        deadline
    }
}

@Composable
fun BookSummaryHeader(
    book: BookResponse?,
    approvedCount: Int,
    totalSlots: Int
) {
    if (book == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = book.coverImageUrl,
                contentDescription = "Book Cover",
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Status: ${
                            book.status?.lowercase()
                                ?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Slots: $approvedCount / $totalSlots",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                book.applicationDeadline?.let { deadline ->
                    val deadlineStatus = getDeadlineStatus(deadline)
                    val formattedDate = formatDate(deadline)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Application Deadline: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = deadlineStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (deadlineStatus.contains(
                                            "Overdue",
                                            ignoreCase = true
                                        )
                                    )
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                book.reviewDeadline?.let { deadline ->
                    val deadlineStatus = getDeadlineStatus(deadline)
                    val formattedDate = formatDate(deadline)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Review Deadline: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = deadlineStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (deadlineStatus.contains(
                                            "Overdue",
                                            ignoreCase = true
                                        )
                                    )
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationStatItem(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ApplicationStatsSection(stats: ApplicationStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ApplicationStatCard(
                title = "Total",
                value = stats.total.toString(),
                modifier = Modifier.weight(1f)
            )
            ApplicationStatCard(
                title = "Pending",
                value = stats.pending.toString(),
                modifier = Modifier.weight(1f)
            )
            ApplicationStatCard(
                title = "Approved",
                value = stats.approved.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ApplicationStatCard(
                title = "Rejected",
                value = stats.rejected.toString(),
                modifier = Modifier.weight(1f)
            )
            ApplicationStatCard(
                title = "Withdrawn",
                value = stats.withdrawn.toString(),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ApplicationStatCard(
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
fun BulkActionsBar(
    selectedCount: Int,
    availableSlots: Int,
    onApproveSelected: () -> Unit,
    onRejectSelected: () -> Unit,
    onMarkSentSelected: () -> Unit,
    onCancelSelection: () -> Unit,
    showMarkSent: Boolean,
    canApprove: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "$selectedCount ${if (selectedCount == 1) "application" else "applications"} selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!canApprove) {
                            Text(
                                text = "Only $availableSlots ${if (availableSlots == 1) "slot" else "slots"} available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                TextButton(onClick = onCancelSelection) {
                    Text("Cancel")
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showMarkSent) {
                    OutlinedButton(
                        onClick = onMarkSentSelected,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Sent")
                    }
                }
                Button(
                    onClick = onApproveSelected,
                    modifier = Modifier.weight(1f),
                    enabled = canApprove,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
                OutlinedButton(
                    onClick = onRejectSelected,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
            }
        }
    }
}

@Composable
fun SortFilterBar(
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    onToggleSelectionMode: () -> Unit,
    isSelectionMode: Boolean,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            OutlinedButton(onClick = { onShowSortMenuChange(true) }) {
                Icon(Icons.Default.Sort, contentDescription = "Sort")
                Spacer(modifier = Modifier.width(4.dp))
                Text(sortOption.displayName)
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onShowSortMenuChange(false) }
            ) {
                SortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName) },
                        onClick = {
                            onSortOptionChange(option)
                            onShowSortMenuChange(false)
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = onToggleSelectionMode,
            enabled = enabled
        ) {
            Icon(
                if (isSelectionMode) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                contentDescription = "Select",
                tint = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    isSelectionMode -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun RatingDistributionChartFromObject(ratingDistribution: RatingDistributionResponse) {
    val ratings = listOf(5, 4, 3, 2, 1)
    val counts = listOf(
        ratingDistribution.`5`,
        ratingDistribution.`4`,
        ratingDistribution.`3`,
        ratingDistribution.`2`,
        ratingDistribution.`1`
    )
    val maxCount = counts.maxOrNull() ?: 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        ratings.forEachIndexed { index, rating ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                val barHeight = if (maxCount > 0) {
                    (counts[index].toFloat() / maxCount * 120).dp.coerceAtLeast(4.dp)
                } else {
                    4.dp
                }
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(barHeight)
                        .background(
                            when (rating) {
                                5 -> Color(0xFF4CAF50)
                                4 -> Color(0xFF8BC34A)
                                3 -> Color(0xFFFFC107)
                                2 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            },
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                        text = rating.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = counts[index].toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ReaderStatsRow(
    readerId: String?,
    navController: NavController,
    profileViewModel: ProfileViewModel = getViewModel()
) {
    var userProfile by remember { mutableStateOf<UserProfileResponse?>(null) }
    var isLoadingStats by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(readerId) {
        if (readerId != null) {
            isLoadingStats = true
            hasError = false
            try {
                profileViewModel.loadPublicUserProfile(readerId)
            } catch (e: Exception) {
                hasError = true
            } finally {
                isLoadingStats = false
            }
        }
    }

    val publicProfile by profileViewModel.publicProfile.collectAsState()
    val profileError by profileViewModel.error.collectAsState()

    LaunchedEffect(publicProfile, profileError) {
        if (profileError != null) {
            hasError = true
        } else {
            publicProfile?.let { profile ->
                userProfile = profile.toFullProfile()
            }
        }
    }

    val stats = userProfile?.stats
    val genresBreakdown = stats?.genresBreakdown

    LaunchedEffect(Unit) {
    }

    if (isLoadingStats || hasError || stats == null) {
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            stats.totalReviews?.let { reviews ->
                Text(
                    text = "Reviews: $reviews",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } ?: Text(
                text = "Reviews: N/A",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            stats.averageRating?.let { rating ->
                Text(
                    text = "Avg rating: ${String.format("%.1f", rating)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } ?: Text(
                text = "Avg rating: N/A",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        genresBreakdown?.let { breakdown ->
            if (breakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Favorite genres:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val topGenres = breakdown.entries.sortedByDescending { it.value }.take(5)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        topGenres.chunked(3).forEach { rowGenres ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rowGenres.forEach { entry ->
                                    GenreTag(text = entry.key)
                                }
                            }
                        }
                    }
                }
            }
        } ?: run {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Favorite genres:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "N/A",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatisticsTabContent(
    bookId: String,
    sessionManager: SessionManager,
    analyticsViewModel: AnalyticsViewModel = getViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        StatisticsTab(
            bookId = bookId,
            sessionManager = sessionManager,
            analyticsViewModel = analyticsViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookApplicationDetailScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    bookId: String,
    applicationViewModel: ApplicationViewModel = getViewModel(),
    bookViewModel: BookViewModel = getViewModel(),
    reviewViewModel: ReviewViewModel = getViewModel()
) {
    val bookApplications by applicationViewModel.bookApplications.collectAsState()
    val isLoading by applicationViewModel.isLoading.collectAsState()
    val bookDetails by bookViewModel.bookDetails.collectAsState()
    val bookReviews by reviewViewModel.bookReviews.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Pending", "Approved", "Rejected", "Reviews", "Statistics")

    val book = bookDetails ?: bookApplications.firstOrNull()?.book

    val isLotteryBook = book?.selectionMethod?.let { method ->
        method.lowercase().trim() == "lottery" || method.lowercase().trim() == "random_selection"
    } ?: false

    val lotteryDeadlinePassed = book?.applicationDeadline?.let { deadline ->
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val deadlineDate = inputFormat.parse(deadline)
            deadlineDate?.before(Date()) ?: false
        } catch (e: Exception) {
            false
        }
    } ?: false
    val lotteryHasPending = bookApplications.any { it.status == "pending" }
    val lotteryHasProcessed = bookApplications.any { it.status in listOf("approved", "rejected") }
    var showLotteryDialog by remember { mutableStateOf(false) }

    var selectedApplicationIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isSelectionMode by remember { mutableStateOf(false) }

    var sortOption by remember { mutableStateOf<SortOption>(SortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(bookId) {
        bookViewModel.getBookDetails(bookId)
        applicationViewModel.loadBookApplications(bookId)
        reviewViewModel.loadBookReviews(bookId)
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab != 1 && isSelectionMode) {
            isSelectionMode = false
            selectedApplicationIds = emptySet()
        }
    }

    val applicationsWithReviews = remember(bookApplications, bookReviews) {
        bookApplications.map { application ->
            val review = bookReviews.find { it.applicationId == application.id }
            application.copy(review = review)
        }
    }

    val applicationStats = remember(applicationsWithReviews) {
        val total = applicationsWithReviews.size
        val pending = applicationsWithReviews.count { it.status == "pending" }
        val approved = applicationsWithReviews.count { it.status == "approved" }
        val rejected = applicationsWithReviews.count { it.status == "rejected" }
        val withdrawn = applicationsWithReviews.count { it.status == "withdrawn" }
        ApplicationStats(
            total = total,
            pending = pending,
            approved = approved,
            rejected = rejected,
            withdrawn = withdrawn
        )
    }

    val filteredApplications: List<ApplicationResponse> =
        remember(selectedTab, applicationsWithReviews, sortOption) {
            val filtered = when (selectedTab) {
                0 -> applicationsWithReviews
                1 -> applicationsWithReviews.filter { it.status == "pending" }
                2 -> applicationsWithReviews.filter { it.status == "approved" }
                3 -> applicationsWithReviews.filter { it.status == "rejected" }
                else -> applicationsWithReviews
            }

            when (sortOption) {
                SortOption.DATE_DESC -> filtered.sortedByDescending {
                    try {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(
                            it.appliedAt
                        )?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }

                SortOption.DATE_ASC -> filtered.sortedBy {
                    try {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(
                            it.appliedAt
                        )?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }

                SortOption.RATING_DESC -> filtered.sortedByDescending {
                    try {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(
                            it.appliedAt
                        )?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }

                SortOption.READING_STATUS -> filtered.sortedBy { it.readingStatus ?: "" }
            }
        }

    val approvedCount = applicationStats.approved

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                book?.title ?: "Book Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Status: ${
                                    book?.status?.lowercase()
                                        ?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        BackButton(onClick = { navController.popBackStack() })
                    },
                    actions = {
                        IconButton(onClick = {
                            book?.id?.let {
                                navController.navigate(Screen.BookEdit.createRoute(it))
                            }
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Book")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = {
                            bookId.let {
                                navController.navigate("book_analytics/$it")
                            }
                        }) {
                            Icon(Icons.Filled.Info, contentDescription = "Analytics")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                BookSummaryHeader(
                    book = book,
                    approvedCount = approvedCount,
                    totalSlots = book?.totalCopies ?: 0
                )
            }

            item {
                ApplicationStatsSection(stats = applicationStats)
            }

            if (isLotteryBook) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    LotterySelectionCard(
                        isLotteryBook = isLotteryBook,
                        deadlinePassed = lotteryDeadlinePassed,
                        hasPendingApplications = lotteryHasPending,
                        hasProcessedApplications = lotteryHasProcessed,
                        pendingCount = applicationStats.pending,
                        availableCopies = book?.availableCopies ?: 0,
                        onRunLottery = { showLotteryDialog = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 0.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                selectedApplicationIds = emptySet()
                                if (index != 1) {
                                    isSelectionMode = false
                                }
                            },
                            text = {
                                Text(
                                    title + when (index) {
                                        0 -> " (${applicationStats.total})"
                                        1 -> " (${applicationStats.pending})"
                                        2 -> " (${applicationStats.approved})"
                                        3 -> " (${applicationStats.rejected})"
                                        4 -> " (${applicationsWithReviews.count { it.review != null }})"
                                        else -> ""
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            if (isSelectionMode && selectedApplicationIds.isNotEmpty() && selectedTab == 1) {
                item {
                    val totalSlots = book?.totalCopies ?: 0
                    val availableSlots = totalSlots - approvedCount
                    val canApprove = selectedApplicationIds.size <= availableSlots

                    BulkActionsBar(
                        selectedCount = selectedApplicationIds.size,
                        availableSlots = availableSlots,
                        onApproveSelected = {
                            applicationViewModel.bulkActionApplications(
                                selectedApplicationIds.toList(),
                                "approved"
                            )
                            selectedApplicationIds = emptySet()
                            isSelectionMode = false
                        },
                        onRejectSelected = {
                            applicationViewModel.bulkActionApplications(
                                selectedApplicationIds.toList(),
                                "rejected"
                            )
                            selectedApplicationIds = emptySet()
                            isSelectionMode = false
                        },
                        onMarkSentSelected = {
                            selectedApplicationIds.forEach { id ->
                                applicationViewModel.markCopySent(id)
                            }
                            selectedApplicationIds = emptySet()
                            isSelectionMode = false
                        },
                        onCancelSelection = {
                            selectedApplicationIds = emptySet()
                            isSelectionMode = false
                        },
                        showMarkSent = selectedTab == 2 && selectedApplicationIds.any { appId ->
                            val app = filteredApplications.find { it.id == appId }
                            val isCopySent = !app?.copySentAt.isNullOrBlank()
                            val isDigital = app?.book?.distributionType?.lowercase() == "digital"
                            !isCopySent && !isDigital
                        },
                        canApprove = canApprove
                    )
                }
            }

            if (selectedTab != 4 && selectedTab != 5) {
                item {
                    SortFilterBar(
                        sortOption = sortOption,
                        onSortOptionChange = { option: SortOption -> sortOption = option },
                        showSortMenu = showSortMenu,
                        onShowSortMenuChange = { show: Boolean -> showSortMenu = show },
                        onToggleSelectionMode = {
                            if (selectedTab == 1) {
                                isSelectionMode = !isSelectionMode
                                if (!isSelectionMode) selectedApplicationIds = emptySet()
                            }
                        },
                        isSelectionMode = isSelectionMode,
                        enabled = selectedTab == 1
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    if (isLoading && filteredApplications.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        items(
                            filteredApplications.size,
                            key = { filteredApplications[it].id }) { index ->
                            val application = filteredApplications[index]
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                val appId = application.id
                                EnhancedApplicationCard(
                                    application = application,
                                    isSelected = selectedApplicationIds.contains(appId),
                                    isSelectionMode = isSelectionMode,
                                    onToggleSelection = {
                                        selectedApplicationIds =
                                            if (selectedApplicationIds.contains(appId)) {
                                                selectedApplicationIds - appId
                                            } else {
                                                selectedApplicationIds + appId
                                            }
                                    },
                                    navController = navController,
                                    onApprove = { app, notes ->
                                        applicationViewModel.approveApplication(
                                            app.id,
                                            notes
                                        )
                                    },
                                    onReject = { app, notes ->
                                        applicationViewModel.rejectApplication(
                                            app.id,
                                            notes
                                        )
                                    },
                                    onMarkSent = null,
                                    isLotteryBook = isLotteryBook
                                )
                            }
                        }
                    }
                }

                1 -> {
                    val totalSlots = book?.totalCopies ?: 0
                    val availableSlots = totalSlots - approvedCount

                    if (isLoading && filteredApplications.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        items(
                            filteredApplications.size,
                            key = { filteredApplications[it].id }) { index ->
                            val application = filteredApplications[index]
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                val appId = application.id
                                val canSelectMore = selectedApplicationIds.size < availableSlots

                                EnhancedApplicationCard(
                                    application = application,
                                    isSelected = selectedApplicationIds.contains(appId),
                                    isSelectionMode = isSelectionMode,
                                    onToggleSelection = {
                                        if (selectedApplicationIds.contains(appId)) {
                                            selectedApplicationIds = selectedApplicationIds - appId
                                        } else {
                                            if (canSelectMore) {
                                                selectedApplicationIds =
                                                    selectedApplicationIds + appId
                                            }
                                        }
                                    },
                                    navController = navController,
                                    onApprove = { app, notes ->
                                        applicationViewModel.approveApplication(
                                            app.id,
                                            notes
                                        )
                                    },
                                    onReject = { app, notes ->
                                        applicationViewModel.rejectApplication(
                                            app.id,
                                            notes
                                        )
                                    },
                                    onMarkSent = null,
                                    isLotteryBook = isLotteryBook
                                )
                            }
                        }
                    }
                }

                2 -> {
                    if (isLoading && filteredApplications.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        items(
                            filteredApplications.size,
                            key = { filteredApplications[it].id }) { index ->
                            val application = filteredApplications[index]
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                val appId = application.id
                                EnhancedApprovedApplicationCard(
                                    application = application,
                                    isSelected = selectedApplicationIds.contains(appId),
                                    isSelectionMode = isSelectionMode,
                                    onToggleSelection = {
                                        selectedApplicationIds =
                                            if (selectedApplicationIds.contains(appId)) {
                                                selectedApplicationIds - appId
                                            } else {
                                                selectedApplicationIds + appId
                                            }
                                    },
                                    navController = navController,
                                    onMarkSent = { app -> applicationViewModel.markCopySent(app.id) }
                                )
                            }
                        }
                    }
                }

                3 -> {
                    if (isLoading && filteredApplications.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        items(
                            filteredApplications.size,
                            key = { filteredApplications[it].id }) { index ->
                            val application = filteredApplications[index]
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                RejectedApplicationCard(
                                    application = application,
                                    navController = navController
                                )
                            }
                        }
                    }
                }

                4 -> {
                    val reviewsApplications = applicationsWithReviews.filter { it.review != null }
                    item {
                        Text(
                            text = "Submitted Reviews",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        )
                    }
                    if (isLoading && reviewsApplications.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (reviewsApplications.isEmpty()) {
                        item {
                            Text(
                                text = "No reviews submitted yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    } else {
                        items(
                            reviewsApplications.size,
                            key = { reviewsApplications[it].id }
                        ) { index ->
                            ReviewCard(application = reviewsApplications[index])
                        }
                    }
                }

                5 -> {
                    item {
                        StatisticsTabContent(
                            bookId = bookId,
                            sessionManager = sessionManager
                        )
                    }
                }
            }
        }
    }

    if (showLotteryDialog) {
        AlertDialog(
            onDismissRequest = { showLotteryDialog = false },
            title = { Text("Run Lottery Selection") },
            text = {
                Text(
                    "This will randomly select ${book?.availableCopies ?: 0} reader(s) from ${applicationStats.pending} pending application(s). " +
                            "Selected readers will be approved and others will be rejected. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        applicationViewModel.runLottery(bookId)
                        showLotteryDialog = false
                    }
                ) {
                    Text("Run Lottery")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLotteryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ApplicantsTab(
    applications: List<ApplicationResponse>,
    isLoading: Boolean,
    book: BookResponse?,
    totalApplicationsCount: Int,
    navController: NavController,
    onApprove: (ApplicationResponse) -> Unit,
    onReject: (ApplicationResponse) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Applicants (${totalApplicationsCount}/${book?.totalCopies ?: 0} slots filled)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Text("Genre")
            }
            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Text("Date")
            }
            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Text("Location")
            }
            OutlinedButton(onClick = { }) {
                Text("Sort")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(applications) { application ->
                    ApplicantCard(
                        application = application,
                        navController = navController,
                        onApprove = { onApprove(application) },
                        onReject = { onReject(application) }
                    )
                }
            }
        }

        if (applications.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve All")
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject All")
                }
            }
        }
    }
}

@Composable
fun EnhancedApplicationCard(
    application: ApplicationResponse,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    navController: NavController,
    onApprove: (ApplicationResponse, String?) -> Unit,
    onReject: (ApplicationResponse, String?) -> Unit,
    onMarkSent: ((ApplicationResponse) -> Unit)?,
    isLotteryBook: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    var authorNotes by remember { mutableStateOf(application.authorNotes ?: "") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelectionMode) {
                    Modifier.clickable { onToggleSelection() }
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection() }
                        )
                    }

                    val onProfileClick: () -> Unit = {
                        application.reader?.id?.let { readerId ->
                            navController.navigate(Screen.Profile.createRoute(readerId))
                        }
                        Unit
                    }

                    if (application.reader?.profilePictureUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(onClick = onProfileClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = application.reader?.username?.firstOrNull()?.uppercase()
                                    ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        AsyncImage(
                            model = application.reader?.profilePictureUrl,
                            contentDescription = "Reader Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(onClick = onProfileClick),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = application.reader?.username ?: "Unknown Reader",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onProfileClick)
                        )
                        Text(
                            text = "Applied on ${formatDate(application.appliedAt ?: "")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            }

            application.applicationMessage?.let { message ->
                Column {
                    Text(
                        text = if (isExpanded) message else message.take(150) + if (message.length > 150) "..." else "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (message.length > 150) {
                        TextButton(onClick = { isExpanded = !isExpanded }) {
                            Text(if (isExpanded) "Show less" else "Show more")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            ReaderStatsRow(
                readerId = application.reader?.id,
                navController = navController
            )

            if (application.status == "pending" && !isSelectionMode) {
                if (isLotteryBook) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = "Lottery",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "This application will be decided by lottery selection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = authorNotes,
                        onValueChange = { authorNotes = it },
                        label = { Text("Add notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        placeholder = { Text("Add private notes about this application") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onApprove(application, authorNotes.ifBlank { null })
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Approve")
                        }
                        OutlinedButton(
                            onClick = { onReject(application, authorNotes.ifBlank { null }) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedApprovedApplicationCard(
    application: ApplicationResponse,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    navController: NavController,
    onMarkSent: (ApplicationResponse) -> Unit
) {
    val isCopySent = !application.copySentAt.isNullOrBlank()
    val distributionType = application.book?.distributionType?.lowercase()
    val isDigital = distributionType == "digital"
    val shouldShowMarkSentButton = !isCopySent && !isDigital
    val readingStatus = application.readingStatus
    val hasReview = !application.reviewSubmittedAt.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelectionMode) {
                    Modifier.clickable { onToggleSelection() }
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection() }
                        )
                    }

                    val onProfileClick: () -> Unit = {
                        application.reader?.id?.let { readerId ->
                            navController.navigate(Screen.Profile.createRoute(readerId))
                        }
                        Unit
                    }

                    if (application.reader?.profilePictureUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(onClick = onProfileClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = application.reader?.username?.firstOrNull()?.uppercase()
                                    ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        AsyncImage(
                            model = application.reader?.profilePictureUrl,
                            contentDescription = "Reader Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(onClick = onProfileClick),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = application.reader?.username ?: "Unknown Reader",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onProfileClick)
                        )
                        Text(
                            text = "Approved on ${formatDate(application.respondedAt ?: "")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Copy Status: ${if (isCopySent || isDigital) "Sent" else "Not Sent"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCopySent || isDigital) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (shouldShowMarkSentButton && !isSelectionMode) {
                    Button(onClick = { onMarkSent(application) }) {
                        Text("Mark as Sent")
                    }
                }
            }

            Text(
                text = "Reading Status: ${
                    readingStatus.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Review Status: ${if (hasReview) "Submitted" else "Pending"}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasReview) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            val requiresPhysicalCopy =
                application.book?.distributionType?.lowercase() in listOf("physical", "both")
            val shouldShowAddresses =
                requiresPhysicalCopy && application.reader?.addresses?.isNotEmpty() == true

            if (shouldShowAddresses) {
                Spacer(modifier = Modifier.height(8.dp))
                ReaderAddressesSection(
                    addresses = application.reader?.addresses ?: emptyList(),
                    readerName = application.reader?.username ?: "Reader"
                )
            }
        }
    }
}

@Composable
fun RejectedApplicationCard(
    application: ApplicationResponse,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val onProfileClick: () -> Unit = {
                        application.reader?.id?.let { readerId ->
                            navController.navigate(Screen.Profile.createRoute(readerId))
                        }
                        Unit
                    }

                    if (application.reader?.profilePictureUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(onClick = onProfileClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = application.reader?.username?.firstOrNull()?.uppercase()
                                    ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        AsyncImage(
                            model = application.reader?.profilePictureUrl,
                            contentDescription = "Reader Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(onClick = onProfileClick),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = application.reader?.username ?: "Unknown Reader",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onProfileClick)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Rejected",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Rejected on ${formatDate(application.respondedAt ?: "")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            application.authorNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Author Notes",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicantCard(
    application: ApplicationResponse,
    navController: NavController,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = application.reader?.username?.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = application.reader?.username ?: "Unknown Reader",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Applied on ${formatDate(application.appliedAt ?: "")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Approve")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GenreTag("General")
            }

            Spacer(modifier = Modifier.height(12.dp))

            application.applicationMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Previous Reviews: N/A",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onReject) {
                    Text("Reject")
                }
                TextButton(
                    onClick = {
                        application.reader?.id?.let { readerId ->
                            navController.navigate(Screen.Profile.createRoute(readerId))
                        }
                    }
                ) {
                    Text("View Profile")
                }
            }
        }
    }
}

@Composable
fun GenreTag(text: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ApprovedTab(
    applications: List<ApplicationResponse>,
    isLoading: Boolean,
    book: BookResponse?,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Approved Readers (${applications.size}/${book?.totalCopies ?: 0} slots filled)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(applications) { application ->
                    ApprovedReaderCard(
                        application = application,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovedReaderCard(
    application: ApplicationResponse,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = application.reader?.username?.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = application.reader?.username ?: "Unknown Reader",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Approved on ${formatDate(application.respondedAt ?: "")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        application.reader?.id?.let { readerId ->
                            navController.navigate(Screen.Profile.createRoute(readerId))
                        }
                    }
                ) {
                    Text("View Profile")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GenreTag("General")
            }
        }
    }
}

@Composable
fun ReviewCard(application: ApplicationResponse) {
    var showReviewDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = application.reader?.username?.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = application.reader?.username ?: "Unknown Reader",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Submitted on ${formatDate(application.reviewSubmittedAt ?: "")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showReviewDialog = true }
                ) {
                    Text("View Review")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${application.reader?.username}'s review of '${application.book?.title ?: "this book"}': ${application.review?.reviewContent ?: "Review content not available"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showReviewDialog) {
        ReviewDetailDialog(
            review = application.review,
            reader = application.reader,
            book = application.book,
            onDismiss = { showReviewDialog = false }
        )
    }
}

@Composable
fun ReviewDetailDialog(
    review: ReviewResponse?,
    reader: ApplicationReaderResponse?,
    book: BookResponse?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Review Details")
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reader:",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = reader?.username ?: "Unknown")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Book:",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = book?.title ?: "Unknown")
                }

                review?.let { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rating:",
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(r.rating) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFFFC107)
                                )
                            }
                            Text("(${r.rating}/5)")
                        }
                    }

                    Divider()

                    if (!r.reviewContent.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Review:",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = r.reviewContent)
                    }

                    r.reviewUrls?.let { urls ->
                        if (urls.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Review Links:",
                                fontWeight = FontWeight.Bold
                            )
                            urls.forEach { url ->
                                Text(
                                    text = url,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    r.wordCount?.let { wordCount ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Word Count:",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "$wordCount words")
                        }
                    }
                } ?: run {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚠️ Review Data Not Available",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "The review data is not being returned by the backend API. The backend needs to include the 'review' relation when fetching applications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontSize = MaterialTheme.typography.titleSmall.fontSize)
            }
        }
    )
}

@Composable
fun StatisticsTab(
    bookId: String,
    sessionManager: SessionManager,
    analyticsViewModel: AnalyticsViewModel = getViewModel()
) {
    val analyticsState by analyticsViewModel.bookAnalyticsState.collectAsState()

    LaunchedEffect(bookId) {
        analyticsViewModel.loadDetailedBookAnalytics(bookId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (val state = analyticsState) {
            is BookAnalyticsUiState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is BookAnalyticsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is BookAnalyticsUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Statistics not available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Book analytics data is not available at this time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = {
                        analyticsViewModel.loadDetailedBookAnalytics(bookId)
                    }) {
                        Text("Retry")
                    }
                }
            }

            is BookAnalyticsUiState.Success -> {
                val analytics = state.analytics
                val reviewStats = analytics.reviewStatistics ?: analytics.reviewAnalytics
                val appStats = analytics.applicationStatistics ?: analytics.applicationAnalytics

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    reviewStats?.let { stats ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Review Statistics",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    title = "Total Reviews",
                                    value = stats.totalReviews.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Average Rating",
                                    value = try {
                                        String.format(
                                            "%.1f",
                                            stats.averageRating.toDoubleOrNull() ?: 0.0
                                        )
                                    } catch (e: Exception) {
                                        stats.averageRating
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            StatCard(
                                title = "Positive Feedback",
                                value = "${stats.positiveFeedback}%",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.BarChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Review Ratings Distribution",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.3f
                                    )
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (stats.ratingBreakdown != null && stats.ratingBreakdown.isNotEmpty()) {
                                        RatingBreakdownChart(ratingBreakdown = stats.ratingBreakdown)
                                    } else {
                                        RatingDistributionChartFromObject(stats.ratingDistribution)
                                    }
                                }
                            }
                        }
                    }

                    appStats?.let { stats ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Application Statistics",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatCard(
                                            title = "Total",
                                            value = stats.totalApplications.toString(),
                                            modifier = Modifier.weight(1f),
                                            icon = Icons.Default.Apps,
                                            iconTint = MaterialTheme.colorScheme.primary
                                        )
                                        StatCard(
                                            title = "Approved",
                                            value = stats.approvedApplications.toString(),
                                            modifier = Modifier.weight(1f),
                                            icon = Icons.Default.CheckCircle,
                                            iconTint = Color(0xFF4CAF50)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatCard(
                                            title = "Pending",
                                            value = stats.pendingApplications.toString(),
                                            modifier = Modifier.weight(1f),
                                            icon = Icons.Default.Schedule,
                                            iconTint = Color(0xFFFF9800)
                                        )
                                        StatCard(
                                            title = "Rejected",
                                            value = stats.rejectedApplications.toString(),
                                            modifier = Modifier.weight(1f),
                                            icon = Icons.Default.Cancel,
                                            iconTint = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    if (stats.applicationsThisMonth != null || stats.approvedApplicationsThisMonth != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Divider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.2f
                                            )
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "This Month",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            stats.applicationsThisMonth?.let { count: Int ->
                                                StatCard(
                                                    title = "Applications",
                                                    value = count.toString(),
                                                    modifier = Modifier.weight(1f),
                                                    icon = Icons.Default.Apps,
                                                    iconTint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            stats.approvedApplicationsThisMonth?.let { count: Int ->
                                                StatCard(
                                                    title = "Approved",
                                                    value = count.toString(),
                                                    modifier = Modifier.weight(1f),
                                                    icon = Icons.Default.CheckCircle,
                                                    iconTint = Color(0xFF4CAF50)
                                                )
                                            }
                                            stats.rejectedApplicationsThisMonth?.let { count: Int ->
                                                StatCard(
                                                    title = "Rejected",
                                                    value = count.toString(),
                                                    modifier = Modifier.weight(1f),
                                                    icon = Icons.Default.Cancel,
                                                    iconTint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Divider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.2f
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatCard(
                                            title = "Approval Rate",
                                            value = "${stats.approvalRate}%",
                                            modifier = Modifier.weight(1f),
                                            icon = Icons.Default.TrendingUp,
                                            iconTint = Color(0xFF4CAF50)
                                        )
                                        StatCard(
                                            title = "Rejection Rate",
                                            value = "${stats.rejectionRate}%",
                                            modifier = Modifier.weight(1f),
                                            icon = Icons.Default.TrendingDown,
                                            iconTint = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    stats.averageResponseTime?.let { time ->
                                        Spacer(modifier = Modifier.height(12.dp))
                                        StatCard(
                                            title = "Avg Response Time",
                                            value = if (time < 24) "$time hours" else "${time / 24} days",
                                            modifier = Modifier.fillMaxWidth(),
                                            icon = Icons.Default.AccessTime,
                                            iconTint = Color(0xFF2196F3)
                                        )
                                    }

                                    stats.applicationConversionRate?.let { rate ->
                                        Spacer(modifier = Modifier.height(12.dp))
                                        StatCard(
                                            title = "Conversion Rate",
                                            value = "$rate%",
                                            modifier = Modifier.fillMaxWidth(),
                                            icon = Icons.Default.Transform,
                                            iconTint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        analytics.reviewPerformance?.let { perf ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Review Performance",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            StatCard(
                                                title = "Submission Rate",
                                                value = "${perf.reviewSubmissionRate}%",
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.Default.Upload,
                                                iconTint = Color(0xFF4CAF50)
                                            )
                                            StatCard(
                                                title = "Completion Rate",
                                                value = "${perf.reviewCompletionRate}%",
                                                modifier = Modifier.weight(1f),
                                                icon = Icons.Default.CheckCircle,
                                                iconTint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        perf.averageReviewTime?.let { time ->
                                            Spacer(modifier = Modifier.height(12.dp))
                                            StatCard(
                                                title = "Avg Review Time",
                                                value = String.format("%.1f days", time),
                                                modifier = Modifier.fillMaxWidth(),
                                                icon = Icons.Default.AccessTime,
                                                iconTint = Color(0xFF2196F3)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (analytics.recentReviews.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Recent Reviews",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        analytics.recentReviews.take(5).forEach { review ->
                                            RecentReviewItem(review = review)
                                        }
                                    }
                                }
                            }
                        }

                        analytics.readerDemographics?.let { demographics ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Reader Insights",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    demographics.age?.let { ageDemo ->
                                        AgeDemographicsSection(ageDemographics = ageDemo)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    demographics.countries?.let { countryDemo ->
                                        CountryDemographicsSection(countryDemographics = countryDemo)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    demographics.appliedBookGenres?.let { appliedGenres ->
                                        GenrePreferencesSection(
                                            title = "Genres of Applied Books",
                                            genreDemographics = appliedGenres
                                        )
                                    } ?: demographics.genrePreferences?.let { genrePrefs ->
                                        GenrePreferencesSection(
                                            title = "Reader Genre Preferences",
                                            genreDemographics = genrePrefs
                                        )
                                    }
                                }
                            }
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
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reader = review.application.reader
                    val readerName = listOfNotNull(reader.firstName, reader.lastName)
                        .joinToString(" ")
                        .ifBlank { reader.username }

                    Text(
                        text = readerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = review.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = formatDate(review.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                review.reviewContent?.takeIf { it.isNotBlank() }?.let { content ->
                    Text(
                        text = if (content.length > 160) content.take(160) + "…" else content,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    @Composable
    fun RatingBreakdownChart(ratingBreakdown: List<RatingBreakdownItemResponse>) {
        val maxCount = ratingBreakdown.maxOfOrNull { it.count } ?: 1
        val sortedRatings = ratingBreakdown.sortedByDescending { it.rating }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            sortedRatings.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    val barHeight = if (maxCount > 0) {
                        (item.count.toFloat() / maxCount * 120).dp.coerceAtLeast(4.dp)
                    } else {
                        4.dp
                    }
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(barHeight)
                            .background(
                                when (item.rating) {
                                    5 -> Color(0xFF4CAF50)
                                    4 -> Color(0xFF8BC34A)
                                    3 -> Color(0xFFFFC107)
                                    2 -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                },
                                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                            text = item.rating.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.count.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
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
    fun RatingDistributionChart() {
        val ratings = listOf(1, 2, 3, 4, 5)
        val heights = listOf(0.1f, 0.2f, 0.3f, 0.8f, 0.6f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ratings.forEachIndexed { index, rating ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height((heights[index] * 100).dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rating.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }


    @Composable
    fun AllApplicationsTab(
        applications: List<ApplicationResponse>,
        isLoading: Boolean,
        selectedApplicationIds: Set<String>,
        isSelectionMode: Boolean,
        onToggleSelection: (String) -> Unit,
        navController: NavController,
        onApprove: (ApplicationResponse, String?) -> Unit,
        onReject: (ApplicationResponse, String?) -> Unit
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(applications) { application ->
                    EnhancedApplicationCard(
                        application = application,
                        isSelected = selectedApplicationIds.contains(application.id),
                        isSelectionMode = isSelectionMode,
                        onToggleSelection = { onToggleSelection(application.id) },
                        navController = navController,
                        onApprove = { app, notes -> onApprove(app, notes) },
                        onReject = { app, notes -> onReject(app, notes) },
                        onMarkSent = null
                    )
                }
            }
        }
    }

    @Composable
    fun PendingApplicationsTab(
        applications: List<ApplicationResponse>,
        isLoading: Boolean,
        selectedApplicationIds: Set<String>,
        isSelectionMode: Boolean,
        onToggleSelection: (String) -> Unit,
        navController: NavController,
        onApprove: (ApplicationResponse, String?) -> Unit,
        onReject: (ApplicationResponse, String?) -> Unit
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(applications) { application ->
                    EnhancedApplicationCard(
                        application = application,
                        isSelected = selectedApplicationIds.contains(application.id),
                        isSelectionMode = isSelectionMode,
                        onToggleSelection = { onToggleSelection(application.id) },
                        navController = navController,
                        onApprove = { app, notes -> onApprove(app, notes) },
                        onReject = { app, notes -> onReject(app, notes) },
                        onMarkSent = null
                    )
                }
            }
        }
    }

    @Composable
    fun ApprovedApplicationsTab(
        applications: List<ApplicationResponse>,
        isLoading: Boolean,
        selectedApplicationIds: Set<String>,
        isSelectionMode: Boolean,
        onToggleSelection: (String) -> Unit,
        navController: NavController,
        onMarkSent: (ApplicationResponse) -> Unit
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(applications) { application ->
                    EnhancedApprovedApplicationCard(
                        application = application,
                        isSelected = selectedApplicationIds.contains(application.id),
                        isSelectionMode = isSelectionMode,
                        onToggleSelection = { onToggleSelection(application.id) },
                        navController = navController,
                        onMarkSent = { onMarkSent(application) }
                    )
                }
            }
        }
    }

    @Composable
    fun RejectedApplicationsTab(
        applications: List<ApplicationResponse>,
        isLoading: Boolean,
        navController: NavController
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(applications) { application ->
                    RejectedApplicationCard(
                        application = application,
                        navController = navController
                    )
                }
            }
        }
    }


    @Composable
    fun AgeDemographicsSection(ageDemographics: com.example.booknest.domain.model.response.AgeDemographicsResponse) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Age Demographics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Average Age",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${ageDemographics.averageAge ?: "N/A"}${if (ageDemographics.averageAge != null) " years" else ""}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${ageDemographics.totalWithAge ?: 0} readers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (ageDemographics.ageRanges.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
    fun CountryDemographicsSection(countryDemographics: com.example.booknest.domain.model.response.CountryDemographicsResponse) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Country Distribution",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${countryDemographics.totalWithCountry} readers",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (countryDemographics.countries.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

    @Composable
    fun CountryItem(
        country: String,
        count: Int,
        percentage: Int
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = country,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count ($percentage%)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    @Composable
    fun GenrePreferencesSection(
        title: String,
        genreDemographics: GenreDemographicsResponse
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            genreDemographics.totalWithPreferences?.let { total ->
                Text(
                    text = "$total readers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (genreDemographics.genres.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

    @Composable
    fun GenreItem(
        genre: String,
        count: Int,
        percentage: Int
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
                text = "$count ($percentage%)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LotterySelectionCard(
    isLotteryBook: Boolean,
    deadlinePassed: Boolean,
    hasPendingApplications: Boolean,
    hasProcessedApplications: Boolean,
    pendingCount: Int,
    availableCopies: Int,
    onRunLottery: () -> Unit
) {
    val canRunLottery = deadlinePassed && hasPendingApplications && !hasProcessedApplications

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canRunLottery) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (canRunLottery) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Lottery",
                        tint = if (canRunLottery) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Lottery Selection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (canRunLottery) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        "Random selection process",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )

            when {
                hasProcessedApplications -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Lottery completed. Winners have been selected.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                !deadlinePassed -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = "Waiting",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Waiting for application deadline to pass",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.6f
                                )
                            )
                        ) {
                            Icon(
                                Icons.Filled.Shuffle,
                                contentDescription = "Run Lottery",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Run Lottery Selection",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                !hasPendingApplications -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "No pending applications available for lottery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "$pendingCount",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    "Pending",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$availableCopies",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    "Available Slots",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            "Ready to randomly select winners from pending applications.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onRunLottery,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                Icons.Filled.Shuffle,
                                contentDescription = "Run Lottery",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Lottery Selection", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

