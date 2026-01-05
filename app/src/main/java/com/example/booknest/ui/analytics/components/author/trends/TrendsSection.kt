package com.example.booknest.ui.author.components.analytics.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.AuthorTrendsResponse
import com.example.booknest.domain.model.response.MonthlyDataResponse
import com.example.booknest.viewmodel.AnalyticsViewModel

@Composable
fun TrendsSection(
    trends: AuthorTrendsResponse,
    analyticsViewModel: AnalyticsViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Trends (Last 6 Months)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            TrendChart(
                title = "Monthly Applications",
                data = trends.monthlyApplications,
                color = MaterialTheme.colorScheme.primary,
                analyticsViewModel = analyticsViewModel
            )

            TrendChart(
                title = "Monthly Reviews",
                data = trends.monthlyReviews,
                color = MaterialTheme.colorScheme.primary,
                analyticsViewModel = analyticsViewModel
            )
        }
    }
}

@Composable
fun TrendChart(
    title: String,
    data: List<MonthlyDataResponse>,
    color: Color,
    analyticsViewModel: AnalyticsViewModel,
    isPercentage: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        if (data.isEmpty()) {
            Text(
                text = "No data available",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val maxCount = data.maxOfOrNull { it.numericValue } ?: 1

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.take(6).forEach { monthData ->
                    val value = monthData.numericValue
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(if (maxCount > 0) (value.toFloat() / maxCount * 80).dp else 0.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )

                        Text(
                            text = analyticsViewModel.formatMonth(monthData.month),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (isPercentage) "$value%" else value.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}
