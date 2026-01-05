package com.example.booknest.ui.account.components.stats.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.UserStatsDataResponse

@Composable
fun GenresBreakdownSection(
    stats: com.example.booknest.domain.model.response.UserStatsDataResponse
) {
    val genresBreakdown = stats.genresBreakdown
    if (genresBreakdown != null && genresBreakdown.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Genres Breakdown",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Books read by genre",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val sortedGenres =
                    genresBreakdown.toList().sortedByDescending { (_, count) -> count }
                val maxCount = sortedGenres.maxOfOrNull { (_, count) -> count } ?: 1

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sortedGenres.take(10).forEach { (genre, count) ->
                        GenreBreakdownItem(
                            genre = genre,
                            count = count,
                            maxCount = maxCount
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenreBreakdownItem(
    genre: String,
    count: Int,
    maxCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = genre,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count ${if (count == 1) "book" else "books"}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (maxCount > 0) count.toFloat() / maxCount else 0f)
                    .fillMaxHeight()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

