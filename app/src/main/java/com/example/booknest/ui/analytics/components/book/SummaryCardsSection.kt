package com.example.booknest.ui.analytics.components.book

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.BookAnalyticsSummaryResponse

@Composable
fun SummaryCardsSection(summary: BookAnalyticsSummaryResponse) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Reviews",
                value = summary.totalReviews.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Average Rating",
                value = String.format("%.1f", summary.averageRating),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Positive Feedback",
                value = "${summary.positiveFeedback}%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}
