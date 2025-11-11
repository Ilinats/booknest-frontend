package com.example.booknest.ui.applications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.Book
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.ApplicationViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationFormScreen(
    navController: NavController,
    authManager: AuthManager,
    book: Book,
    applicationViewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(authManager)
    ),
) {
    var applicationMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    // Email verification is now handled by the new 6-digit system
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUser = authManager.getCurrentUser()

    LaunchedEffect(Unit) {
        applicationViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
            if (message.contains("successfully")) {
                navController.popBackStack()
            }
        }
    }
    
    // Email verification is now handled by the new 6-digit system
    // No need for old verification status checking

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Apply for Review", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email verification is now handled by the new 6-digit system
            // Users must verify their email during registration
                // Book information
                Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Book Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    book.shortDescription?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Age Rating: ${book.ageRating ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Pages: ${book.pageCount ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Application Deadline: ${book.applicationDeadline?.let { formatDate(it) } ?: "Not specified"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Application form
            Text(
                text = "Application Message",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tell the author why you'd like to review this book. This message will help them decide whether to approve your application.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = applicationMessage,
                onValueChange = { applicationMessage = it },
                label = { Text("Your message to the author") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                placeholder = { Text("I'm interested in reviewing this book because...") }
            )

            Text(
                text = "Character count: ${applicationMessage.length}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = {
                    isSubmitting = true
                    applicationViewModel.createApplication(
                        bookId = book.id,
                        message = applicationMessage.ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSubmitting) "Submitting..." else "Submit Application")
            }

            // Terms and conditions
            Text(
                text = "By submitting this application, you agree to:\n" +
                        "• Read the book within the specified timeframe\n" +
                        "• Submit an honest review\n" +
                        "• Follow the author's review guidelines",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: java.util.Date())
    } catch (e: Exception) {
        dateString
    }
}
