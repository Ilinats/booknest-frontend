package com.example.booknest.ui.reviews.components.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.booknest.ui.reviews.utils.formatDate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun BookReferenceHeader(
    bookTitle: String,
    authorName: String,
    bookCover: String?,
    reviewDeadline: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (!bookCover.isNullOrBlank()) {
                        AsyncImage(
                            model = bookCover,
                            contentDescription = bookTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = "No cover",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = bookTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "by $authorName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            reviewDeadline?.let { deadline ->
                val (daysLeft, isUrgent, isCritical) = remember(deadline) {
                    try {
                        val inputFormat = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                            Locale.getDefault()
                        )
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
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val textColor = when {
                        isCritical -> MaterialTheme.colorScheme.onErrorContainer
                        isUrgent -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
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
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

