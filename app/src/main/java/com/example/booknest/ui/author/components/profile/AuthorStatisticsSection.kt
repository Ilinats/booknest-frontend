package com.example.booknest.ui.author.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statItems = getStatItems(stats)

                statItems.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { (title, value) ->
                            AuthorStatCard(
                                title = title,
                                value = when {
                                    title == "Average Rating" -> String.format(
                                        "%.1f",
                                        (value as? Number)?.toDouble() ?: 0.0
                                    )
                                    title == "Approval Rate" -> "${(value as? Number)?.toInt() ?: 0}%"
                                    else -> value.toString()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getStatItems(stats: Any): List<Pair<String, Any>> {
    return try {
        val clazz = stats.javaClass
        listOf(
            "Total Books" to (getProperty<Int>(stats, "totalBooks") ?: 0),
            "Published Books" to (getProperty<Int>(stats, "publishedBooks") ?: 0),
            "Draft Books" to (getProperty<Int>(stats, "draftBooks") ?: 0),
            "Total Applications" to (getProperty<Int>(stats, "totalApplications") ?: 0),
            "Approval Rate" to (run {
                val totalApplications = getProperty<Int>(stats, "totalApplications") ?: 0
                val approvedApplications = getProperty<Int>(stats, "approvedApplications") ?: 0
                if (totalApplications > 0) {
                    ((approvedApplications.toDouble() / totalApplications) * 100).toInt()
                } else 0
            }),
            "Average Rating" to (getProperty<Double>(stats, "averageRating") ?: 0.0),
            "Total Reviews" to (getProperty<Int>(stats, "totalReviews") ?: 0)
        )
    } catch (e: Exception) {
        emptyList()
    }
}

private fun <T> getProperty(obj: Any, propertyName: String): T? {
    return try {
        val field = obj.javaClass.getDeclaredField(propertyName)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        field.get(obj) as? T
    } catch (e: Exception) {
        null
    }
}
