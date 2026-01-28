package com.example.booknest.ui.analytics.components.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.RatingBreakdownItemResponse
import com.example.booknest.domain.model.response.RatingDistributionResponse
import com.example.booknest.viewmodel.AnalyticsViewModel

@Composable
fun RatingBreakdownChart(ratingBreakdown: List<RatingBreakdownItemResponse>) {
    val maxCount = ratingBreakdown.maxOfOrNull { it.count } ?: 1
    val minBarHeight = 8.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ratingBreakdown.sortedByDescending { it.rating }.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val barHeight = if (maxCount > 0) {
                        maxOf(minBarHeight, (item.count.toFloat() / maxCount * 120).dp)
                    } else {
                        minBarHeight
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(barHeight)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (item.rating) {
                                    5 -> MaterialTheme.colorScheme.primary
                                    4 -> MaterialTheme.colorScheme.secondary
                                    3 -> MaterialTheme.colorScheme.tertiary
                                    2 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                    )

                    Text(
                        text = item.count.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.rating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingDistributionChart(
    ratingDistribution: RatingDistributionResponse,
    analyticsViewModel: AnalyticsViewModel
) {
    val ratingList = analyticsViewModel.getRatingDistributionList(ratingDistribution)
    val maxCount = analyticsViewModel.getTopRatingCount(ratingDistribution)
    val minBarHeight = 8.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ratingList.sortedByDescending { it.first }.forEach { (rating, count) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val barHeight = if (maxCount > 0) {
                        maxOf(minBarHeight, (count.toFloat() / maxCount * 120).dp)
                    } else {
                        minBarHeight
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(barHeight)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (rating) {
                                    5 -> MaterialTheme.colorScheme.primary
                                    4 -> MaterialTheme.colorScheme.secondary
                                    3 -> MaterialTheme.colorScheme.tertiary
                                    2 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                    )

                    Text(
                        text = count.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = rating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
