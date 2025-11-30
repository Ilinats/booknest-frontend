package com.example.booknest.ui.reviews

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.Review
import com.example.booknest.viewmodel.ReviewViewModel
import com.example.booknest.viewmodel.ReviewViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReviewsScreen(
    navController: NavController,
    authManager: AuthManager,
    bookId: String,
    reviewViewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(authManager)
    )
) {
    val bookReviews by reviewViewModel.bookReviews.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bookId) {
        reviewViewModel.loadBookReviews(bookId)
        reviewViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Book Reviews", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) { paddingValues ->
        if (isLoading && bookReviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (bookReviews.isEmpty()) {
            EmptyReviewsState(
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
                // Review statistics
                ReviewStats(reviews = bookReviews)

                // Reviews list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Featured reviews first
                    val featuredReviews = bookReviews.filter { it.isFeatured }
                    if (featuredReviews.isNotEmpty()) {
                        item {
                            Text(
                                text = "Featured Reviews",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(featuredReviews) { review ->
                            ReviewCard(
                                review = review,
                                isFeatured = true
                            )
                        }
                        
                        item {
                            Text(
                                text = "All Reviews",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Regular reviews
                    val regularReviews = bookReviews.filter { !it.isFeatured }
                    items(regularReviews) { review ->
                        ReviewCard(
                            review = review,
                            isFeatured = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewStats(reviews: List<Review>) {
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else 0.0
    
    val ratingDistribution = reviews.groupingBy { it.rating }.eachCount()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Average rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Average Rating",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("%.1f", averageRating),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= averageRating.toInt()) Icons.Filled.Star else Icons.Filled.FavoriteBorder,
                                    contentDescription = "$star stars",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (star <= averageRating.toInt()) 
                                        Color.Yellow 
                                    else 
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                Text(
                    text = "${reviews.size} reviews",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Rating distribution
            if (reviews.isNotEmpty()) {
                Text(
                    text = "Rating Distribution",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                (5 downTo 1).forEach { rating ->
                    val count = ratingDistribution[rating] ?: 0
                    val percentage = if (reviews.isNotEmpty()) (count.toFloat() / reviews.size * 100) else 0f
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$rating",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "$rating stars",
                            modifier = Modifier.size(12.dp),
                            tint = Color.Yellow
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        LinearProgressIndicator(
                            progress = percentage / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: Review,
    isFeatured: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(if (isFeatured) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with rating and featured badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rating stars
                    Row {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= review.rating) Icons.Filled.Star else Icons.Filled.FavoriteBorder,
                                contentDescription = "$star stars",
                                modifier = Modifier.size(16.dp),
                                tint = if (star <= review.rating) 
                                    Color.Yellow 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${review.rating}/5",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFeatured) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Featured",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = formatDate(review.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Reviewer info
            val reviewer = review.application?.reader
            val reviewerName = reviewer?.username 
                ?: "${reviewer?.firstName ?: ""} ${reviewer?.lastName ?: ""}".trim()
                .takeIf { it.isNotBlank() }
                ?: "Reviewer"
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reviewer profile picture
                val reviewerInitial = reviewer?.username?.firstOrNull()?.uppercase() ?: "R"
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
                
                Column {
                    Text(
                        text = reviewerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Review content
            val reviewType = review.reviewType ?: com.example.booknest.network.ReviewType.TEXT
            when (reviewType) {
                com.example.booknest.network.ReviewType.TEXT -> {
                    review.reviewContent?.let { content ->
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                        
                        if (review.wordCount != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${review.wordCount} words",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                com.example.booknest.network.ReviewType.LINK -> {
                    review.reviewUrls?.forEach { url ->
                        if (url.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            com.example.booknest.ui.components.ReviewLinkPreview(
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
                        com.example.booknest.ui.components.ReviewLinkPreview(
                            url = url,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Review metadata
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Review Type: ${(review.reviewType ?: com.example.booknest.network.ReviewType.TEXT).value.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!review.isPublic) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Private",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Private",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyReviewsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = "No Reviews",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No reviews yet!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Be the first to review this book.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
