package com.example.booknest.ui.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.analytics.ReviewViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import androidx.compose.runtime.collectAsState
import com.example.booknest.ui.reviews.components.card.UserReviewCard
import com.example.booknest.ui.reviews.components.stats.ReviewStats
import com.example.booknest.ui.components.BackgroundDecoration

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
            BackgroundDecoration(modifier = Modifier.fillMaxSize())

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
                                UserReviewCard(
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
