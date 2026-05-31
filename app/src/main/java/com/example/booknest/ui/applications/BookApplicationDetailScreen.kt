package com.example.booknest.ui.applications

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.applications.components.content.BookApplicationDetailContent
import com.example.booknest.viewmodel.applications.BookApplicationViewModel
import com.example.booknest.viewmodel.books.BookDetailsViewModel
import com.example.booknest.viewmodel.analytics.ReviewViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@Composable
fun BookApplicationDetailScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    bookId: String,
    bookApplicationViewModel: BookApplicationViewModel = getViewModel(),
    bookDetailsViewModel: BookDetailsViewModel = getViewModel(),
    reviewViewModel: ReviewViewModel = getViewModel()
) {
    BookApplicationDetailContent(
        navController = navController,
        sessionManager = sessionManager,
        bookId = bookId,
        bookApplicationViewModel = bookApplicationViewModel,
        bookDetailsViewModel = bookDetailsViewModel,
        reviewViewModel = reviewViewModel
    )
}