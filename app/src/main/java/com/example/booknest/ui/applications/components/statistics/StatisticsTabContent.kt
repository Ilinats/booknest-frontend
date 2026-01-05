package com.example.booknest.ui.applications.components.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.data.session.SessionManager
import com.example.booknest.viewmodel.AnalyticsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun StatisticsTabContent(
    bookId: String,
    sessionManager: SessionManager,
    analyticsViewModel: AnalyticsViewModel = getViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        StatisticsTab(
            bookId = bookId,
            sessionManager = sessionManager,
            analyticsViewModel = analyticsViewModel
        )
    }
}
