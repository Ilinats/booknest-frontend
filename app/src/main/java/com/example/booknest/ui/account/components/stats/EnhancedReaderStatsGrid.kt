package com.example.booknest.ui.account.components.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.ui.components.stats.StatCard

@Composable
fun EnhancedReaderStatsGrid(
    stats: UserStatsDataResponse,
    profile: UserProfileResponse,
    favoriteGenres: List<GenreResponse> = emptyList()
) {
    val statItems = listOf(
        "Books Read" to (stats.completedReads ?: 0),
        "Reviews Written" to (stats.totalReviews ?: 0),
        "Total Applications" to stats.totalApplications,
        "Approved Applications" to stats.approvedApplications
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
                        value = value.toString(),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }

    if (favoriteGenres.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Favorite Genres",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            favoriteGenres.chunked(3).forEach { rowGenres ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowGenres.forEach { genre ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = genre.name,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

