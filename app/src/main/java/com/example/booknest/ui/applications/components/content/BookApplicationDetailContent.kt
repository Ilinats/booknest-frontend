package com.example.booknest.ui.applications.components.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.applications.components.detail.BookSummaryHeader
import com.example.booknest.ui.applications.components.detail.BulkActionsBar
import com.example.booknest.ui.applications.components.detail.OverdueReviewsCard
import com.example.booknest.ui.applications.components.detail.SortFilterBar
import com.example.booknest.ui.applications.components.list.ApplicationStatsSection
import com.example.booknest.ui.applications.components.list.AuthorApprovedTabInfoCard
import com.example.booknest.ui.applications.components.list.EnhancedApplicationCard
import com.example.booknest.ui.applications.components.list.EnhancedApprovedApplicationCard
import com.example.booknest.ui.applications.components.list.RejectedApplicationCard
import com.example.booknest.ui.applications.components.lottery.LotterySelectionCard
import com.example.booknest.ui.applications.components.review.ApplicationReaderReviewCard
import com.example.booknest.ui.applications.components.statistics.StatisticsTabContent
import com.example.booknest.ui.applications.dialogs.RunLotteryDialog
import com.example.booknest.ui.applications.models.ApplicationStats
import com.example.booknest.ui.applications.models.SortOption
import com.example.booknest.ui.components.AppScaffoldContentInsets
import com.example.booknest.ui.components.AppTopBar
import com.example.booknest.ui.components.BackButton
import com.example.booknest.ui.components.paddingTopFromScaffold
import com.example.booknest.navigation.rememberAuthorBookEditorViewModel
import com.example.booknest.ui.author.components.LeakFingerprintDecodeSection
import com.example.booknest.ui.author.components.books.formatBookStatus
import com.example.booknest.viewmodel.applications.BookApplicationViewModel
import com.example.booknest.viewmodel.books.BookDetailsViewModel
import com.example.booknest.viewmodel.author.AuthorBookEditorViewModel
import com.example.booknest.viewmodel.analytics.ReviewViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookApplicationDetailContent(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    bookId: String,
    bookApplicationViewModel: BookApplicationViewModel = getViewModel(),
    bookDetailsViewModel: BookDetailsViewModel = getViewModel(),
    reviewViewModel: ReviewViewModel = getViewModel(),
    authorBookEditorViewModel: AuthorBookEditorViewModel = rememberAuthorBookEditorViewModel(navController),
) {
    val context = LocalContext.current
    val bookApplications by bookApplicationViewModel.bookApplications.collectAsState()
    val leakFingerprintState by authorBookEditorViewModel.leakFingerprintState.collectAsState()
    val isLoading by bookApplicationViewModel.isLoading.collectAsState()
    val bookDetails by bookDetailsViewModel.bookDetails.collectAsState()
    val bookReviews by reviewViewModel.bookReviews.collectAsState()
    val allOverdueReviews by bookApplicationViewModel.overdueReviews.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Pending", "Approved", "Rejected", "Reviews", "Statistics")

    val book = bookDetails ?: bookApplications.firstOrNull()?.book

    val showLeakFingerprintTool = book?.let { b ->
        !b.fileUrl.isNullOrBlank() &&
            b.distributionType?.lowercase() != "physical"
    } == true

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

    LaunchedEffect(Unit) {
        bookApplicationViewModel.loadOverdueReviews()
    }

    LaunchedEffect(bookId) {
        bookDetailsViewModel.getBookDetails(bookId)
        bookApplicationViewModel.loadBookApplications(bookId)
        reviewViewModel.loadBookReviews(bookId)
        authorBookEditorViewModel.clearLeakFingerprintState()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab != 1 && isSelectionMode) {
            isSelectionMode = false
            selectedApplicationIds = emptySet()
        }
    }

    val applicationsWithReviews = remember(bookApplications, bookReviews) {
        bookApplications.map { application ->
            val review =
                application.review ?: bookReviews.find { it.applicationId == application.id }
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

    val overdueReviews = remember(allOverdueReviews, bookId) {
        allOverdueReviews.filter { application ->
            application.bookId == bookId || application.book?.id == bookId
        }
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
        contentWindowInsets = AppScaffoldContentInsets,
        topBar = {
            AppTopBar(
                title = book?.title ?: "Book Details",
                subtitle = "Status: ${formatBookStatus(book?.status)}",
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
                        navController.navigate(Screen.BookAnalytics.createRoute(bookId))
                    }) {
                        Icon(Icons.Filled.Info, contentDescription = "Analytics")
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .paddingTopFromScaffold(paddingValues),
            contentPadding = PaddingValues(bottom = 0.dp),
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

            if (showLeakFingerprintTool) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    LeakFingerprintDecodeSection(
                        leakFingerprintState = leakFingerprintState,
                        bookApplications = applicationsWithReviews,
                        onFileChosen = { uri ->
                            authorBookEditorViewModel.decodeLeakFingerprint(bookId, uri, context)
                        },
                        onDismissResult = { authorBookEditorViewModel.clearLeakFingerprintState() },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            if (overdueReviews.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    OverdueReviewsCard(
                        overdueCount = overdueReviews.size,
                        overdueApplications = overdueReviews
                    )
                }
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
                            bookApplicationViewModel.bulkActionApplications(
                                selectedApplicationIds.toList(),
                                "approved"
                            )
                            selectedApplicationIds = emptySet()
                            isSelectionMode = false
                        },
                        onRejectSelected = {
                            bookApplicationViewModel.bulkActionApplications(
                                selectedApplicationIds.toList(),
                                "rejected"
                            )
                            selectedApplicationIds = emptySet()
                            isSelectionMode = false
                        },
                        onMarkSentSelected = {
                            selectedApplicationIds.forEach { id ->
                                bookApplicationViewModel.markCopySent(id)
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
                                        bookApplicationViewModel.approveApplication(
                                            app.id,
                                            notes
                                        )
                                    },
                                    onReject = { app, notes ->
                                        bookApplicationViewModel.rejectApplication(
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
                                        bookApplicationViewModel.approveApplication(
                                            app.id,
                                            notes
                                        )
                                    },
                                    onReject = { app, notes ->
                                        bookApplicationViewModel.rejectApplication(
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
                    val requiresPhysicalCopy =
                        book?.distributionType?.lowercase() in listOf("physical", "both")
                    if (requiresPhysicalCopy) {
                        item {
                            AuthorApprovedTabInfoCard()
                        }
                    }
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
                                    onMarkSent = { app -> bookApplicationViewModel.markCopySent(app.id) }
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
                            ApplicationReaderReviewCard(
                                application = reviewsApplications[index],
                                navController = navController
                            )
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
        RunLotteryDialog(
            availableCopies = book?.availableCopies ?: 0,
            pendingCount = applicationStats.pending,
            onConfirm = {
                bookApplicationViewModel.runLottery(bookId)
                showLotteryDialog = false
            },
            onDismiss = { showLotteryDialog = false }
        )
    }
}

