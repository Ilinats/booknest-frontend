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
import com.example.booknest.ui.components.BackButton
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
import com.example.booknest.viewmodel.BookViewModel
import com.example.booknest.viewmodel.BookViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookApplicationDetailScreen(
    navController: NavController,
    authManager: AuthManager,
    bookId: String,
    applicationViewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(authManager)
    ),
    bookViewModel: BookViewModel = viewModel(
        factory = BookViewModelFactory(authManager)
    )
) {
    val bookApplications by applicationViewModel.bookApplications.collectAsState()
    val isLoading by applicationViewModel.isLoading.collectAsState()
    val bookDetails by bookViewModel.bookDetails.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Applicants", "Approved", "Reviews", "Statistics")
    
    // Use book details directly, fallback to first application's book if needed
    val book = bookDetails ?: bookApplications.firstOrNull()?.book

    LaunchedEffect(bookId) {
        // Load both book details and applications
        bookViewModel.getBookDetails(bookId)
        applicationViewModel.loadBookApplications(bookId)
        applicationViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Filter applications based on selected tab
    val filteredApplications = remember(selectedTab, bookApplications) {
        when (selectedTab) {
            0 -> bookApplications.filter { it.status == "pending" }
            1 -> bookApplications.filter { it.status == "approved" }
            2 -> bookApplications.filter { it.reviewSubmittedAt != null }
            3 -> bookApplications // All for statistics
            else -> bookApplications
        }
    }
    
    // For the Applicants tab, we want to show total applications count, not just pending
    val totalApplicationsCount = bookApplications.size

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(book?.title ?: "Book Details", fontWeight = FontWeight.Bold)
                        Text(
                            "Status: ${book?.status?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                },
                actions = {
                    IconButton(onClick = { /* TODO: Edit book - navigate to book edit screen */ }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Book")
                    }
                    IconButton(onClick = { /* TODO: Share book link */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* TODO: View book analytics */ }) {
                        Icon(Icons.Filled.Info, contentDescription = "Analytics")
                    }
                }
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
            when (selectedTab) {
                0 -> ApplicantsTab(
                    applications = filteredApplications,
                    isLoading = isLoading,
                    book = book,
                    totalApplicationsCount = totalApplicationsCount,
                    onApprove = { app -> applicationViewModel.approveApplication(app.id) },
                    onReject = { app -> applicationViewModel.rejectApplication(app.id) }
                )
                1 -> ApprovedTab(
                    applications = filteredApplications,
                    isLoading = isLoading,
                    book = book
                )
                2 -> ReviewsTab(
                    applications = filteredApplications,
                    isLoading = isLoading
                )
                3 -> StatisticsTab(
                    applications = filteredApplications,
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun ApplicantsTab(
    applications: List<Application>,
    isLoading: Boolean,
    book: Book?,
    totalApplicationsCount: Int,
    onApprove: (Application) -> Unit,
    onReject: (Application) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section header
        Text(
            text = "Applicants (${totalApplicationsCount}/${book?.totalCopies ?: 0} slots filled)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { /* TODO: Genre filter */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Genre")
            }
            OutlinedButton(
                onClick = { /* TODO: Date filter */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Date")
            }
            OutlinedButton(
                onClick = { /* TODO: Location filter */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Location")
            }
            OutlinedButton(onClick = { /* TODO: Sort */ }) {
                Text("Sort")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Applications list
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
                        onApprove = { onApprove(application) },
                        onReject = { onReject(application) }
                    )
                }
            }
        }
        
        // Bulk action buttons
        if (applications.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { /* TODO: Bulk approve */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve All")
                }
                OutlinedButton(
                    onClick = { /* TODO: Bulk reject */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject All")
                }
            }
        }
    }
}

@Composable
fun ApplicantCard(
    application: Application,
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
            // Header with profile and approve button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile picture placeholder
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
                            text = "Applied on ${formatDate(application.appliedAt)}",
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
            
            // Genre tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show fallback genres since bookGenres is not available in the current model
                GenreTag("General")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Application message
            application.applicationMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Previous Reviews: N/A", // TODO: Get actual review count from API
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onReject) {
                    Text("Reject")
                }
                TextButton(onClick = { /* TODO: View profile */ }) {
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
    applications: List<Application>,
    isLoading: Boolean,
    book: Book?
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
                    ApprovedReaderCard(application = application)
                }
            }
        }
    }
}

@Composable
fun ApprovedReaderCard(application: Application) {
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
                
                OutlinedButton(onClick = { /* TODO: View profile */ }) {
                    Text("View Profile")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show fallback genres since bookGenres is not available in the current model
                GenreTag("General")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Approved to review '${application.book?.title ?: "this book"}'. Please submit your review by ${formatDate(application.book?.applicationDeadline ?: "")}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ReviewsTab(
    applications: List<Application>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Submitted Reviews",
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
                    ReviewCard(application = application)
                }
            }
        }
    }
}

@Composable
fun ReviewCard(application: Application) {
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
                            text = "Submitted on ${formatDate(application.reviewSubmittedAt ?: "")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                OutlinedButton(onClick = { /* TODO: View review */ }) {
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
}

@Composable
fun StatisticsTab(
    applications: List<Application>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Review Statistics",
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
            // Statistics cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Total Reviews",
                    value = applications.count { it.reviewSubmittedAt != null }.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Average Rating",
                    value = "4.5/5", // TODO: Calculate actual average
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            StatCard(
                title = "Positive Feedback",
                value = "90%", // TODO: Calculate actual percentage
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Review Ratings Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple bar chart representation
            RatingDistributionChart()
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
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RatingDistributionChart() {
    val ratings = listOf(1, 2, 3, 4, 5)
    val heights = listOf(0.1f, 0.2f, 0.3f, 0.8f, 0.6f) // Mock data
    
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

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
