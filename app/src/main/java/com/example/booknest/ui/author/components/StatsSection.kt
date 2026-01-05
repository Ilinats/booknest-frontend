package com.example.booknest.ui.author.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.booknest.viewmodel.AuthorViewModel
import com.example.booknest.ui.components.stats.StatCard

@Composable
fun QuickStatsSection(quickStats: AuthorViewModel.QuickStats) {
    Column {
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Books",
                value = quickStats.totalBooks.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Active Books",
                value = quickStats.activeBooks.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Pending Applications",
                value = quickStats.pendingApplications.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Avg. Response Time",
                value = quickStats.avgResponseTime,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Reviews",
                value = quickStats.totalReviews.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Average Rating",
                value = String.format("%.1f", quickStats.averageRating),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PerformanceSummarySection(
    applicationsThisMonth: Int,
    approvalRate: Int,
    reviewCompletionRate: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
        ,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Performance Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PerformanceMetric(
                    label = "This Month's Applications",
                    value = applicationsThisMonth.toString(),
                    modifier = Modifier.weight(1f)
                )
                PerformanceMetric(
                    label = "Approval Rate",
                    value = "$approvalRate%",
                    modifier = Modifier.weight(1f)
                )
                PerformanceMetric(
                    label = "Review Completion",
                    value = "$reviewCompletionRate%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PerformanceMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF757575),
            modifier = Modifier.padding(top = 2.dp),
            textAlign = TextAlign.Center
        )
    }
}
