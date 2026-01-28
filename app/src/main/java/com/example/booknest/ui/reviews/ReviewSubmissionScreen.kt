package com.example.booknest.ui.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlin.math.roundToInt
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.*
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.ReviewViewModel
import com.example.booknest.viewmodel.ReviewType
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.ui.reviews.components.header.BookReferenceHeader
import com.example.booknest.ui.reviews.components.form.EnhancedRatingSelector
import com.example.booknest.ui.reviews.components.form.EnhancedReviewUrlInputs
import com.example.booknest.ui.reviews.components.common.GuidelineItem
import com.example.booknest.ui.reviews.utils.isValidUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSubmissionScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    applicationId: String,
    reviewId: String? = null,
    reviewViewModel: ReviewViewModel = getViewModel(),
    applicationViewModel: ApplicationViewModel = getViewModel()
) {
    val isEditMode = reviewId != null
    var rating by remember { mutableStateOf(5.0f) }
    var reviewContent by remember { mutableStateOf("") }
    var reviewUrls by remember { mutableStateOf(listOf("")) }
    var isPublic by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showGuidelines by remember { mutableStateOf(false) }
    var hasJustSaved by remember { mutableStateOf(false) }

    var application by remember { mutableStateOf<ApplicationResponse?>(null) }
    val currentReview by reviewViewModel.currentReview.collectAsState()

    LaunchedEffect(applicationId) {
        applicationViewModel.getApplication(applicationId)
            .onSuccess { app ->
                application = app
            }
            .onFailure { e ->
                com.example.booknest.ui.toast.GlobalToastHandler.showError("Failed to load application: ${e.message}")
            }
    }

    LaunchedEffect(reviewId) {
        reviewId?.let {
            reviewViewModel.loadReview(it)
        }
    }

    LaunchedEffect(currentReview) {
        val review = currentReview
        if (isEditMode && review != null) {
            rating = review.rating.toFloat()
            reviewContent = review.reviewContent ?: ""
            reviewUrls = review.reviewUrls?.takeIf { it.isNotEmpty() } ?: listOf("")
            isPublic = review.isPublic
        }
    }

    val isLoading by reviewViewModel.isLoading.collectAsState()

    LaunchedEffect(isLoading) {
        isSubmitting = isLoading
    }

    LaunchedEffect(isLoading) {
        if (hasJustSaved && !isLoading && !isSubmitting) {
            hasJustSaved = false
            applicationViewModel.loadMyApplications()
            kotlinx.coroutines.delay(100)
            navController.popBackStack()
        }
    }

    val book = application?.book
    val bookTitle = book?.title ?: application?.bookTitle ?: "Unknown Book"
    val bookCover = book?.coverImageUrl ?: application?.bookCoverImageUrl
    val authorName = book?.authorName
        ?: book?.author?.displayName
        ?: application?.authorName
        ?: "Unknown Author"
    val reviewDeadline = book?.reviewDeadline

    val validationIssues = remember(rating, reviewContent, reviewUrls) {
        buildList {
            val wordCount =
                reviewContent.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            val hasText = reviewContent.isNotBlank() && wordCount > 0
            val hasLinks = reviewUrls.any { it.isNotBlank() && isValidUrl(it) }

            if (!hasText && !hasLinks) {
                add("You must provide either a text review or at least one valid review link")
            } else {
                val invalidUrls = reviewUrls.filter { it.isNotBlank() && !isValidUrl(it) }
                if (invalidUrls.isNotEmpty()) {
                    add("Some URLs are invalid. Please use http:// or https://")
                }
            }
        }
    }

    val isValid = validationIssues.isEmpty()

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Text(
                            if (isEditMode) "Edit Review" else "Submit Review",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        BackButton(onClick = { navController.popBackStack() })
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BookReferenceHeader(
                    bookTitle = bookTitle,
                    authorName = authorName,
                    bookCover = bookCover,
                    reviewDeadline = reviewDeadline
                )

                Spacer(modifier = Modifier.height(8.dp))

                Divider()

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Rating",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    EnhancedRatingSelector(
                        currentRating = rating,
                        onRatingChange = { rating = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Review Content",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Write your detailed review of the book. Share your thoughts on the plot, characters, writing style, and overall experience. (Optional if you provide links)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = reviewContent,
                        onValueChange = { reviewContent = it },
                        label = { Text("Your review") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        maxLines = 15,
                        placeholder = { Text("Share your thoughts about this book...") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val wordCount = reviewContent.trim().split("\\s+".toRegex())
                            .filter { it.isNotBlank() }.size
                        Text(
                            text = "Word count: $wordCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Review Links",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Share links to your review on other platforms (e.g., your blog, Goodreads, YouTube, TikTok). (Optional if you provide text)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    EnhancedReviewUrlInputs(
                        urls = reviewUrls,
                        onUrlsChange = { reviewUrls = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Review Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = isPublic,
                                onCheckedChange = { isPublic = it }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPublic) "Public Review" else "Private Review",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isPublic)
                                        "Your review will be visible to everyone on the book's page and in search results."
                                    else
                                        "Your review will only be visible to you and the author. It won't appear on the book's public page.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showGuidelines = !showGuidelines }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Review Guidelines",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = if (showGuidelines) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (showGuidelines) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (showGuidelines) {
                                Divider()
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    GuidelineItem("Be honest and constructive in your feedback")
                                    GuidelineItem("Avoid spoilers without warning")
                                    GuidelineItem("Focus on the book's content and quality")
                                    GuidelineItem("Be respectful to the author and other readers")
                                }
                            }
                        }
                    }

                    if (validationIssues.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Warning",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Please fix the following issues:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                validationIssues.forEach { issue ->
                                    Text(
                                        text = "• $issue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(bottom = 32.dp),
                        enabled = !isSubmitting && isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isSubmitting && isValid)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                        }
                        Text(
                            text = when {
                                isSubmitting -> if (isEditMode) "Updating..." else "Submitting..."
                                !isValid -> if (isEditMode) "Update Review" else "Submit Review"
                                else -> if (isEditMode) "Update Review" else "Submit Review"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (!isSubmitting && isValid)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim)
                        .clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isEditMode) "Updating Review..." else "Submitting Review...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text(if (isEditMode) "Update Review?" else "Submit Review?") },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Are you sure you want to submit this review?")
                        Text(
                            text = "Rating: $rating/5",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (reviewContent.isNotBlank()) {
                            Text(
                                text = "Text review: ${reviewContent.length} characters",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (reviewUrls.any { it.isNotBlank() }) {
                            Text(
                                text = "Links: ${reviewUrls.count { it.isNotBlank() }}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "Privacy: ${if (isPublic) "Public" else "Private"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            val content = reviewContent.takeIf { it.isNotBlank() }
                            val urls = reviewUrls.filter { it.isNotBlank() }

                            val reviewType = when {
                                content != null && urls.isNotEmpty() -> ReviewType.TEXT
                                content != null -> ReviewType.TEXT
                                urls.isNotEmpty() -> ReviewType.LINK
                                else -> ReviewType.TEXT
                            }

                            val decimalRating = rating.toDouble().coerceIn(0.0, 5.0)

                            hasJustSaved = true
                            if (isEditMode && reviewId != null) {
                                reviewViewModel.updateReview(
                                    reviewId = reviewId,
                                    rating = decimalRating,
                                    reviewType = reviewType,
                                    reviewContent = content,
                                    reviewUrls = if (urls.isNotEmpty()) urls else null,
                                    isPublic = isPublic
                                )
                            } else {
                                reviewViewModel.createReview(
                                    applicationId = applicationId,
                                    rating = decimalRating,
                                    reviewType = reviewType,
                                    reviewContent = content,
                                    reviewUrls = if (urls.isNotEmpty()) urls else null,
                                    isPublic = isPublic
                                )
                            }
                        },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(
                            text = if (isEditMode) "Update" else "Submit",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
