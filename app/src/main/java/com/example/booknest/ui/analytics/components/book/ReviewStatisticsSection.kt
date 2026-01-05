package com.example.booknest.ui.analytics.components.book

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.ReviewAnalyticsResponse
import com.example.booknest.viewmodel.AnalyticsViewModel

@Composable
fun ReviewStatisticsSection(
    reviewStatistics: ReviewAnalyticsResponse,
    analyticsViewModel: AnalyticsViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review Statistics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Average Rating",
                        value = "${reviewStatistics.averageRating}/5",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Positive Feedback",
                        value = "${reviewStatistics.positiveFeedback}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Reviews",
                        value = reviewStatistics.totalReviews.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Avg Word Count",
                        value = "${reviewStatistics.averageWordCount}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                Text(
                    text = "Rating Distribution",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (reviewStatistics.ratingBreakdown != null && reviewStatistics.ratingBreakdown.isNotEmpty()) {
                    RatingBreakdownChart(ratingBreakdown = reviewStatistics.ratingBreakdown)
                } else {
                    RatingDistributionChart(
                        ratingDistribution = reviewStatistics.ratingDistribution,
                        analyticsViewModel = analyticsViewModel
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                ReviewTypesSection(reviewTypes = reviewStatistics.reviewTypes)
            }
        }
    }
}
