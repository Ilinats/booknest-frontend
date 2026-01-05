package com.example.booknest.ui.author.components.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DateRangeSelector(
    selectedDateRange: DateRangeOption,
    onDateRangeChange: (DateRangeOption) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Date Range",
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val sortedDateRanges = remember(selectedDateRange) {
                DateRangeOption.values().toList().sortedBy { range ->
                    if (range == selectedDateRange) 0 else 1
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedDateRanges) { range ->
                    FilterChip(
                        selected = selectedDateRange == range,
                        onClick = { onDateRangeChange(range) },
                        label = { Text(range.displayName) }
                    )
                }
            }
        }
    }
}

enum class DateRangeOption(val displayName: String, val apiValue: String) {
    LAST_7_DAYS("Last 7 Days", "last_7_days"),
    LAST_30_DAYS("Last 30 Days", "last_30_days"),
    LAST_90_DAYS("Last 90 Days", "last_90_days"),
    YEAR("Year", "year"),
    ALL_TIME("All Time", "all_time")
}
