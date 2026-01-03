package com.example.booknest.ui.applications

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.ReaderAddressResponse
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.BookViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorApplicationManagementScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    bookId: String,
    applicationViewModel: ApplicationViewModel = getViewModel(),
    bookViewModel: BookViewModel = getViewModel()
) {
    val bookApplications by applicationViewModel.bookApplications.collectAsState()
    val isLoading by applicationViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val bookDetails by bookViewModel.bookDetails.collectAsState()

    var showBulkActionDialog by remember { mutableStateOf(false) }
    var showLotteryDialog by remember { mutableStateOf(false) }
    var selectedApplications by remember { mutableStateOf<Set<String>>(emptySet()) }

    val bookFromApplications = bookApplications.firstOrNull { it.book != null }?.book
        ?: bookApplications.firstOrNull()?.book

    val book = bookFromApplications ?: bookDetails

    // Don't use remember - this needs to recalculate when book data loads
    val selectionMethod = book?.selectionMethod
        ?: bookApplications.firstOrNull { it.book != null }?.book?.selectionMethod
        ?: bookApplications.firstOrNull()?.book?.selectionMethod
    
    val isLotteryBook = selectionMethod?.let { method ->
        val methodLower = method.lowercase().trim()
        methodLower == "lottery" || methodLower == "random_selection"
    } ?: false

    val deadlinePassed = book?.applicationDeadline?.let { deadline ->
        try {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
            )
            var parsedDate: Date? = null
            for (format in formats) {
                try {
                    val formatter = SimpleDateFormat(format, Locale.getDefault())
                    parsedDate = formatter.parse(deadline)
                    if (parsedDate != null) break
                } catch (e: Exception) {
                    continue
                }
            }
            parsedDate?.before(Date()) ?: false
        } catch (e: Exception) {
            false
        }
    } ?: false

    LaunchedEffect(bookId) {
        applicationViewModel.loadBookApplications(bookId)
        if (bookApplications.isEmpty() || bookApplications.all { it.book == null }) {
            bookViewModel.getBookDetails(bookId)
        }
        applicationViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(bookApplications) {
        if (bookApplications.isNotEmpty() && bookApplications.all { it.book == null } && bookDetails == null) {
            bookViewModel.getBookDetails(bookId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Book Applications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                },
                actions = {
                    if (bookApplications.isNotEmpty()) {
                        IconButton(
                            onClick = { showBulkActionDialog = true },
                            enabled = selectedApplications.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Bulk Actions"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading && bookApplications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (bookApplications.isEmpty()) {
            EmptyApplicationsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ApplicationStats(applications = bookApplications)

                val lotteryDeadlinePassed = book?.applicationDeadline?.let { deadline ->
                    try {
                        try {
                            val deadlineInstant = Instant.parse(deadline)
                            val nowInstant = Instant.now()
                            deadlineInstant.isBefore(nowInstant)
                        } catch (e: Exception) {
                            val formats = listOf(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                "yyyy-MM-dd HH:mm:ss",
                                "yyyy-MM-dd"
                            )
                            var parsedDate: Date? = null
                            for (format in formats) {
                                try {
                                    val formatter = SimpleDateFormat(format, Locale.getDefault())
                                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                                    parsedDate = formatter.parse(deadline)
                                    if (parsedDate != null) break
                                } catch (e: Exception) {
                                    continue
                                }
                            }
                            parsedDate?.before(Date()) ?: false
                        }
                    } catch (e: Exception) {
                        false
                    }
                } ?: false
                
                val lotteryHasPending = bookApplications.any { it.status == "pending" }
                val lotteryHasProcessed = bookApplications.any { it.status in listOf("approved", "rejected") }

                if (isLotteryBook) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LotterySection(
                        bookId = bookId,
                        isLotteryBook = isLotteryBook,
                        deadlinePassed = lotteryDeadlinePassed,
                        hasPendingApplications = lotteryHasPending,
                        hasProcessedApplications = lotteryHasProcessed,
                        pendingCount = bookApplications.count { it.status == "pending" },
                        availableCopies = book?.availableCopies ?: 0,
                        onRunLottery = {
                            showLotteryDialog = true
                        }
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bookApplications) { application ->
                        AuthorApplicationCard(
                            application = application,
                            isSelected = selectedApplications.contains(application.id),
                            onSelectionChange = { isSelected ->
                                selectedApplications = if (isSelected) {
                                    selectedApplications + application.id
                                } else {
                                    selectedApplications - application.id
                                }
                            },
                            onApprove = { notes ->
                                applicationViewModel.approveApplication(application.id, notes)
                            },
                            onReject = { notes ->
                                applicationViewModel.rejectApplication(application.id, notes)
                            },
                            isLotteryBook = isLotteryBook,
                            onMarkSent = {
                                applicationViewModel.markCopySent(application.id)
                            }
                        )
                    }
                }
            }
        }

        if (showLotteryDialog) {
            RunLotteryDialog(
                pendingCount = bookApplications.count { it.status == "pending" },
                availableCopies = book?.availableCopies ?: 0,
                onConfirm = {
                    applicationViewModel.runLottery(bookId)
                    showLotteryDialog = false
                },
                onDismiss = { showLotteryDialog = false }
            )
        }

        if (showBulkActionDialog) {
            BulkActionDialog(
                selectedCount = selectedApplications.size,
                onDismiss = { showBulkActionDialog = false },
                onBulkApprove = { notes ->
                    applicationViewModel.bulkActionApplications(
                        selectedApplications.toList(),
                        "approved",
                        notes
                    )
                    selectedApplications = emptySet()
                    showBulkActionDialog = false
                },
                onBulkReject = { notes ->
                    applicationViewModel.bulkActionApplications(
                        selectedApplications.toList(),
                        "rejected",
                        notes
                    )
                    selectedApplications = emptySet()
                    showBulkActionDialog = false
                }
            )
        }
    }
}

@Composable
fun ApplicationStats(applications: List<ApplicationResponse>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Total",
                value = applications.size.toString(),
                icon = Icons.Filled.Info
            )
            StatItem(
                label = "Pending",
                value = applications.count { it.status == "pending" }.toString(),
                icon = Icons.Filled.DateRange
            )
            StatItem(
                label = "Approved",
                value = applications.count { it.status == "approved" }.toString(),
                icon = Icons.Filled.CheckCircle
            )
            StatItem(
                label = "Rejected",
                value = applications.count { it.status == "rejected" }.toString(),
                icon = Icons.Filled.Close
            )
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun AuthorApplicationCard(
    application: ApplicationResponse,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onApprove: (String?) -> Unit,
    onReject: (String?) -> Unit,
    onMarkSent: () -> Unit,
    isLotteryBook: Boolean = false
) {
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelectionChange(!isSelected) },
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.reader?.username ?: "Unknown Reader",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = application.reader?.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(status = application.status)
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onSelectionChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ApplicationDetails(application = application)

            val requiresPhysicalCopy =
                application.book?.distributionType?.lowercase() in listOf("physical", "both")
            val shouldShowAddresses = application.status == "approved" && requiresPhysicalCopy &&
                    application.reader?.addresses?.isNotEmpty() == true

            if (shouldShowAddresses) {
                Spacer(modifier = Modifier.height(12.dp))
                ReaderAddressesSection(
                    addresses = application.reader?.addresses ?: emptyList(),
                    readerName = application.reader?.username ?: "Reader"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AuthorApplicationActions(
                application = application,
                onApprove = { showApproveDialog = true },
                onReject = { showRejectDialog = true },
                onMarkSent = onMarkSent,
                isLotteryBook = isLotteryBook
            )
        }
    }

    if (showApproveDialog) {
        ApplicationActionDialog(
            title = "Approve Application",
            message = "Add notes for the reader (optional):",
            onConfirm = { notes -> onApprove(notes) },
            onDismiss = { showApproveDialog = false }
        )
    }

    if (showRejectDialog) {
        ApplicationActionDialog(
            title = "Reject Application",
            message = "Add notes for the reader (optional):",
            onConfirm = { notes -> onReject(notes) },
            onDismiss = { showRejectDialog = false }
        )
    }
}

