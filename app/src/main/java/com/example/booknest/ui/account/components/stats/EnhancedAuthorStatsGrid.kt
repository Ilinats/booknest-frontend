package com.example.booknest.ui.account.components.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.ui.components.stats.StatCard

@Composable
fun EnhancedAuthorStatsGrid(stats: UserStatsDataResponse) {
    AuthorStatsGridContent(statItems = authorProfileStatItems(stats))
}

@Composable
internal fun AuthorStatsGridContent(statItems: List<Pair<String, Any>>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (title, value) ->
                    StatCard(
                        title = title,
                        color = MaterialTheme.colorScheme.surface,
                        value = formatAuthorStatValue(title, value),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}
