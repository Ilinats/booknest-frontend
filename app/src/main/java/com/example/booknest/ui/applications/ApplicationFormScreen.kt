package com.example.booknest.ui.applications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.viewmodel.ApplicationViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationFormScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    book: BookResponse,
    applicationViewModel: ApplicationViewModel = getViewModel()
) {
    var applicationMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val currentUser by sessionManager.currentUser.collectAsState()

    val applicationCheck by applicationViewModel.applicationCheck.collectAsState()
    LaunchedEffect(applicationCheck) {
        if (applicationCheck?.hasApplied == true && !isSubmitting) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply for Review", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
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
            val isEmailVerified = currentUser?.emailVerified == true

            if (!isEmailVerified) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Email Verification Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "You must verify your email address before applying for book reviews. Please check your email and verify your account, or visit your profile to resend the verification email.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go to Profile")
                        }
                    }
                }
            } else {
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
                            text = "Application Deadline: ${
                                book.applicationDeadline?.let { dateString ->
                                    try {
                                        val inputFormat = java.text.SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                            java.util.Locale.getDefault()
                                        )
                                        val outputFormat = java.text.SimpleDateFormat(
                                            "MMM dd, yyyy",
                                            java.util.Locale.getDefault()
                                        )
                                        val date = inputFormat.parse(dateString)
                                        outputFormat.format(date ?: java.util.Date())
                                    } catch (e: Exception) {
                                        dateString
                                    }
                                } ?: "Not specified"
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

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
}
