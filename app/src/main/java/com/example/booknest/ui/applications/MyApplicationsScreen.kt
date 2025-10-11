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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.*
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.ApplicationViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    navController: NavController,
    authManager: AuthManager,
    applicationViewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(authManager)
    )
) {
    val myApplications by applicationViewModel.myApplications.collectAsState()
    val isLoading by applicationViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Approved", "Completed", "Rejected")

    LaunchedEffect(Unit) {
        applicationViewModel.loadMyApplications()
        applicationViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Filter applications based on selected tab
    val filteredApplications = remember(selectedTab, myApplications) {
        when (selectedTab) {
            0 -> myApplications.filter { it.status == ApplicationStatus.PENDING }
            1 -> myApplications.filter { it.status == ApplicationStatus.APPROVED }
            2 -> myApplications.filter { it.status == ApplicationStatus.APPROVED && it.readingStatus == ReadingStatus.REVIEWED }
            3 -> myApplications.filter { it.status == ApplicationStatus.REJECTED }
            else -> myApplications
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Applications", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab navigation
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content based on selected tab
            if (isLoading && filteredApplications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredApplications.isEmpty()) {
                EmptyApplicationsState(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                when (selectedTab) {
                    0 -> PendingApplicationsContent(
                        applications = filteredApplications,
                        applicationViewModel = applicationViewModel
                    )
                    1 -> ApprovedApplicationsContent(
                        applications = filteredApplications,
                        applicationViewModel = applicationViewModel
                    )
                    2 -> CompletedApplicationsContent(
                        applications = filteredApplications
                    )
                    3 -> RejectedApplicationsContent(
                        applications = filteredApplications
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(
    application: Application,
    onUpdateReadingStatus: (ReadingStatus) -> Unit,
    onMarkReceived: () -> Unit,
    onWithdraw: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            // Header with book title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = application.book?.title ?: "Unknown Book",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                StatusChip(status = application.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Application details
            ApplicationDetails(application = application)

            Spacer(modifier = Modifier.height(12.dp))

            // Progress indicator
            ApplicationProgress(application = application)

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            ApplicationActions(
                application = application,
                onUpdateReadingStatus = onUpdateReadingStatus,
                onMarkReceived = onMarkReceived,
                onWithdraw = onWithdraw,
                onNavigateToReview = onNavigateToReview
            )
        }
    }
}

@Composable
fun ApplicationDetails(application: Application) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Applied date
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = "Applied",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Applied: ${formatDate(application.appliedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Response date (if available)
        application.respondedAt?.let { respondedAt ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Responded",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Responded: ${formatDate(respondedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Copy status
        if (application.copySentAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Copy Sent",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy sent: ${formatDate(application.copySentAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (application.copyReceivedAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = "Copy Received",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy received: ${formatDate(application.copyReceivedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Author notes (if available)
        application.authorNotes?.let { notes ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Author Notes:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ApplicationProgress(application: Application) {
    val steps = listOf(
        "Applied" to (application.appliedAt != null),
        "Approved" to (application.status == ApplicationStatus.APPROVED),
        "Copy Received" to (application.copyReceivedAt != null),
        "Reading" to (application.readingStatus != ReadingStatus.NOT_STARTED),
        "Reviewed" to (application.reviewSubmittedAt != null)
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
                        .size(24.dp)
                        .background(
                            color = if (completed) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ApplicationActions(
    application: Application,
    onUpdateReadingStatus: (ReadingStatus) -> Unit,
    onMarkReceived: () -> Unit,
    onWithdraw: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Reading status selector
        if (application.status == ApplicationStatus.APPROVED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Reading Status:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                ReadingStatusSelector(
                    currentStatus = application.readingStatus,
                    onStatusChange = onUpdateReadingStatus
                )
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mark copy received button
            if (application.copySentAt != null && application.copyReceivedAt == null) {
                Button(
                    onClick = onMarkReceived,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = "Mark Received",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark Received")
                }
            }

            // Review button
            if (application.copyReceivedAt != null && application.reviewSubmittedAt == null) {
                Button(
                    onClick = onNavigateToReview,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Submit Review",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Review")
                }
            }

            // Withdraw button (only for pending applications)
            if (application.status == ApplicationStatus.PENDING) {
                OutlinedButton(
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = "Withdraw",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Withdraw")
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
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        statuses.forEach { status ->
            FilterChip(
                onClick = { onStatusChange(status) },
                label = { 
                    Text(
                        text = status.value.replace("_", " ").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = currentStatus == status,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatusChip(status: ApplicationStatus) {
    val (backgroundColor, textColor, statusText) = when (status) {
        ApplicationStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Pending"
        )
        ApplicationStatus.APPROVED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Approved"
        )
        ApplicationStatus.REJECTED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Rejected"
        )
        ApplicationStatus.WITHDRAWN -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Withdrawn"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyApplicationsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Create,
            contentDescription = "No Applications",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No applications yet!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Browse books and apply for review copies to get started.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PendingApplicationsContent(
    applications: List<Application>,
    applicationViewModel: ApplicationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(applications) { application ->
            SimpleApplicationCard(
                application = application,
                statusIndicator = Color.Green,
                actionButton = {
                    OutlinedButton(
                        onClick = { applicationViewModel.withdrawApplication(application.id) }
                    ) {
                        Text("Withdraw")
                    }
                }
            )
        }
    }
}

@Composable
fun ApprovedApplicationsContent(
    applications: List<Application>,
    applicationViewModel: ApplicationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(applications) { application ->
            SimpleApplicationCard(
                application = application,
                statusIndicator = Color.Green,
                actionButton = {
                    when (application.readingStatus) {
                        ReadingStatus.NOT_STARTED -> {
                            OutlinedButton(
                                onClick = { 
                                    applicationViewModel.updateReadingStatus(application.id, ReadingStatus.CURRENTLY_READING)
                                }
                            ) {
                                Text("Start")
                            }
                        }
                        ReadingStatus.CURRENTLY_READING -> {
                            OutlinedButton(
                                onClick = { 
                                    applicationViewModel.updateReadingStatus(application.id, ReadingStatus.FOR_REVIEW)
                                }
                            ) {
                                Text("To Review")
                            }
                        }
                        ReadingStatus.FOR_REVIEW -> {
                            OutlinedButton(
                                onClick = { /* TODO: Navigate to review screen */ }
                            ) {
                                Text("Review")
                            }
                        }
                        else -> {
                            // No button for completed reviews
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CompletedApplicationsContent(
    applications: List<Application>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(applications) { application ->
            SimpleApplicationCard(
                application = application,
                statusIndicator = Color.Green,
                actionButton = {
                    // Completed - no action button, just status indicator
                }
            )
        }
    }
}

@Composable
fun RejectedApplicationsContent(
    applications: List<Application>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(applications) { application ->
            SimpleApplicationCard(
                application = application,
                statusIndicator = Color.Red,
                actionButton = {
                    // Rejected - no action button
                }
            )
        }
    }
}

@Composable
fun SimpleApplicationCard(
    application: Application,
    statusIndicator: Color,
    actionButton: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Book cover placeholder
        Box(
            modifier = Modifier
                .size(60.dp, 80.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = "Book Cover",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Book info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = application.book?.title ?: "Unknown Book",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "By ${application.book?.author?.username ?: "Unknown Author"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Status indicator and action button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusIndicator, CircleShape)
            )
            
            // Action button
            actionButton()
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
