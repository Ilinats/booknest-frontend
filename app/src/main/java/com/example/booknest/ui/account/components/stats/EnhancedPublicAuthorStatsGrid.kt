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
fun EnhancedPublicAuthorStatsGrid(stats: UserStatsDataResponse, followerCount: Int? = null) {
    val statItems = buildList {
        add("Active Books" to (stats.publishedBooks ?: 0))
        add("Total Reviews" to (stats.totalReviews ?: 0))
        add("Average Rating" to (stats.averageRating ?: 0.0))
        followerCount?.let { count ->
            add("Followers" to count)
        }
    }

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
                        value = if (title == "Average Rating") String.format(
                            "%.1f",
                            value
                        ) else value.toString(),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

