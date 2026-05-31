package com.example.booknest.ui.author.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.ui.account.components.stats.AuthorStatsGridContent
import com.example.booknest.ui.account.components.stats.authorProfileStatItems
import com.example.booknest.ui.account.components.stats.authorProfileStatItemsFromMap

@Composable
fun AuthorStatisticsSection(stats: Any?) {
    if (stats == null) return
    
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
                text = "Statistics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val statItems = when (stats) {
                is UserStatsDataResponse -> authorProfileStatItems(stats)
                else -> authorProfileStatItemsFromMap(reflectStatsMap(stats))
            }

            if (statItems.isNotEmpty()) {
                AuthorStatsGridContent(statItems = statItems)
            }
        }
    }
}

private fun reflectStatsMap(stats: Any): Map<String, Any?> {
    return try {
        stats.javaClass.declaredFields.associate { field ->
            field.isAccessible = true
            field.name to field.get(stats)
        }
    } catch (_: Exception) {
        emptyMap()
    }
}
