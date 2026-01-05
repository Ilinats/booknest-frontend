package com.example.booknest.ui.account.components.stats.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.ui.components.stats.StatCard
import com.example.booknest.ui.account.components.stats.StatItem

@Composable
fun ReaderStatsGrid(
    stats: UserStatsDataResponse
) {
    val statItems = listOf(
        StatItem("Total Applications", stats.totalApplications.toString(), Icons.Default.Menu),
        StatItem(
            "Approved Applications",
            stats.approvedApplications.toString(),
            Icons.Default.CheckCircle
        ),
        StatItem(
            "Pending Applications",
            stats.pendingApplications.toString(),
            Icons.Default.DateRange
        ),
        StatItem("Completed Reads", (stats.completedReads ?: 0).toString(), Icons.Default.Done),
        StatItem("Reviews Written", (stats.totalReviews ?: 0).toString(), Icons.Default.Star)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(250.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statItems) { statItem ->
            StatCard(
                title = statItem.title,
                value = statItem.value,
                icon = statItem.icon,
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}