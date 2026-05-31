package com.example.booknest.ui.author.components.wizard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.ui.author.components.common.AgeRating
import com.example.booknest.ui.author.components.common.DistributionType
import com.example.booknest.ui.author.components.common.SelectionMethod
import com.example.booknest.utils.BookDateUtils

@Composable
fun PreviewStep(
    title: String,
    shortDescription: String,
    fullDescription: String,
    pageCount: String,
    ageRating: AgeRating?,
    distributionType: DistributionType?,
    totalCopies: String,
    genres: List<Int>,
    genreList: List<GenreResponse>,
    series: SeriesResponse?,
    seriesOrder: String,
    applicationDeadline: String?,
    reviewDeadline: String?,
    selectionMethod: SelectionMethod?,
    selectionCriteria: String,
    hasCoverImage: Boolean,
    hasBookFile: Boolean
) {
    val formattedApplicationDeadline = remember(applicationDeadline) {
        applicationDeadline?.let { BookDateUtils.formatDateOnlyForDisplay(it) } ?: "Not set"
    }

    val formattedReviewDeadline = remember(reviewDeadline) {
        reviewDeadline?.let { BookDateUtils.formatDateOnlyForDisplay(it) } ?: "Not set"
    }

    val selectedGenreNames = remember(genres, genreList) {
        genres.mapNotNull { genreId ->
            genreList.find { it.id == genreId }?.name
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Preview & Publish",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Required Items Checklist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ChecklistItem(
                    label = "Title",
                    isComplete = title.isNotBlank()
                )
                ChecklistItem(
                    label = "Age Rating",
                    isComplete = ageRating != null
                )
                ChecklistItem(
                    label = "Distribution Type",
                    isComplete = distributionType != null
                )
                ChecklistItem(
                    label = "Application Deadline",
                    isComplete = applicationDeadline != null
                )
                if (distributionType == DistributionType.DIGITAL || distributionType == DistributionType.BOTH) {
                    ChecklistItem(
                        label = "Book File (Required for Digital)",
                        isComplete = hasBookFile
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Book Preview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                Text(
                    text = title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                if (shortDescription.isNotBlank()) {
                    Text(
                        text = shortDescription,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (fullDescription.isNotBlank()) {
                    Text(
                        text = "Full Description:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = fullDescription,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Divider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (pageCount.isNotBlank()) {
                        PreviewDetail("Page Count", pageCount)
                    }

                    ageRating?.let { rating ->
                        PreviewDetail("Age Rating", rating.displayName)
                    }

                    distributionType?.let { type ->
                        PreviewDetail("Distribution Type", type.displayName)
                    }

                    if (totalCopies.isNotBlank()) {
                        PreviewDetail("Total Copies", totalCopies)
                    }

                    if (selectedGenreNames.isNotEmpty()) {
                        PreviewDetail("Genres", selectedGenreNames.joinToString(", "))
                    }

                    series?.let { s ->
                        val seriesText = if (seriesOrder.isNotBlank()) {
                            "${s.name} (#$seriesOrder)"
                        } else {
                            s.name
                        }
                        PreviewDetail("Series", seriesText)
                    }

                    PreviewDetail("Application Deadline", formattedApplicationDeadline)

                    if (reviewDeadline != null) {
                        PreviewDetail("Review Deadline", formattedReviewDeadline)
                    }

                    selectionMethod?.let { method ->
                        PreviewDetail("Selection Method", method.displayName)
                    }

                    if (selectionCriteria.isNotBlank()) {
                        PreviewDetail("Selection Criteria", selectionCriteria)
                    }

                    PreviewDetail("Cover Image", if (hasCoverImage) "Uploaded" else "Not uploaded")

                    if (distributionType == DistributionType.DIGITAL || distributionType == DistributionType.BOTH) {
                        PreviewDetail(
                            "Book File",
                            if (hasBookFile) "Uploaded" else "Not uploaded (Required)"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistItem(label: String, isComplete: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            if (isComplete) Icons.Filled.Check else Icons.Filled.Info,
            contentDescription = if (isComplete) "Complete" else "Incomplete",
            modifier = Modifier.size(20.dp),
            tint = if (isComplete)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isComplete)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun PreviewDetail(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
