package com.example.booknest.ui.applications

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
fun AuthorApplicationManagementScreen(
    navController: NavController,
    authManager: AuthManager,
    bookId: String,
    applicationViewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(authManager)
    )
) {
    val bookApplications by applicationViewModel.bookApplications.collectAsState()
    val isLoading by applicationViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showBulkActionDialog by remember { mutableStateOf(false) }
    var selectedApplications by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(bookId) {
        applicationViewModel.loadBookApplications(bookId)
        applicationViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
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
                // Application statistics
                ApplicationStats(applications = bookApplications)

                // Applications list
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
                            onMarkSent = {
                                applicationViewModel.markCopySent(application.id)
                            }
                        )
                    }
                }
            }
        }

        // Bulk action dialog
        if (showBulkActionDialog) {
            BulkActionDialog(
                selectedCount = selectedApplications.size,
                onDismiss = { showBulkActionDialog = false },
                onBulkApprove = { notes ->
                    applicationViewModel.bulkActionApplications(
                        selectedApplications.toList(),
                        "approve",
                        notes
                    )
                    selectedApplications = emptySet()
                    showBulkActionDialog = false
                },
                onBulkReject = { notes ->
                    applicationViewModel.bulkActionApplications(
                        selectedApplications.toList(),
                        "reject",
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
fun ApplicationStats(applications: List<Application>) {
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
    application: Application,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onApprove: (String?) -> Unit,
    onReject: (String?) -> Unit,
    onMarkSent: () -> Unit
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
            // Header with reader info and selection
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

            // Application details
            ApplicationDetails(application = application)

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            AuthorApplicationActions(
                application = application,
                onApprove = { showApproveDialog = true },
                onReject = { showRejectDialog = true },
                onMarkSent = onMarkSent
            )
        }
    }

    // Approve dialog
    if (showApproveDialog) {
        ApplicationActionDialog(
            title = "Approve Application",
            message = "Add notes for the reader (optional):",
            onConfirm = { notes -> onApprove(notes) },
            onDismiss = { showApproveDialog = false }
        )
    }

    // Reject dialog
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
fun AuthorApplicationActions(
    application: Application,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onMarkSent: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (application.status) {
            "pending" -> {
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
