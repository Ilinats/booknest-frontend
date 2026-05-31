package com.example.booknest.ui.author.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.BookResponse

fun hasActionNeeded(
    pendingApplications: Int,
    overdueReviews: Int,
    booksWithDeadline: List<BookResponse>,
): Boolean = pendingApplications > 0 ||
    overdueReviews > 0 ||
    booksWithDeadline.isNotEmpty()

@Composable
fun ActionNeededSection(
    pendingApplications: Int,
    overdueReviews: Int,
    booksWithDeadline: List<BookResponse>
) {
    if (!hasActionNeeded(pendingApplications, overdueReviews, booksWithDeadline)) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
        ,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE5E5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Action Needed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }

            if (pendingApplications > 0) {
                ActionItem(
                    title = "Pending Applications",
                    count = pendingApplications
                )
            }

            if (overdueReviews > 0) {
                ActionItem(
                    title = "Overdue Reviews",
                    count = overdueReviews
                )
            }

            if (booksWithDeadline.isNotEmpty()) {
                ActionItem(
                    title = "Books with Deadline Approaching",
                    count = booksWithDeadline.size
                )
            }
        }
    }
}

@Composable
fun ActionItem(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$title: $count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575)
        )
    }
}
