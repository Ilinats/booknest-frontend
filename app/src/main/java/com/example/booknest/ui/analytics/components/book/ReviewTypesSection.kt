package com.example.booknest.ui.analytics.components.book

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.ReviewTypesResponse

@Composable
fun ReviewTypesSection(reviewTypes: ReviewTypesResponse) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Review Types",
            fontSize = 16.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Text Reviews",
                value = reviewTypes.text.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Link Reviews",
                value = reviewTypes.link.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
