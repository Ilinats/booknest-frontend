package com.example.booknest.ui.applications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.theme.DarkNavyBlue
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.ReadingStatus
import org.koin.androidx.compose.getViewModel
import com.example.booknest.viewmodel.FileViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

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
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Approved", "Completed", "Rejected")

    LaunchedEffect(Unit) {
        applicationViewModel.loadMyApplications()
        applicationViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(fileUiState.downloadingMessage) {
        fileUiState.downloadingMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(fileUiState.successMessage) {
        fileUiState.successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            fileViewModel.clearSuccessMessage()
            fileViewModel.clearDownloadingMessage()
        }
    }

    LaunchedEffect(fileUiState.error) {
        fileUiState.error?.let {
            snackbarHostState.showSnackbar(it)
            fileViewModel.clearError()
            fileViewModel.clearDownloadingMessage()
        }
    }

    val filteredApplications = remember(selectedTab, myApplications) {
        when (selectedTab) {
            0 -> myApplications.filter { it.status == "pending" }
            1 -> myApplications.filter { it.status == "approved" && it.reviewSubmittedAt == null }
            2 -> myApplications.filter {
                it.status == "approved" && (it.readingStatus == "reviewed" || it.reviewSubmittedAt != null)
            }

            3 -> myApplications.filter { it.status == "rejected" }
            else -> myApplications
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Applications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DarkNavyBlue
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF1E9EE)
                )
            )
        },
        containerColor = Color(0xFFF1E9EE)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1E9EE))
                .padding(top = 12.dp)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8DFE4)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.Transparent,
                        contentColor = DarkNavyBlue,
                        edgePadding = 8.dp,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = DarkNavyBlue
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val count = when (index) {
                                0 -> myApplications.count { it.status == "pending" }
                                1 -> myApplications.count { it.status == "approved" && it.reviewSubmittedAt == null }
                                2 -> myApplications.count {
                                    it.status == "approved" && (it.readingStatus == "reviewed" || it.reviewSubmittedAt != null)
                                }

                                3 -> myApplications.count { it.status == "rejected" }
                                else -> 0
                            }
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        "$title ($count)",
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) DarkNavyBlue else Color(
                                            0xFF757575
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                if (isLoading && filteredApplications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkNavyBlue)
                    }
                } else if (filteredApplications.isEmpty()) {
                    EmptyApplicationsState(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        tabName = tabs[selectedTab]
                    )
                } else {
                    when (selectedTab) {
                        0 -> PendingApplicationsContent(
                            applications = filteredApplications,
                            applicationViewModel = applicationViewModel,
                            navController = navController
                        )

                        1 -> ApprovedApplicationsContent(
                            applications = filteredApplications,
                            applicationViewModel = applicationViewModel,
                            fileViewModel = fileViewModel,
                            navController = navController
                        )

                        2 -> CompletedApplicationsContent(
                            applications = filteredApplications,
                            navController = navController,
                            fileViewModel = fileViewModel
                        )

                        3 -> RejectedApplicationsContent(
                            applications = filteredApplications,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String?) {
    val (backgroundColor, textColor, statusText) = when (status) {
        "pending" -> Triple(
            SkyBluePeriwinkle.copy(alpha = 0.3f),
            DarkNavyBlue,
            "Pending"
        )

        "approved" -> Triple(
            SkyBluePeriwinkle.copy(alpha = 0.5f),
            DarkNavyBlue,
            "Approved"
        )

        "rejected" -> Triple(
            Color(0xFFFFCDD2),
            Color(0xFFC62828),
            "Rejected"
        )

        "withdrawn" -> Triple(
            Color(0xFFE8DFE4),
            Color(0xFF757575),
            "Withdrawn"
        )

        null -> Triple(
            Color(0xFFE8DFE4),
            Color(0xFF757575),
            "Unknown"
        )

        else -> Triple(
            Color(0xFFE8DFE4),
            Color(0xFF757575),
            "Unknown"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyApplicationsState(
    modifier: Modifier = Modifier,
    tabName: String = ""
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape
                ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8DFE4)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MenuBook,
                    contentDescription = "No Applications",
                    modifier = Modifier.size(56.dp),
                    tint = DarkNavyBlue.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No ${tabName.lowercase()} applications",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = DarkNavyBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (tabName) {
                "Pending" -> "Apply for book review copies to see them here!"
                "Approved" -> "Your approved applications will appear here"
                "Completed" -> "Books you've reviewed will show up here"
                "Rejected" -> "Rejected applications will appear here"
                else -> "Browse books and apply for review copies to get started."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color(0xFF757575)
        )
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
            StyledApplicationCard(
                application = application,
                statusColor = SkyBluePeriwinkle.copy(alpha = 0.3f),
                onClick = {
                    navController.navigate(Screen.BookDetails.createRoute(application.bookId))
                },
                actionButton = {
                    OutlinedButton(
                        onClick = { applicationViewModel.withdrawApplication(application.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DarkNavyBlue
                        )
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Withdraw",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Withdraw")
                    }
                }
            )
        }
    }
}

@Composable
fun ApprovedApplicationsContent(
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
            StyledApplicationCard(
                application = application,
                statusColor = SkyBluePeriwinkle.copy(alpha = 0.5f),
                showProgress = true,
                onClick = {
                    navController.navigate(Screen.BookDetails.createRoute(application.bookId))
                },
                actionButton = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { fileViewModel.downloadBook(application.bookId) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkNavyBlue
                                )
                            ) {
                                Icon(
                                    Icons.Filled.Download,
                                    contentDescription = "Download",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            if (application.copySentAt != null && application.copyReceivedAt == null && application.reviewSubmittedAt == null) {
                                Button(
                                    onClick = {
                                        applicationViewModel.markCopyReceived(application.id)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SkyBluePeriwinkle
                                    )
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = "Mark Received",
                                        modifier = Modifier.size(16.dp),
                                        tint = DarkNavyBlue
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Received",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = DarkNavyBlue
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (application.readingStatus) {
                                "not_started" -> {
                                    OutlinedButton(
                                        onClick = {
                                            applicationViewModel.updateReadingStatus(
                                                application.id,
                                                ReadingStatus.CURRENTLY_READING
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = DarkNavyBlue
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Start Reading")
                                    }
                                }

                                "currently_reading" -> {
                                    OutlinedButton(
                                        onClick = {
                                            applicationViewModel.updateReadingStatus(
                                                application.id,
                                                ReadingStatus.FOR_REVIEW
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = DarkNavyBlue
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mark for Review")
                                    }
                                }

                                "for_review" -> {
                                    if (application.copyReceivedAt != null || application.copySentAt == null) {
                                        Button(
                                            onClick = {
                                                navController.navigate(
                                                    Screen.ReviewSubmission.createRoute(
                                                        application.id
                                                    )
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = DarkNavyBlue
                                            )
                                        ) {
                                            Icon(
                                                Icons.Filled.RateReview,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Write Review")
                                        }
                                    }
                                }
                            }

                            if (application.copyReceivedAt != null && application.reviewSubmittedAt == null && application.readingStatus != "for_review") {
                                Button(
                                    onClick = {
                                        navController.navigate(
                                            Screen.ReviewSubmission.createRoute(
                                                application.id
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkNavyBlue
                                    )
                                ) {
                                    Icon(
                                        Icons.Filled.RateReview,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Review")
                                }
                            }
                        }
                    }
                }
            )
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
            StyledApplicationCard(
                application = application,
                statusColor = SkyBluePeriwinkle.copy(alpha = 0.7f),
                showCompleted = true,
                onClick = {
                    navController.navigate(Screen.BookDetails.createRoute(application.bookId))
                },
                actionButton = {
                    Button(
                        onClick = { fileViewModel.downloadBook(application.bookId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkNavyBlue
                        )
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download")
                    }
                }
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
            StyledApplicationCard(
                application = application,
                statusColor = Color(0xFFFFCDD2),
                onClick = {
                    navController.navigate(Screen.BookDetails.createRoute(application.bookId))
                },
                actionButton = {
                    if (application.authorNotes != null) {
                        Text(
                            text = "Reason: ${application.authorNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun StyledApplicationCard(
    application: ApplicationResponse,
    statusColor: Color,
    showProgress: Boolean = false,
    showCompleted: Boolean = false,
    onClick: () -> Unit,
    actionButton: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Card(
                    modifier = Modifier
                        .size(70.dp, 100.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SkyBluePeriwinkle.copy(alpha = 0.2f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.MenuBook,
                            contentDescription = "Book Cover",
                            modifier = Modifier.size(32.dp),
                            tint = DarkNavyBlue.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = application.book?.title ?: application.bookTitle ?: "Unknown Book",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyBlue,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "by ${application.book?.author?.displayName ?: application.authorName ?: "Unknown Author"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    StatusChip(status = application.status)

                    Text(
                        text = "Applied: ${formatDate(application.appliedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            if (showProgress) {
                Spacer(modifier = Modifier.height(16.dp))
                ApplicationProgressBar(application = application)
            }

            if (showCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = DarkNavyBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Review submitted",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = DarkNavyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            actionButton()
        }
    }
}

@Composable
fun ApplicationProgressBar(application: ApplicationResponse) {
    val steps = listOf(
        "Applied" to (application.appliedAt != null),
        "Approved" to (application.status == "approved"),
        "Received" to (application.copyReceivedAt != null),
        "Reading" to (application.readingStatus != "not_started"),
        "Reviewed" to (application.reviewSubmittedAt != null)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SkyBluePeriwinkle.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, (label, completed) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (completed) DarkNavyBlue else Color(0xFFE8DFE4)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completed) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Completed",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF757575),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (completed) DarkNavyBlue else Color(0xFF757575),
                        fontWeight = if (completed) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicationDetails(application: ApplicationResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.DateRange,
                contentDescription = "Applied",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Applied: ${formatDate(application.appliedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
        }

        application.respondedAt?.let { respondedAt ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Responded",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Responded: ${formatDate(respondedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
        }

        if (application.copySentAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Copy Sent",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy sent: ${formatDate(application.copySentAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
        }

        if (application.copyReceivedAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = "Copy Received",
                    modifier = Modifier.size(16.dp),
                    tint = DarkNavyBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy received: ${formatDate(application.copyReceivedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkNavyBlue
                )
            }
        }

        application.authorNotes?.let { notes ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Author Notes:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = DarkNavyBlue
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
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
