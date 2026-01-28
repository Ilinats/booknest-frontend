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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
import com.example.booknest.ui.myapplications.models.SortOption
import com.example.booknest.ui.myapplications.models.ApplicationStats
import com.example.booknest.ui.myapplications.utils.parseDate
import com.example.booknest.ui.myapplications.components.stats.QuickStatsSummary
import com.example.booknest.ui.myapplications.components.common.EmptyApplicationsState
import com.example.booknest.ui.myapplications.components.common.StatusBadge
import com.example.booknest.ui.myapplications.components.content.AllApplicationsContent
import com.example.booknest.ui.myapplications.components.content.PendingApplicationsContent
import com.example.booknest.ui.myapplications.components.content.ApprovedApplicationsContent
import com.example.booknest.ui.myapplications.components.content.CompletedApplicationsContent
import com.example.booknest.ui.myapplications.components.content.RejectedApplicationsContent
import com.example.booknest.ui.myapplications.components.cards.ApplicationCard
import com.example.booknest.ui.myapplications.components.cards.PendingApplicationCard
import com.example.booknest.ui.myapplications.components.cards.ApprovedApplicationCard
import com.example.booknest.ui.myapplications.components.cards.CompletedApplicationCard
import com.example.booknest.ui.myapplications.components.cards.RejectedApplicationCard
import com.example.booknest.ui.myapplications.components.info.ApprovedTabInfoCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    var searchQuery by remember { mutableStateOf("") }
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

    val filteredAndSortedApplications = remember(selectedTab, myApplications, searchQuery, sortOption) {
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

        val searched = if (searchQuery.isBlank()) {
            filtered
        } else {
            filtered.filter { application ->
                application.bookTitle?.contains(searchQuery, ignoreCase = true) == true ||
                application.book?.title?.contains(searchQuery, ignoreCase = true) == true ||
                application.authorName?.contains(searchQuery, ignoreCase = true) == true ||
                application.book?.author?.firstName?.contains(searchQuery, ignoreCase = true) == true ||
                application.book?.author?.lastName?.contains(searchQuery, ignoreCase = true) == true ||
                application.book?.author?.username?.contains(searchQuery, ignoreCase = true) == true ||
                application.applicationMessage?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        when (sortOption) {
            SortOption.APPLICATION_DATE -> searched.sortedByDescending {
                parseDate(it.appliedAt)?.time ?: 0L
            }

            SortOption.DEADLINE -> searched.sortedBy { app ->
                val deadline = app.book?.reviewDeadline ?: app.book?.applicationDeadline
                deadline?.let { parseDate(it)?.time } ?: Long.MAX_VALUE
            }

            SortOption.STATUS -> searched.sortedBy { it.status }
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
                        com.example.booknest.ui.components.BackButton(
                            onClick = { navController.popBackStack() }
                        )
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    placeholder = { Text("Search applications...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
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
                        AllApplicationsContent(
                            applications = filteredAndSortedApplications,
                            applicationViewModel = applicationViewModel,
                            fileViewModel = fileViewModel,
                            navController = navController
                        )
                    }

                    1 -> {
                        PendingApplicationsContent(
                            applications = filteredAndSortedApplications,
                            applicationViewModel = applicationViewModel,
                            navController = navController
                        )
                    }

                    2 -> {
                        ApprovedApplicationsContent(
                            awaitingCopy = approvedApplications.first,
                            reading = approvedApplications.second,
                            applicationViewModel = applicationViewModel,
                            fileViewModel = fileViewModel,
                            navController = navController
                        )
                    }

                    3 -> {
                        CompletedApplicationsContent(
                            applications = filteredAndSortedApplications,
                            navController = navController,
                            fileViewModel = fileViewModel
                        )
                    }

                    4 -> {
                        RejectedApplicationsContent(
                            applications = filteredAndSortedApplications,
                            navController = navController
                        )
                    }
                }
            }
        }
        }
    }
}