package com.example.booknest.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.ui.books.components.list.BookItem
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.AuthorFollowViewModel
import com.example.booknest.viewmodel.BookViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun BookCategoryScreen(
    navController: NavController,
    category: String,
    bookViewModel: BookViewModel = getViewModel(),
    authorFollowViewModel: AuthorFollowViewModel = getViewModel()
) {
    val recommendedBooks by bookViewModel.recommendedBooks.collectAsState()
    val newReleases by bookViewModel.newReleases.collectAsState()
    val trendingBooks by bookViewModel.trendingBooks.collectAsState()
    val booksFromFollowedAuthors by authorFollowViewModel.booksFromFollowedAuthors.collectAsState()
    val isLoading by bookViewModel.isLoading.collectAsState()
    val followedAuthorsLoading by authorFollowViewModel.isLoading.collectAsState()

    val screenTitle = when (category) {
        "recommended" -> "Recommended Books"
        "new_releases" -> "New Releases"
        "followed_authors" -> "From Authors You Follow"
        "trending" -> "Trending This Week"
        else -> "Books"
    }

    val currentBooks = when (category) {
        "recommended" -> recommendedBooks
        "new_releases" -> newReleases
        "followed_authors" -> booksFromFollowedAuthors
        "trending" -> trendingBooks.map { it.book }
        else -> emptyList()
    }

    val currentIsLoading = when (category) {
        "followed_authors" -> followedAuthorsLoading
        else -> isLoading
    }

    LaunchedEffect(category) {
        when (category) {
            "recommended" -> bookViewModel.getRecommendedBooks()
            "new_releases" -> bookViewModel.getNewReleases()
            "followed_authors" -> authorFollowViewModel.loadBooksFromFollowedAuthors()
            "trending" -> bookViewModel.getTrendingBooks()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 175.dp, y = (-175).dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 135.dp, y = (-135).dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onClick = { navController.popBackStack() })

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = screenTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentIsLoading && currentBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (currentBooks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No books found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currentBooks) { book ->
                        BookItem(
                            book = book,
                            navController = navController,
                            isFullWidth = true
                        )
                    }
                }
            }
        }
    }
}