@Composable
fun RunLotteryDialog(
    pendingCount: Int,
    availableCopies: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Shuffle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Run Lottery Selection")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("This will randomly select $availableCopies winner(s) from $pendingCount pending applications.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Selected applications will be approved, and the rest will be rejected. This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Run Lottery")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AuthorApplicationActions(
    application: ApplicationResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onMarkSent: () -> Unit,
    isLotteryBook: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (application.status) {
            "pending" -> {
                if (isLotteryBook) {
                    Text(
                        text = "Pending lottery selection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                } else {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Approve",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Approve")
                    }

                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = "Reject",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reject")
                    }
                }
            }

            "approved" -> {
                if (application.copySentAt == null) {
                    Button(
                        onClick = onMarkSent,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Mark Sent",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Copy Sent")
                    }
                } else {
                    Text(
                        text = "Copy sent on ${formatDate(application.copySentAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            else -> {
                Text(
                    text = "Application ${application.status?.replaceFirstChar { it.uppercase() } ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun ApplicationActionDialog(
    title: String,
    message: String,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(message)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(notes.ifBlank { null }) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BulkActionDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onBulkApprove: (String?) -> Unit,
    onBulkReject: (String?) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var action by remember { mutableStateOf<BulkAction?>(null) }

    if (action != null) {
        AlertDialog(
            onDismissRequest = { action = null },
            title = { Text("Bulk ${action!!.displayName}") },
            text = {
                Column {
                    Text("You are about to ${action!!.displayName.lowercase()} $selectedCount applications.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (action) {
                            BulkAction.APPROVE -> onBulkApprove(notes.ifBlank { null })
                            BulkAction.REJECT -> onBulkReject(notes.ifBlank { null })
                            null -> TODO()
                        }
                        action = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { action = null }) {
                    Text("Cancel")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Bulk Actions") },
            text = { Text("Select an action for $selectedCount applications:") },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { action = BulkAction.APPROVE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Approve All")
                    }
                    OutlinedButton(
                        onClick = { action = BulkAction.REJECT }
                    ) {
                        Text("Reject All")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

private enum class BulkAction(val displayName: String) {
    APPROVE("Approve"),
    REJECT("Reject")
}

@Composable
fun LotterySection(
    bookId: String,
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
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with icon and title
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
            
            // Content based on state
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
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Icon(
                                Icons.Filled.Shuffle,
                                contentDescription = "Run Lottery",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Lottery Selection", style = MaterialTheme.typography.bodySmall)
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
                        // Stats row
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

@Composable
fun ReaderAddressesSection(
    addresses: List<ReaderAddressResponse>,
    readerName: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = "Address",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Shipping Address${if (addresses.size > 1) "es (${addresses.size})" else ""}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (expanded) {
                addresses.forEachIndexed { index, address ->
                    if (index > 0) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (address.isPrimary) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Primary",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Primary Address",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = address.streetAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${address.city}, ${address.postalCode}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = address.country,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
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
