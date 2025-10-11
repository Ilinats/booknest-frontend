package com.example.booknest.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.data.AuthManager
import com.example.booknest.network.*
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.ApplicationViewModelFactory
import com.example.booknest.viewmodel.ReviewViewModel
import com.example.booknest.viewmodel.ReviewViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    navController: NavController,
    authManager: AuthManager,
    bookId: String,
    applicationViewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(authManager)
    ),
    reviewViewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(authManager)
    )
) {
    var book by remember { mutableStateOf<Book?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var userApplication by remember { mutableStateOf<Application?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load book details, reviews, and user's applications
    LaunchedEffect(bookId) {
        try {
            val response = RetrofitInstance.api.getBookDetails(bookId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    book = apiResponse.data
                    book?.let { 
                        reviewViewModel.loadBookReviews(it.id)
                    }
                }
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
        
        // Check if user has already applied for this book
        applicationViewModel.checkApplication(bookId)
        
        applicationViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Book Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (book != null) {
            BookDetailsContent(
                book = book!!,
                navController = navController,
                applicationViewModel = applicationViewModel,
                reviewViewModel = reviewViewModel,
                onApplyClick = { showApplyDialog = true },
                onWithdrawClick = { showWithdrawDialog = true },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Book not found")
            }
        }
    }

    // Apply dialog
    if (showApplyDialog && book != null) {
        ApplicationFormDialog(
            book = book!!,
            onDismiss = { showApplyDialog = false },
            onSubmit = { message ->
                applicationViewModel.createApplication(book!!.id, message)
                // Refresh application check after creating application
                applicationViewModel.checkApplication(bookId)
                showApplyDialog = false
            }
        )
    }
    
    // Withdraw dialog
    if (showWithdrawDialog && userApplication != null) {
        WithdrawApplicationDialog(
            book = book!!,
            onDismiss = { showWithdrawDialog = false },
            onConfirm = {
                applicationViewModel.withdrawApplication(userApplication!!.id)
                // Refresh application check after withdrawal
                applicationViewModel.checkApplication(bookId)
                showWithdrawDialog = false
            }
        )
    }
}

@Composable
fun BookDetailsContent(
    book: Book,
    navController: NavController,
    applicationViewModel: ApplicationViewModel,
    reviewViewModel: ReviewViewModel,
    onApplyClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bookReviews by reviewViewModel.bookReviews.collectAsState()
    val isLoadingReviews by reviewViewModel.isLoading.collectAsState()
    val applicationCheck by applicationViewModel.applicationCheck.collectAsState()
    
    // Get user's application for this book
    val userApplication = remember(book.id, applicationCheck) {
        val check = applicationCheck
        if (check?.hasApplied == true && check.application != null) {
            // Convert ApplicationCheckApplication to Application-like object
            Application(
                id = check.application.id,
                status = ApplicationStatus.valueOf(check.application.status.uppercase()),
                appliedAt = check.application.appliedAt,
                applicationMessage = check.application.applicationMessage,
                authorNotes = check.application.authorNotes,
                respondedAt = check.application.respondedAt,
                bookId = book.id,
                readerId = null,
                reader = null,
                book = book,
                review = null,
                copySentAt = null,
                copyReceivedAt = null,
                reviewSubmittedAt = null,
                readingStatus = ReadingStatus.NOT_STARTED,
                readingStartedAt = null,
                readingCompletedAt = null
            )
        } else {
            null
        }
    }
    
    // Check if application deadline has passed
    val isApplicationDeadlinePassed = remember(book.applicationDeadline) {
        try {
            val deadlineFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val deadline = deadlineFormat.parse(book.applicationDeadline)
            val now = Date()
            deadline?.before(now) ?: true
        } catch (e: Exception) {
            true // If we can't parse the date, assume deadline passed
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Book cover and basic info
        item {
            BookHeaderSection(book = book)
        }

        // Genre tags
        item {
            GenreTagsSection(book = book)
        }

        // Book description
        item {
            BookDescriptionSection(book = book)
        }

        // Book metadata
        item {
            BookMetadataSection(book = book)
        }

        // Application information and apply/withdraw button
        item {
            // Debug logging
            LaunchedEffect(userApplication) {
                println("DEBUG: userApplication = $userApplication")
                println("DEBUG: userApplication?.status = ${userApplication?.status}")
                println("DEBUG: isApplicationDeadlinePassed = $isApplicationDeadlinePassed")
                println("DEBUG: showWithdrawButton = ${!isApplicationDeadlinePassed && userApplication?.status == ApplicationStatus.PENDING}")
            }
            
            ApplicationInfoSection(
                book = book,
                userApplication = userApplication,
                onApplyClick = onApplyClick,
                onWithdrawClick = onWithdrawClick,
                showApplyButton = !isApplicationDeadlinePassed && (userApplication == null || userApplication.status == ApplicationStatus.WITHDRAWN),
                showWithdrawButton = !isApplicationDeadlinePassed && userApplication?.status == ApplicationStatus.PENDING
            )
        }

        // About the author
        item {
            AboutAuthorSection(book = book, navController = navController)
        }

        // Reviews section
        item {
            ReviewsSection(
                reviews = bookReviews,
                isLoading = isLoadingReviews,
                bookId = book.id
            )
        }
    }
}

@Composable
fun BookHeaderSection(book: Book) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Book cover
        Box(
            modifier = Modifier
                .size(120.dp, 160.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (book.coverImageUrl != null) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = "Book Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Book Cover",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Book info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "by ${book.author?.username ?: "Unknown Author"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            book.series?.let { series ->
                Text(
                    text = "Book ${book.seriesOrder ?: 1} of ${series.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GenreTagsSection(book: Book) {
    val genres = book.bookGenres?.mapNotNull { it.genre?.name } ?: emptyList()
    
    if (genres.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            genres.take(3).forEach { genre ->
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun BookDescriptionSection(book: Book) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = book.fullDescription ?: book.shortDescription ?: "No description available.",
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Composable
fun BookMetadataSection(book: Book) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MetadataRow(label = "Pages", value = book.pageCount?.toString() ?: "N/A")
        Divider()
        MetadataRow(label = "Age", value = book.ageRating.value.uppercase())
        Divider()
        MetadataRow(label = "Distribution", value = book.distributionType.value.replaceFirstChar { it.uppercase() })
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ApplicationInfoSection(
    book: Book,
    userApplication: Application?,
    onApplyClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    showApplyButton: Boolean,
    showWithdrawButton: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Application info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Slots Filled: ${book.totalCopies - book.availableCopies}/${book.totalCopies}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Application Deadline: ${formatDate(book.applicationDeadline)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Review Deadline: ${book.reviewDeadlineDays} days after approval",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Application status and buttons
        // Debug logging
        LaunchedEffect(userApplication, showWithdrawButton, showApplyButton) {
            println("DEBUG ApplicationInfoSection: userApplication?.status = ${userApplication?.status}")
            println("DEBUG ApplicationInfoSection: showWithdrawButton = $showWithdrawButton")
            println("DEBUG ApplicationInfoSection: showApplyButton = $showApplyButton")
        }
        
        when {
            userApplication?.status == ApplicationStatus.APPROVED -> {
                Text(
                    text = "✅ Application Approved! Check your email for the book copy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            userApplication?.status == ApplicationStatus.REJECTED -> {
                Text(
                    text = "❌ Application Rejected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            userApplication?.status == ApplicationStatus.WITHDRAWN -> {
                Text(
                    text = "Application Withdrawn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            showWithdrawButton -> {
                OutlinedButton(
                    onClick = onWithdrawClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Withdraw Application")
                }
            }
            showApplyButton -> {
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
fun AboutAuthorSection(book: Book, navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "About the Author",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Author profile picture
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = book.author?.username?.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.author?.username ?: "Unknown Author",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Author of ${book.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { /* TODO: Follow author */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Follow Author")
            }
            
            OutlinedButton(
                onClick = { 
                    book.author?.id?.let { authorId ->
                        navController.navigate("profile/$authorId")
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("View Profile")
            }
        }
        
        // Add stats button for authors
        book.author?.userType?.let { userType ->
            if (userType == "author") {
                OutlinedButton(
                    onClick = { 
                        book.author?.id?.let { authorId ->
                            navController.navigate("stats/$authorId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Author Statistics")
                }
            }
        }
    }
}

@Composable
fun ReviewsSection(
    reviews: List<Review>,
    isLoading: Boolean,
    bookId: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Reviews",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (reviews.isEmpty()) {
            Text(
                text = "No reviews yet. Be the first to review this book!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                reviews.take(3).forEach { review ->
                    ReviewCard(review = review)
                }
                
                if (reviews.size > 3) {
                    TextButton(
                        onClick = { /* TODO: Navigate to all reviews */ }
                    ) {
                        Text("View All Reviews (${reviews.size})")
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Review header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reviewer profile picture
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "R", // TODO: Get actual reviewer name
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = "Reviewer", // TODO: Get actual reviewer name
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatDate(review.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Rating stars
                Row {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= review.rating) Icons.Filled.Star else Icons.Filled.Star,
                            contentDescription = "$star stars",
                            modifier = Modifier.size(16.dp),
                            tint = if (star <= review.rating) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Review content
            when (review.reviewType) {
                ReviewType.TEXT -> {
                    review.reviewContent?.let { content ->
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                ReviewType.LINK -> {
                    Text(
                        text = "Review available at external link",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicationFormDialog(
    book: Book,
    onDismiss: () -> Unit,
    onSubmit: (String?) -> Unit
) {
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply for Review Copy") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("You are applying to review \"${book.title}\"")
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Why do you want to review this book? (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(message.ifBlank { null }) }) {
                Text("Submit Application")
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
fun WithdrawApplicationDialog(
    book: Book,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw Application") },
        text = {
            Text("Are you sure you want to withdraw your application for \"${book.title}\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Withdraw")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
