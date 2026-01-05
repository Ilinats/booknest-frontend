package com.example.booknest.ui.account.components.stats.sections

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.UserStatsDataResponse

@Composable
fun AdditionalStatsInfo(
    stats: com.example.booknest.domain.model.response.UserStatsDataResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Additional Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (stats.userType == "author") {
                val approvalRate = if (stats.totalApplications > 0) {
                    (stats.approvedApplications.toDouble() / stats.totalApplications * 100)
                } else 0.0

                val publishRate = if (stats.totalBooks ?: 0 > 0) {
                    ((stats.publishedBooks ?: 0).toDouble() / (stats.totalBooks ?: 1) * 100)
                } else 0.0

                AdditionalInfoItem(
                    label = "Application Approval Rate",
                    value = String.format("%.1f%%", approvalRate)
                )

                AdditionalInfoItem(
                    label = "Publication Rate",
                    value = String.format("%.1f%%", publishRate)
                )

                if (stats.averageRating != null && stats.averageRating > 0) {
                    AdditionalInfoItem(
                        label = "Average Rating",
                        value = String.format("%.1f/5.0", stats.averageRating)
                    )
                }
            } else {
                val completionRate = if (stats.approvedApplications > 0) {
                    ((stats.completedReads ?: 0).toDouble() / stats.approvedApplications * 100)
                } else 0.0

                val reviewRate = if (stats.completedReads ?: 0 > 0) {
                    ((stats.totalReviews ?: 0).toDouble() / (stats.completedReads ?: 1) * 100)
                } else 0.0

                AdditionalInfoItem(
                    label = "Reading Completion Rate",
                    value = String.format("%.1f%%", completionRate)
                )

                AdditionalInfoItem(
                    label = "Review Submission Rate",
                    value = String.format("%.1f%%", reviewRate)
                )
            }
        }
    }
}

@Composable
fun AdditionalInfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}