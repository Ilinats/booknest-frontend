package com.example.booknest.ui.myapplications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.FileViewModel
import com.example.booknest.viewmodel.ReadingStatus
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class SortOption {
    APPLICATION_DATE,
    DEADLINE,
    STATUS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    applicationViewModel: ApplicationViewModel = getViewModel(),
    fileViewModel: FileViewModel = getViewModel()
) {
    val myApplications by applicationViewModel.myApplications.collectAsState()
    val isLoading by applicationViewModel.isLoading.collectAsState()
    val fileUiState by fileViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var sortOption by remember { mutableStateOf(SortOption.APPLICATION_DATE) }
    var showSortMenu by remember { mutableStateOf(false) }

    val tabs = listOf("All", "Pending", "Approved", "Completed", "Rejected")

    LaunchedEffect(Unit) {
        applicationViewModel.loadMyApplications()
    }

    LaunchedEffect(fileUiState.error) {
        fileUiState.error?.let { error ->
            com.example.booknest.ui.toast.GlobalToastHandler.showError(error)
            fileViewModel.clearError()
        }
    }

    LaunchedEffect(fileUiState.downloadingMessage) {
        fileUiState.downloadingMessage?.let { message ->
            com.example.booknest.ui.toast.GlobalToastHandler.showInfo(message)
        }
    }

    LaunchedEffect(fileUiState.successMessage) {
        fileUiState.successMessage?.let { message ->
            com.example.booknest.ui.toast.GlobalToastHandler.showSuccess(message)
            fileViewModel.clearSuccessMessage()
            fileViewModel.clearDownloadingMessage()
        }
    }

    val stats = remember(myApplications) {
        val total = myApplications.size
        val approved = myApplications.count { it.status == "approved" }
        val rejected = myApplications.count { it.status == "rejected" }
        val approvalRate = if (total > 0) (approved.toDouble() / total * 100) else 0.0

        val now = Calendar.getInstance()
        val thisMonth = now.get(Calendar.MONTH)
        val thisYear = now.get(Calendar.YEAR)

        val reviewsThisMonth = myApplications.count { app ->
            app.reviewSubmittedAt?.let { dateStr ->
                try {
                    val inputFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val date = inputFormat.parse(dateStr)
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        cal.get(Calendar.MONTH) == thisMonth && cal.get(Calendar.YEAR) == thisYear
                    } else false
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }

        val pendingReviews = myApplications.count {
            it.status == "approved" &&
                    it.reviewSubmittedAt == null &&
                    it.readingStatus != "reviewed"
        }

        ApplicationStats(
            total = total,
            approvalRate = approvalRate,
            reviewsThisMonth = reviewsThisMonth,
            pendingReviews = pendingReviews
        )
    }

    val filteredAndSortedApplications = remember(selectedTab, myApplications, sortOption) {
        val filtered = when (selectedTab) {
            0 -> myApplications
            1 -> myApplications.filter { it.status == "pending" }
            2 -> myApplications.filter {
                it.status == "approved" && it.reviewSubmittedAt == null
            }

            3 -> myApplications.filter {
                it.status == "approved" && (it.readingStatus == "reviewed" || it.reviewSubmittedAt != null)
            }

            4 -> myApplications.filter {
                it.status == "rejected" || it.status == "withdrawn"
            }

            else -> myApplications
        }

        when (sortOption) {
            SortOption.APPLICATION_DATE -> filtered.sortedByDescending {
                parseDate(it.appliedAt)?.time ?: 0L
            }

            SortOption.DEADLINE -> filtered.sortedBy { app ->
                val deadline = app.book?.reviewDeadline ?: app.book?.applicationDeadline
                deadline?.let { parseDate(it)?.time } ?: Long.MAX_VALUE
            }

            SortOption.STATUS -> filtered.sortedBy { it.status }
        }
    }

    val approvedApplications = remember(filteredAndSortedApplications, selectedTab) {
        if (selectedTab == 2) {
            val awaitingCopy = filteredAndSortedApplications.filter {
                it.copyReceivedAt == null
            }
            val reading = filteredAndSortedApplications.filter {
                it.copyReceivedAt != null && it.reviewSubmittedAt == null
            }
            Pair(awaitingCopy, reading)
        } else {
            Pair(emptyList(), emptyList())
        }
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
                TopAppBar(
                    title = {
                        Text(
                            "My Applications",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("By Application Date") },
                                onClick = {
                                    sortOption = SortOption.APPLICATION_DATE
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Deadline") },
                                onClick = {
                                    sortOption = SortOption.DEADLINE
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Status") },
                                onClick = {
                                    sortOption = SortOption.STATUS
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                QuickStatsSummary(stats = stats)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 0.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        val count = when (index) {
                            0 -> myApplications.size
                            1 -> myApplications.count { it.status == "pending" }
                            2 -> myApplications.count {
                                it.status == "approved" && it.reviewSubmittedAt == null
                            }

                            3 -> myApplications.count {
                                it.status == "approved" && (it.readingStatus == "reviewed" || it.reviewSubmittedAt != null)
                            }

                            4 -> myApplications.count {
                                it.status == "rejected" || it.status == "withdrawn"
                            }

                            else -> 0
                        }

                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    if (count > 0) "$title ($count)" else title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            if (selectedTab == 0 && filteredAndSortedApplications.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "For detailed information and actions, check the Pending, Approved, Completed, and Rejected tabs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (isLoading && filteredAndSortedApplications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (selectedTab == 2 && approvedApplications.first.isEmpty() && approvedApplications.second.isEmpty()) {
                item {
                    EmptyApplicationsState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        message = "No approved applications",
                        onBrowseBooks = { navController.navigate(Screen.Home.route) }
                    )
                }
            } else if (selectedTab != 2 && filteredAndSortedApplications.isEmpty()) {
                item {
                    EmptyApplicationsState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        message = when (selectedTab) {
                            0 -> "No applications yet"
                            1 -> "No pending applications"
                            3 -> "No completed applications"
                            4 -> "No rejected applications"
                            else -> "No applications"
                        },
                        onBrowseBooks = { navController.navigate(Screen.Home.route) }
                    )
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        items(filteredAndSortedApplications) { application ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                ApplicationCard(
                                    application = application,
                                    showFullDetails = false,
                                    applicationViewModel = applicationViewModel,
                                    fileViewModel = fileViewModel,
                                    navController = navController
                                )
                            }
                        }
                    }

                    1 -> {
                        items(filteredAndSortedApplications) { application ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                PendingApplicationCard(
                                    application = application,
                                    applicationViewModel = applicationViewModel,
                                    navController = navController
                                )
                            }
                        }
                    }

                    2 -> {
                        if (approvedApplications.first.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Awaiting Copy (${approvedApplications.first.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(approvedApplications.first) { application ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    ApprovedApplicationCard(
                                        application = application,
                                        applicationViewModel = applicationViewModel,
                                        fileViewModel = fileViewModel,
                                        navController = navController
                                    )
                                }
                            }
                        }

                        if (approvedApplications.second.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Reading (${approvedApplications.second.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(approvedApplications.second) { application ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    ApprovedApplicationCard(
                                        application = application,
                                        applicationViewModel = applicationViewModel,
                                        fileViewModel = fileViewModel,
                                        navController = navController
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        items(filteredAndSortedApplications) { application ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                CompletedApplicationCard(
                                    application = application,
                                    navController = navController,
                                    fileViewModel = fileViewModel
                                )
                            }
                        }
                    }

                    4 -> {
                        items(filteredAndSortedApplications) { application ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                RejectedApplicationCard(
                                    application = application,
                                    navController = navController
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

data class ApplicationStats(
    val total: Int,
    val approvalRate: Double,
    val reviewsThisMonth: Int,
    val pendingReviews: Int
)

@Composable
fun QuickStatsSummary(stats: ApplicationStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total\nBooks",
                    value = stats.total.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Approval Rate",
                    value = "${stats.approvalRate.toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Reviews This Month",
                    value = stats.reviewsThisMonth.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Pending Reviews",
                    value = stats.pendingReviews.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AllApplicationsContent(
    applications: List<ApplicationResponse>,
    applicationViewModel: ApplicationViewModel,
    fileViewModel: FileViewModel,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(applications) { application ->
            ApplicationCard(
                application = application,
                showFullDetails = false,
                applicationViewModel = applicationViewModel,
                fileViewModel = fileViewModel,
                navController = navController
            )
        }
    }
}

@Composable
fun PendingApplicationsContent(
    applications: List<ApplicationResponse>,
    applicationViewModel: ApplicationViewModel,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(applications) { application ->
            PendingApplicationCard(
                application = application,
                applicationViewModel = applicationViewModel,
                navController = navController
            )
        }
    }
}

@Composable
fun ApprovedApplicationsContent(
    awaitingCopy: List<ApplicationResponse>,
    reading: List<ApplicationResponse>,
    applicationViewModel: ApplicationViewModel,
    fileViewModel: FileViewModel,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (awaitingCopy.isNotEmpty()) {
            item {
                Text(
                    text = "Awaiting Copy (${awaitingCopy.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(awaitingCopy) { application ->
                ApprovedApplicationCard(
                    application = application,
                    applicationViewModel = applicationViewModel,
                    fileViewModel = fileViewModel,
                    navController = navController
                )
            }
        }

        if (reading.isNotEmpty()) {
            item {
                Text(
                    text = "Reading (${reading.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(reading) { application ->
                ApprovedApplicationCard(
                    application = application,
                    applicationViewModel = applicationViewModel,
                    fileViewModel = fileViewModel,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun CompletedApplicationsContent(
    applications: List<ApplicationResponse>,
    navController: NavController,
    fileViewModel: FileViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(applications) { application ->
            CompletedApplicationCard(
                application = application,
                navController = navController,
                fileViewModel = fileViewModel
            )
        }
    }
}

@Composable
fun RejectedApplicationsContent(
    applications: List<ApplicationResponse>,
    navController: NavController
) {
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

@Composable
fun ApplicationCard(
    application: ApplicationResponse,
    showFullDetails: Boolean,
    applicationViewModel: ApplicationViewModel,
    fileViewModel: FileViewModel,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                application.bookId?.let {
                    navController.navigate(Screen.BookDetails.createRoute(it))
                }
            },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val coverImageUrl = application.bookCoverImageUrl ?: application.book?.coverImageUrl
                if (!coverImageUrl.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(coverImageUrl),
                        contentDescription = application.bookTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Book,
                        contentDescription = "No cover",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = application.bookTitle ?: application.book?.title ?: "Unknown Book",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable {
                        application.bookId?.let {
                            navController.navigate(Screen.BookDetails.createRoute(it))
                        }
                    }
                )

                Text(
                    text = "by ${application.authorName ?: application.book?.authorName ?: application.book?.author?.displayName ?: "Unknown Author"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                    }
                )

                Text(
                    text = "Applied: ${formatDate(application.appliedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                StatusBadge(status = application.status)
            }
        }
    }
}

@Composable
fun PendingApplicationCard(
    application: ApplicationResponse,
    applicationViewModel: ApplicationViewModel,
    navController: NavController
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val coverImageUrl = application.bookCoverImageUrl ?: application.book?.coverImageUrl
                    if (!coverImageUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(coverImageUrl),
                            contentDescription = application.bookTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = "No cover",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = application.bookTitle ?: application.book?.title ?: "Unknown Book",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            application.bookId?.let {
                                navController.navigate(Screen.BookDetails.createRoute(it))
                            }
                        }
                    )

                    Text(
                        text = "by ${application.authorName ?: application.book?.authorName ?: application.book?.author?.displayName ?: "Unknown Author"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                        }
                    )

                    Text(
                        text = "Applied: ${formatDate(application.appliedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    StatusBadge(status = application.status)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            application.applicationMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Your Application:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showWithdrawDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Withdraw",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Withdraw Application")
            }
        }
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Withdraw Application") },
            text = {
                Text("Are you sure you want to withdraw your application? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        applicationViewModel.withdrawApplication(application.id)
                        showWithdrawDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Withdraw")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ApprovedApplicationCard(
    application: ApplicationResponse,
    applicationViewModel: ApplicationViewModel,
    fileViewModel: FileViewModel,
    navController: NavController
) {
    val book = application.book
    val isDigital = book?.distributionType?.lowercase() in listOf("digital", "both")
    val isPhysical = book?.distributionType?.lowercase() in listOf("physical", "both")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val coverImageUrl = application.bookCoverImageUrl ?: application.book?.coverImageUrl
                    if (!coverImageUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(coverImageUrl),
                            contentDescription = application.bookTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = "No cover",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = application.bookTitle ?: book?.title ?: "Unknown Book",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            application.bookId?.let {
                                navController.navigate(Screen.BookDetails.createRoute(it))
                            }
                        }
                    )

                    Text(
                        text = "by ${application.authorName ?: book?.authorName ?: book?.author?.displayName ?: "Unknown Author"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                        }
                    )

                    Text(
                        text = "Applied: ${formatDate(application.appliedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    StatusBadge(status = application.status)
                }
            }

            ProgressTimeline(application = application)

            CopyStatusSection(
                application = application,
                isDigital = isDigital,
                isPhysical = isPhysical,
                onMarkReceived = {
                    applicationViewModel.markCopyReceived(application.id)
                },
                onDownload = {
                    application.bookId?.let { fileViewModel.downloadBook(it) }
                }
            )

            if (application.reviewSubmittedAt == null) {
                ReadingStatusSelector(
                    currentStatus = when (application.readingStatus) {
                        "not_started" -> ReadingStatus.NOT_STARTED
                        "currently_reading" -> ReadingStatus.CURRENTLY_READING
                        "for_review" -> ReadingStatus.FOR_REVIEW
                        "reviewed" -> ReadingStatus.FOR_REVIEW
                        else -> ReadingStatus.NOT_STARTED
                    },
                    onStatusChange = { status ->
                        applicationViewModel.updateReadingStatus(application.id, status)
                    }
                )
            }

            application.book?.reviewDeadline?.let { deadline ->
                ReviewDeadlineCountdown(deadline = deadline)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isDigital && application.copyReceivedAt != null) {
                    Button(
                        onClick = {
                            application.bookId?.let { fileViewModel.downloadBook(it) }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download Book")
                    }
                }

                if (application.readingStatus == "for_review" ||
                    (application.copyReceivedAt != null && application.reviewSubmittedAt == null)
                ) {
                    Button(
                        onClick = {
                            navController.navigate(Screen.ReviewSubmission.createRoute(application.id))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Review",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Write Review")
                    }
                }

                TextButton(
                    onClick = {
                        application.bookId?.let {
                            navController.navigate(Screen.BookDetails.createRoute(it))
                        }
                    }
                ) {
                    Text("View Book Details")
                }
            }
        }
    }
}

@Composable
fun ProgressTimeline(application: ApplicationResponse) {
    val steps = listOf(
        "Applied" to (application.appliedAt != null),
        "Approved" to (application.status == "approved"),
        "Copy Received" to (application.copyReceivedAt != null),
        "Reading" to (application.readingStatus != "not_started" && application.readingStatus != "reviewed"),
        "Reviewed" to (application.reviewSubmittedAt != null)
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Progress",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, (label, completed) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (completed) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completed) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Completed",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (completed) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CopyStatusSection(
    application: ApplicationResponse,
    isDigital: Boolean,
    isPhysical: Boolean,
    onMarkReceived: () -> Unit,
    onDownload: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Copy Status",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )

        if (isDigital) {
            if (application.copyReceivedAt != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Downloaded",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Downloaded",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Button(
                            onClick = onDownload,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = "Re-download",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-download", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Button(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = "Download",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Book")
                }
            }
        }

        if (isPhysical) {
            if (application.copySentAt != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocalShipping,
                                contentDescription = "Shipped",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shipped on ${formatDate(application.copySentAt)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (application.copyReceivedAt == null) {
                            Button(
                                onClick = onMarkReceived,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Mark Received",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mark as Received")
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Received",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Received on ${formatDate(application.copyReceivedAt)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = "Awaiting",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Awaiting shipment",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingStatusSelector(
    currentStatus: ReadingStatus,
    onStatusChange: (ReadingStatus) -> Unit
) {
    val statuses = ReadingStatus.values()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Reading Status",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statuses.forEach { status ->
                FilterChip(
                    onClick = { onStatusChange(status) },
                    label = {
                        Text(
                            text = status.value.replace("_", " ").replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = currentStatus == status,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ReviewDeadlineCountdown(deadline: String) {
    val (daysLeft, isUrgent, isCritical) = remember(deadline) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val deadlineDate = inputFormat.parse(deadline) ?: Date()
            val now = Date()
            val diff = deadlineDate.time - now.time
            val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()

            Triple(days, days <= 7 && days >= 0, days <= 3 && days >= 0)
        } catch (e: Exception) {
            Triple(null, false, false)
        }
    }

    if (daysLeft != null) {
        val backgroundColor = when {
            isCritical -> MaterialTheme.colorScheme.errorContainer
            isUrgent -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

        val textColor = when {
            isCritical -> MaterialTheme.colorScheme.onErrorContainer
            isUrgent -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isCritical) Icons.Filled.Warning else Icons.Filled.Schedule,
                        contentDescription = "Deadline",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Review Deadline",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Text(
                            text = formatDate(deadline),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor
                        )
                    }
                }

                Text(
                    text = when {
                        daysLeft < 0 -> "Deadline passed"
                        daysLeft == 0 -> "Due today!"
                        daysLeft == 1 -> "1 day left"
                        else -> "$daysLeft days left"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun CompletedApplicationCard(
    application: ApplicationResponse,
    navController: NavController,
    fileViewModel: FileViewModel
) {
    val review = application.review

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val coverImageUrl = application.bookCoverImageUrl ?: application.book?.coverImageUrl
                    if (!coverImageUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(coverImageUrl),
                            contentDescription = application.bookTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = "No cover",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = application.bookTitle ?: application.book?.title ?: "Unknown Book",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            application.bookId?.let {
                                navController.navigate(Screen.BookDetails.createRoute(it))
                            }
                        }
                    )

                    Text(
                        text = "by ${application.authorName ?: application.book?.authorName ?: application.book?.author?.displayName ?: "Unknown Author"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Completed: ${application.reviewSubmittedAt?.let { formatDate(it) } ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    StatusBadge(status = "completed")
                }
            }

            review?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Review",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(it.rating) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "Star",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        it.reviewContent?.take(150)?.let { excerpt ->
                            Text(
                                text = excerpt + if (it.reviewContent.length > 150) "..." else "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    review?.id?.let { id ->
                                        navController.navigate(
                                            Screen.ReviewSubmission.createRoute(application.id) + "?reviewId=$id"
                                        )
                                    } ?: run {
                                        navController.navigate(
                                            Screen.ReviewSubmission.createRoute(
                                                application.id
                                            )
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Review")
                            }
                        }
                    }
                }
            }

            if (application.book?.distributionType?.lowercase() in listOf("digital", "both")) {
                OutlinedButton(
                    onClick = {
                        application.bookId?.let { fileViewModel.downloadBook(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = "Re-download",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-download Book")
                }
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
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val coverImageUrl = application.bookCoverImageUrl ?: application.book?.coverImageUrl
                    if (!coverImageUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(coverImageUrl),
                            contentDescription = application.bookTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = "No cover",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = application.bookTitle ?: application.book?.title ?: "Unknown Book",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            application.bookId?.let {
                                navController.navigate(Screen.BookDetails.createRoute(it))
                            }
                        }
                    )

                    Text(
                        text = "by ${application.authorName ?: application.book?.authorName ?: application.book?.author?.displayName ?: "Unknown Author"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Applied: ${formatDate(application.appliedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    StatusBadge(status = application.status)
                }
            }

            application.authorNotes?.let { notes ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Author's Notes:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String?) {
    val (backgroundColor, textColor, statusText) = when (status?.lowercase()) {
        "pending" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Pending"
        )

        "approved" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Approved"
        )

        "rejected" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Rejected"
        )

        "withdrawn" -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Withdrawn"
        )

        "completed" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Completed"
        )

        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Unknown"
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyApplicationsState(
    message: String,
    onBrowseBooks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Description,
            contentDescription = "No Applications",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBrowseBooks) {
            Text("Browse Books")
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun parseDate(dateString: String): Date? {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.parse(dateString)
    } catch (e: Exception) {
        null
    }
}
