package com.example.booknest.ui.applications

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.applications.components.content.BookApplicationDetailContent
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.BookViewModel
import com.example.booknest.viewmodel.ReviewViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@Composable
fun BookApplicationDetailScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    bookId: String,
    applicationViewModel: ApplicationViewModel = getViewModel(),
    bookViewModel: BookViewModel = getViewModel(),
    reviewViewModel: ReviewViewModel = getViewModel()
) {
    BookApplicationDetailContent(
        navController = navController,
        sessionManager = sessionManager,
        bookId = bookId,
        applicationViewModel = applicationViewModel,
        bookViewModel = bookViewModel,
        reviewViewModel = reviewViewModel
    )
}