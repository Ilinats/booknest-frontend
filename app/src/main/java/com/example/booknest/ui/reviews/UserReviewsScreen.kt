package com.example.booknest.ui.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.ReviewViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.collectLatest
import com.example.booknest.ui.reviews.components.card.ReviewCard
import com.example.booknest.ui.reviews.components.stats.ReviewStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewsScreen(
    navController: NavController,
    userId: String,
    userName: String? = null,
    sessionManager: SessionManager = koinInject(),
    reviewViewModel: ReviewViewModel = getViewModel()
) {
    val userReviews by reviewViewModel.userReviews.collectAsState()
    val isLoading by reviewViewModel.isLoading.collectAsState()
    LaunchedEffect(userId) {
        reviewViewModel.loadUserReviews(userId)
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.shadow(elevation = 4.dp)
            ) {
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-175).dp, y = (-175).dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-135).dp, y = (-135).dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 175.dp, y = 175.dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 135.dp, y = 135.dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = 16.dp)
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

                            items(userReviews) { review ->
                                ReviewCard(
                                    review = review,
                                    onBookClick = { bookId ->
                                        navController.navigate(Screen.BookDetails.createRoute(bookId))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
