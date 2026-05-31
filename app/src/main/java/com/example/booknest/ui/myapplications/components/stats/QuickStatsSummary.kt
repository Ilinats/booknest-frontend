package com.example.booknest.ui.myapplications.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.viewmodel.applications.ApplicationStats

@Composable
fun QuickStatsSummary(stats: ApplicationStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total\nBooks",
                    value = stats.total.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Approval Rate",
                    value = "${stats.approvalRate.toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Reviews This Month",
                    value = stats.reviewsThisMonth.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Pending Reviews",
                    value = stats.pendingReviews.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

