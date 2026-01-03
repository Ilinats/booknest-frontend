package com.example.booknest.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.ui.components.BackButton
import com.example.booknest.ui.theme.BackgroundWhite
import com.example.booknest.ui.theme.DarkNavyBlue
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
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onClick = { navController.popBackStack() })

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = screenTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavyBlue
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
                    CircularProgressIndicator(color = DarkNavyBlue)
                }
            } else if (currentBooks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No books found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
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

