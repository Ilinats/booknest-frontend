package com.example.booknest.ui.analytics.components.book

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.ReviewPerformanceResponse

@Composable
fun ReviewPerformanceSection(reviewPerformance: ReviewPerformanceResponse) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review Performance",
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
                        title = "Submission Rate",
                        value = "${reviewPerformance.reviewSubmissionRate}%",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completion Rate",
                        value = "${reviewPerformance.reviewCompletionRate}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                reviewPerformance.averageReviewTime?.let { time ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Avg Review Time",
                            value = String.format("%.1f days", time),
                            modifier = Modifier.weight(1f)
                        )
                        reviewPerformance.averageWordCount?.let { wordCount ->
                            StatCard(
                                title = "Avg Word Count",
                                value = "$wordCount",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } ?: run {
                    reviewPerformance.averageWordCount?.let { wordCount ->
                        StatCard(
                            title = "Avg Word Count",
                            value = "$wordCount",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
