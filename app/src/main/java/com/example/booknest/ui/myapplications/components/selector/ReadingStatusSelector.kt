package com.example.booknest.ui.myapplications.components.selector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.viewmodel.applications.ReadingStatus
import java.util.Locale

@Composable
fun ReadingStatusSelector(
    currentStatus: ReadingStatus,
    onStatusChange: (ReadingStatus) -> Unit
) {
    val statuses = ReadingStatus.values()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Reading Status",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statuses.forEach { status ->
                FilterChip(
                    onClick = { onStatusChange(status) },
                    label = {
                        Text(
                            text = status.value.replace("_", " ").replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = currentStatus == status,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

