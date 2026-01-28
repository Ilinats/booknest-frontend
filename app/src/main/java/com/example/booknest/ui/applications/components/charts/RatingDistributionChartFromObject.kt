package com.example.booknest.ui.applications.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.RatingDistributionResponse

@Composable
fun RatingDistributionChartFromObject(ratingDistribution: RatingDistributionResponse) {
    val ratings = listOf(5, 4, 3, 2, 1)
    val counts = listOf(
        ratingDistribution.`5`,
        ratingDistribution.`4`,
        ratingDistribution.`3`,
        ratingDistribution.`2`,
        ratingDistribution.`1`
    )
    val maxCount = counts.maxOrNull() ?: 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        ratings.forEachIndexed { index, rating ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                val barHeight = if (maxCount > 0) {
                    (counts[index].toFloat() / maxCount * 120).dp.coerceAtLeast(4.dp)
                } else {
                    4.dp
                }
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(barHeight)
                        .background(
                            when (rating) {
                                5 -> Color(0xFF4CAF50)
                                4 -> Color(0xFF8BC34A)
                                3 -> Color(0xFFFFC107)
                                2 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            },
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Text(
                        text = rating.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = counts[index].toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
