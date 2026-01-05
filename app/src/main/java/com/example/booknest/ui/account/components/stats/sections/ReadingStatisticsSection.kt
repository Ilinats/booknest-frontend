package com.example.booknest.ui.account.components.stats.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.ui.account.components.stats.utils.formatReadingTime
import com.example.booknest.ui.account.components.stats.utils.formatNumber

@Composable
fun ReadingStatisticsSection(
    stats: UserStatsDataResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reading Statistics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReadingStatCard(
                    title = "This Month",
                    value = (stats.completedReadsThisMonth ?: 0).toString(),
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
                ReadingStatCard(
                    title = "This Year",
                    value = (stats.completedReadsThisYear ?: 0).toString(),
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
                ReadingStatCard(
                    title = "All Time",
                    value = (stats.completedReads ?: 0).toString(),
                    icon = Icons.Default.History,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (stats.pagesRead != null && stats.pagesRead > 0) {
                    ReadingStatCard(
                        title = "Pages Read",
                        value = formatNumber(stats.pagesRead),
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (stats.averageReadingTime != null && stats.averageReadingTime > 0) {
                    ReadingStatCard(
                        title = "Avg Reading Time",
                        value = formatReadingTime(stats.averageReadingTime),
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (stats.reviewCompletionRate != null) {
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Review Completion Rate",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Percentage of completed reads with reviews",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = String.format("%.1f%%", stats.reviewCompletionRate),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ReadingStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
