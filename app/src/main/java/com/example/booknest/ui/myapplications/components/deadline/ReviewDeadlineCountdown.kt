package com.example.booknest.ui.myapplications.components.deadline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.ui.myapplications.utils.formatDate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun ReviewDeadlineCountdown(deadline: String) {
    val (daysLeft, isUrgent, isCritical) = remember(deadline) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val deadlineDate = inputFormat.parse(deadline) ?: Date()
            val now = Date()
            val diff = deadlineDate.time - now.time
            val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()

            Triple(days, days <= 7 && days >= 0, days <= 3 && days >= 0)
        } catch (e: Exception) {
            Triple(null, false, false)
        }
    }

    if (daysLeft != null) {
        val backgroundColor = when {
            isCritical -> MaterialTheme.colorScheme.errorContainer
            isUrgent -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

        val textColor = when {
            isCritical -> MaterialTheme.colorScheme.onErrorContainer
            isUrgent -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isCritical) Icons.Filled.Warning else Icons.Filled.Schedule,
                        contentDescription = "Deadline",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Review Deadline",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Text(
                            text = formatDate(deadline),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor
                        )
                    }
                }

                Text(
                    text = when {
                        daysLeft < 0 -> "Deadline passed"
                        daysLeft == 0 -> "Due today!"
                        daysLeft == 1 -> "1 day left"
                        else -> "$daysLeft days left"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

