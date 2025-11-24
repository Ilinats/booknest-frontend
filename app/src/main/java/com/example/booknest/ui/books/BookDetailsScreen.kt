package com.example.booknest.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.booknest.navigation.Screen
import com.example.booknest.network.*
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.ApplicationViewModelFactory
import com.example.booknest.viewmodel.AuthorFollowViewModel
import com.example.booknest.viewmodel.AuthorFollowViewModelFactory
import com.example.booknest.viewmodel.BookViewModel
import com.example.booknest.viewmodel.BookViewModelFactory
import com.example.booknest.viewmodel.ReviewViewModel
import com.example.booknest.viewmodel.ReviewViewModelFactory
import com.example.booknest.ui.components.ReviewLinkPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
    ),
    bookViewModel: BookViewModel = viewModel(
        factory = BookViewModelFactory(authManager)
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
        println("DEBUG: BookDetailsScreen - bookId: $bookId")
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
            println("DEBUG: Book details API failed: ${e.message}")
            isLoading = false
        }
        
        // Check if user has already applied for this book (this will also provide book info as fallback)
        applicationViewModel.checkApplication(bookId)
        
        applicationViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    // Use book data from main screen as primary fallback if main API failed
    LaunchedEffect(bookViewModel.books, bookViewModel.featuredBooks, bookViewModel.recommendedBooks, bookViewModel.newReleases) {
        if (book == null) {
            println("DEBUG: Trying to find book in main screen data")
            println("DEBUG: books.value size: ${bookViewModel.books.value.size}")
            println("DEBUG: featuredBooks.value size: ${bookViewModel.featuredBooks.value.size}")
            println("DEBUG: recommendedBooks.value size: ${bookViewModel.recommendedBooks.value.size}")
            println("DEBUG: newReleases.value size: ${bookViewModel.newReleases.value.size}")
            
            val allBooks = bookViewModel.books.value + 
                          bookViewModel.featuredBooks.value + 
                          bookViewModel.recommendedBooks.value + 
                          bookViewModel.newReleases.value
            
            println("DEBUG: Total books available: ${allBooks.size}")
            println("DEBUG: Looking for bookId: $bookId")
            println("DEBUG: Available book IDs: ${allBooks.map { it.id }}")
            
            val foundBook = allBooks.find { it.id == bookId }
            if (foundBook != null) {
                println("DEBUG: Found book in main screen data: $foundBook")
                book = foundBook
            } else {
                println("DEBUG: Book not found in main screen data")
            }
        }
    }
    
    // Use book info from application check as secondary fallback if main API failed and main screen data not available
    LaunchedEffect(applicationViewModel.applicationCheck) {
        applicationViewModel.applicationCheck.collect { appCheck ->
            println("DEBUG: Application check fallback - book is null: ${book == null}")
            println("DEBUG: Application check fallback - appCheck: $appCheck")
            println("DEBUG: Application check fallback - appCheck?.application: ${appCheck?.application}")
            println("DEBUG: Application check fallback - appCheck?.application?.book: ${appCheck?.application?.book}")
            
            // Only run fallback if book is null AND we have application check data
            if (book == null && appCheck?.application?.book != null) {
                val bookFromApp = appCheck.application.book
                println("DEBUG: Creating fallback book from application check: $bookFromApp")
                // Create a minimal Book object from the application check response
                book = Book(
                    id = bookFromApp.id,
                    authorId = bookFromApp.authorId,
                    title = bookFromApp.title,
                    shortDescription = null,
                    fullDescription = null,
                    coverImageUrl = null,
                    pageCount = null,
                    ageRating = null,
                    distributionType = null,
                    fileUrl = null,
                    fileSize = null,
                    fileType = null,
                    totalCopies = null,
                    availableCopies = null,
                    applicationDeadline = null,
                    reviewDeadlineDays = null,
                    selectionCriteria = null,
                    selectionMethod = null,
                    status = null,
                    createdAt = null,
                    updatedAt = null,
                    publishedAt = null,
                    seriesId = null,
                    seriesOrder = null,
                    seriesName = null,
                    authorName = null,
                    author = null,
                    rating = null
                )
                println("DEBUG: Application check fallback book created: $book")
            } else if (book == null) {
                println("DEBUG: No book data available in application check")
            } else {
                println("DEBUG: Book already exists, not using application check fallback")
            }
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
                authManager = authManager,
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
    authManager: AuthManager,
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
                bookId = book.id,
                bookTitle = book.title,
                bookCoverImageUrl = book.coverImageUrl,
                authorName = book.author?.username ?: "Unknown Author",
                status = check.application.status,
                appliedAt = check.application.appliedAt,
                applicationMessage = null,
                authorNotes = null,
                respondedAt = null,
                readingStatus = "not_started",
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
            val deadline = book.applicationDeadline?.let { deadlineFormat.parse(it) }
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
                println("DEBUG: showWithdrawButton = ${!isApplicationDeadlinePassed && userApplication?.status == "pending"}")
            }
            
            ApplicationInfoSection(
                book = book,
                userApplication = userApplication,
                onApplyClick = onApplyClick,
                onWithdrawClick = onWithdrawClick,
                showApplyButton = !isApplicationDeadlinePassed && (userApplication == null || userApplication.status == "withdrawn"),
                showWithdrawButton = !isApplicationDeadlinePassed && userApplication?.status == "pending",
                navController = navController
            )
        }

        // About the author
        item {
            AboutAuthorSection(
                book = book, 
                navController = navController,
                authManager = authManager
            )
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
                text = "by ${book.author?.name ?: book.authorName ?: "Unknown Author"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Series info not available in current model
            if (book.seriesId != null) {
                Text(
                    text = "Book ${book.seriesOrder ?: 1} of Series",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GenreTagsSection(book: Book) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        if (book.genres.isNullOrEmpty()) {
            // Show fallback genre if no genres available
            item {
                GenreTag(
                    text = "General",
                    isPrimary = false
                )
            }
        } else {
            // Show actual genres from the book
            items(book.genres.size) { index ->
                val genre = book.genres[index]
                GenreTag(
                    text = genre.name,
                    isPrimary = true
                )
            }
        }
    }
}

@Composable
fun GenreTag(
    text: String,
    isPrimary: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                if (isPrimary) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (isPrimary) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
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
        MetadataRow(label = "Age", value = book.ageRating?.uppercase() ?: "N/A")
        Divider()
        MetadataRow(label = "Distribution", value = book.distributionType?.replaceFirstChar { it.uppercase() } ?: "N/A")
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
    showWithdrawButton: Boolean,
    navController: NavController? = null
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
                    text = "Slots Filled: ${(book.totalCopies ?: 0) - (book.availableCopies ?: 0)}/${book.totalCopies ?: 0}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Application Deadline: ${book.applicationDeadline?.let { formatDate(it) } ?: "Not specified"}",
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
            userApplication?.status == "approved" -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✅ Application Approved! Check your email for the book copy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            userApplication?.status == "rejected" -> {
                Text(
                    text = "❌ Application Rejected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            userApplication?.status == "withdrawn" -> {
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
fun AboutAuthorSection(
    book: Book, 
    navController: NavController,
    authManager: AuthManager
) {
    val authorFollowViewModel: AuthorFollowViewModel = viewModel(
        factory = AuthorFollowViewModelFactory(authManager)
    )
    
    var isFollowing by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()
    
    // Try to get author info from book.author first, fallback to book.authorId
    val authorId = book.author?.id ?: book.authorId
    val authorUsername = book.author?.username
    
    // Check if following on load
    LaunchedEffect(authorId) {
        // Default to false while checking
        isFollowing = false
        if (authorId != null) {
            authorFollowViewModel.checkIfFollowingAuthor(authorId) { following ->
                isFollowing = following
            }
        }
    }
    
    // Listen for errors and update follow status
    LaunchedEffect(Unit) {
        authorFollowViewModel.error.collectLatest { error ->
            error?.let {
                // On error, refresh follow status
                if (authorId != null) {
                    authorFollowViewModel.checkIfFollowingAuthor(authorId) { following ->
                        isFollowing = following
                    }
                }
            }
        }
    }
    
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
                book.author?.profilePictureUrl?.let { profileUrl ->
                    AsyncImage(
                        model = profileUrl,
                        contentDescription = book.author?.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Text(
                    text = book.author?.name?.firstOrNull()?.uppercase() ?: "?",
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
                    text = book.author?.name ?: book.authorName ?: "Unknown Author",
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
            // Only show Follow button if we have authorId
            if (authorId != null) {
                OutlinedButton(
                    onClick = { 
                        if (isFollowing == true) {
                            authorFollowViewModel.unfollowAuthor(authorId)
                            isFollowing = false
                        } else {
                            authorFollowViewModel.followAuthor(authorId)
                            isFollowing = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isFollowing != null
                ) {
                    Text(if (isFollowing == true) "Unfollow" else "Follow Author")
                }
            }
            
            OutlinedButton(
                onClick = { 
                    authorId?.let { 
                        navController.navigate("profile/$it")
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = authorId != null
            ) {
                Text("View Profile")
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
                    val reviewer = review.application?.reader
                    val reviewerInitial = reviewer?.username?.firstOrNull()?.uppercase() ?: "R"
                    val reviewerName = reviewer?.username 
                        ?: "${reviewer?.firstName ?: ""} ${reviewer?.lastName ?: ""}".trim()
                        .takeIf { it.isNotBlank() }
                        ?: "Reviewer"
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        reviewer?.profilePictureUrl?.let { profileUrl ->
                            AsyncImage(
                                model = profileUrl,
                                contentDescription = reviewerName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Text(
                            text = reviewerInitial,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = reviewerName,
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
            val reviewType = review.reviewType ?: ReviewType.TEXT
            when (reviewType) {
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
                    review.reviewUrls?.forEach { url ->
                        if (url.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ReviewLinkPreview(
                                url = url,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            // Show both text and links if both are present
            if (review.reviewContent != null && review.reviewUrls != null && review.reviewUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                review.reviewUrls.forEach { url ->
                    if (url.isNotBlank()) {
                        ReviewLinkPreview(
                            url = url,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
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
