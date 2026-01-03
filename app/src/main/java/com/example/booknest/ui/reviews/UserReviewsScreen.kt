package com.example.booknest.ui.reviews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.ReviewViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewsScreen(
    navController: NavController,
    userId: String,
    userName: String? = null,
    sessionManager: SessionManager = koinInject(),
    reviewViewModel: ReviewViewModel = getViewModel()
) {
    val currentUser by sessionManager.currentUser.collectAsState()
    val userReviews by reviewViewModel.userReviews.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()
    LaunchedEffect(userId) {
        reviewViewModel.loadUserReviews(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${userName ?: "User"}'s Reviews",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && userReviews.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                userReviews.isEmpty() -> {
                    Text(
                        text = "No reviews yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ReviewStats(reviews = userReviews)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        val featuredReviews = userReviews.filter { it.isFeatured }
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
                                    isFeatured = true,
                                    currentUser = currentUser,
                                    onBookClick = { bookId ->
                                        val isAuthor = currentUser?.userType == "author"
                                        val isMyBook = isAuthor && review.application?.book?.authorId == currentUser?.id
                                        if (isMyBook) {
                                            navController.navigate(Screen.BookApplicationDetail.createRoute(bookId))
                                        } else {
                                            navController.navigate(Screen.BookDetails.createRoute(bookId))
                                        }
                                    }
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

                        val regularReviews = userReviews.filter { !it.isFeatured }
                        items(regularReviews) { review ->
                            ReviewCard(
                                review = review,
                                isFeatured = false,
                                currentUser = currentUser,
                                onBookClick = { bookId ->
                                    val isAuthor = currentUser?.userType == "author"
                                    val isMyBook = isAuthor && review.application?.book?.authorId == currentUser?.id
                                    if (isMyBook) {
                                        navController.navigate(Screen.BookApplicationDetail.createRoute(bookId))
                                    } else {
                                        navController.navigate(Screen.BookDetails.createRoute(bookId))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: ReviewResponse,
    isFeatured: Boolean,
    currentUser: UserResponse? = null,
    onBookClick: (String) -> Unit
) {
    val bookTitle = review.application?.book?.title 
        ?: review.application?.bookTitle 
        ?: "Unknown Book"
    val isAuthor = currentUser?.userType == "author"
    val isMyBook = isAuthor && review.application?.book?.authorId == currentUser?.id
    val isClickable = !isMyBook && review.application?.bookId != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFeatured) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bookTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (isClickable) {
                                Modifier.clickable { 
                                    review.application?.bookId?.let { onBookClick(it) }
                                }
                            } else {
                                Modifier
                            }
                        ),
                    color = if (isClickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(review.rating) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Star,
                            contentDescription = "Star",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            review.reviewContent?.let { content ->
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = formatDate(review.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return "Unknown date"
    return try {
        val inputFormat = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            java.util.Locale.getDefault()
        )
        val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

