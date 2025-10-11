package com.example.booknest.ui.reviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.*
import com.example.booknest.viewmodel.ReviewViewModel
import com.example.booknest.viewmodel.ReviewViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSubmissionScreen(
    navController: NavController,
    authManager: AuthManager,
    applicationId: String,
    reviewViewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(authManager)
    )
) {
    var rating by remember { mutableStateOf(5) }
    var reviewType by remember { mutableStateOf(ReviewType.TEXT) }
    var reviewContent by remember { mutableStateOf("") }
    var reviewUrls by remember { mutableStateOf(listOf("")) }
    var isPublic by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        reviewViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
            if (message.contains("successfully")) {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Submit Review", fontWeight = FontWeight.Bold) },
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
            // Rating section
            Text(
                text = "Rating",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            RatingSelector(
                currentRating = rating,
                onRatingChange = { rating = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Review type selection
            Text(
                text = "Review Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            ReviewTypeSelector(
                currentType = reviewType,
                onTypeChange = { reviewType = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Review content based on type
            when (reviewType) {
                ReviewType.TEXT -> {
                    Text(
                        text = "Review Content",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Write your detailed review of the book. Share your thoughts on the plot, characters, writing style, and overall experience.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = reviewContent,
                        onValueChange = { reviewContent = it },
                        label = { Text("Your review") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10,
                        placeholder = { Text("Share your thoughts about this book...") }
                    )
                    
                    Text(
                        text = "Word count: ${reviewContent.split("\\s+".toRegex()).filter { it.isNotBlank() }.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                ReviewType.LINK -> {
                    Text(
                        text = "Review Links",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Share links to your review on other platforms (blog, Goodreads, social media, etc.).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    ReviewUrlInputs(
                        urls = reviewUrls,
                        onUrlsChange = { reviewUrls = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy settings
            Text(
                text = "Privacy Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Public Review",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isPublic) "Your review will be visible to everyone" else "Your review will be private",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = {
                    isSubmitting = true
                    val content = if (reviewType == ReviewType.TEXT) reviewContent else null
                    val urls = if (reviewType == ReviewType.LINK) reviewUrls.filter { it.isNotBlank() } else null
                    
                    reviewViewModel.createReview(
                        applicationId = applicationId,
                        rating = rating,
                        reviewType = reviewType,
                        reviewContent = content,
                        reviewUrls = urls,
                        isPublic = isPublic
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting && isValidReview(reviewType, reviewContent, reviewUrls)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSubmitting) "Submitting..." else "Submit Review")
            }

            // Guidelines
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
                        text = "Review Guidelines",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Be honest and constructive in your feedback\n" +
                                "• Avoid spoilers without warning\n" +
                                "• Focus on the book's content and quality\n" +
                                "• Be respectful to the author and other readers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RatingSelector(
    currentRating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..5).forEach { rating ->
            IconButton(
                onClick = { onRatingChange(rating) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (rating <= currentRating) Icons.Filled.Star else Icons.Filled.FavoriteBorder,
                    contentDescription = "$rating stars",
                    modifier = Modifier.size(32.dp),
                    tint = if (rating <= currentRating) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReviewTypeSelector(
    currentType: ReviewType,
    onTypeChange: (ReviewType) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        ReviewType.values().forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentType == type,
                        onClick = { onTypeChange(type) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentType == type,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = when (type) {
                            ReviewType.TEXT -> "Text Review"
                            ReviewType.LINK -> "Link Review"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when (type) {
                            ReviewType.TEXT -> "Write your review directly in the app"
                            ReviewType.LINK -> "Share links to reviews on other platforms"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewUrlInputs(
    urls: List<String>,
    onUrlsChange: (List<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        urls.forEachIndexed { index, url ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { newUrl ->
                        val newUrls = urls.toMutableList()
                        newUrls[index] = newUrl
                        onUrlsChange(newUrls)
                    },
                    label = { Text("Review URL ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("https://...") }
                )
                
                if (urls.size > 1) {
                    IconButton(
                        onClick = {
                            val newUrls = urls.toMutableList()
                            newUrls.removeAt(index)
                            onUrlsChange(newUrls)
                        }
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Remove URL",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        
        if (urls.size < 3) {
            OutlinedButton(
                onClick = {
                    onUrlsChange(urls + "")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add URL",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Another URL")
            }
        }
    }
}

private fun isValidReview(
    reviewType: ReviewType,
    reviewContent: String,
    reviewUrls: List<String>
): Boolean {
    return when (reviewType) {
        ReviewType.TEXT -> reviewContent.isNotBlank() && reviewContent.length >= 50
        ReviewType.LINK -> reviewUrls.any { it.isNotBlank() && it.startsWith("http") }
    }
}
