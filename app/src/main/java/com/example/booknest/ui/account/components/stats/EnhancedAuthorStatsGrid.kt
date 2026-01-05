package com.example.booknest.ui.account.components.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.ui.components.stats.StatCard

@Composable
fun EnhancedAuthorStatsGrid(stats: UserStatsDataResponse) {
    val approvalRate = if (stats.totalApplications > 0) {
        (stats.approvedApplications.toDouble() / stats.totalApplications * 100).toInt()
    } else 0

    val statItems = listOf(
        "Total Books" to (stats.totalBooks ?: 0),
        "Published Books" to (stats.publishedBooks ?: 0),
        "Draft Books" to (stats.draftBooks ?: 0),
        "Total Applications" to stats.totalApplications,
        "Approval Rate" to approvalRate,
        "Average Rating" to (stats.averageRating ?: 0.0),
        "Total Reviews" to (stats.totalReviews ?: 0)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (title, value) ->
                    StatCard(
                        title = title,
                        color = MaterialTheme.colorScheme.surface,
                        value = when {
                            title == "Average Rating" -> String.format(
                                "%.1f",
                                (value as? Number)?.toDouble() ?: 0.0
                            )

                            title == "Approval Rate" -> "${(value as? Number)?.toInt() ?: 0}%"
                            else -> value.toString()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

