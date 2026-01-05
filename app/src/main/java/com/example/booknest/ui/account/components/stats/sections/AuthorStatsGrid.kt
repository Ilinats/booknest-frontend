package com.example.booknest.ui.account.components.stats.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.ui.components.stats.StatCard
import com.example.booknest.ui.account.components.stats.StatItem


@SuppressLint("DefaultLocale")
@Composable
fun AuthorStatsGrid(
    stats: UserStatsDataResponse
) {
    val statItems = listOf(
        StatItem("Total Books", (stats.totalBooks ?: 0).toString(), Icons.Default.Menu),
        StatItem("Published Books", (stats.publishedBooks ?: 0).toString(), Icons.Default.Check),
        StatItem("Draft Books", (stats.draftBooks ?: 0).toString(), Icons.Default.Edit),
        StatItem("Total Applications", stats.totalApplications.toString(), Icons.Default.DateRange),
        StatItem(
            "Approved Applications",
            stats.approvedApplications.toString(),
            Icons.Default.CheckCircle
        ),
        StatItem("Pending Applications", stats.pendingApplications.toString(), Icons.Default.Face),
        StatItem("Total Reviews", (stats.totalReviews ?: 0).toString(), Icons.Default.Star),
        StatItem(
            "Average Rating",
            String.format("%.1f", stats.averageRating ?: 0.0),
            Icons.Default.Star
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(400.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statItems) { statItem ->
            StatCard(
                title = statItem.title,
                value = statItem.value,
                icon = statItem.icon
            )
        }
    }
}


