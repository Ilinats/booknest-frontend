package com.example.booknest.ui.applications.components.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.analytics.components.book.BookAnalyticsContent
import com.example.booknest.viewmodel.analytics.AnalyticsViewModel
import com.example.booknest.presentation.common.UiState
import org.koin.androidx.compose.getViewModel

@Composable
@Suppress("UNUSED_PARAMETER")
fun StatisticsTab(
    bookId: String,
    sessionManager: SessionManager,
    analyticsViewModel: AnalyticsViewModel = getViewModel()
) {
    val analyticsState by analyticsViewModel.bookAnalyticsState.collectAsState()

    LaunchedEffect(bookId) {
        analyticsViewModel.loadDetailedBookAnalytics(bookId)
    }

    when (val state = analyticsState) {
        is UiState.Loading,
        is UiState.Idle -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { analyticsViewModel.loadDetailedBookAnalytics(bookId) }) {
                    Text("Retry")
                }
            }
        }

        is UiState.Success -> {
            BookAnalyticsContent(
                analytics = state.data,
                analyticsViewModel = analyticsViewModel
            )
        }
    }
}
