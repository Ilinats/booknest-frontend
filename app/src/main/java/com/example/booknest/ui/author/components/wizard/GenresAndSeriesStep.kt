package com.example.booknest.ui.author.components.wizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.SeriesResponse

@Composable
fun GenresAndSeriesStep(
    selectedGenres: List<Int>,
    selectedSeries: SeriesResponse?,
    seriesOrder: String,
    mySeries: List<SeriesResponse>,
    genres: List<GenreResponse>,
    seriesOrderError: String? = null,
    onUpdate: (List<Int>, SeriesResponse?, String) -> Unit,
    onCreateSeries: (String, String) -> Unit,
    onShowCreateSeriesDialog: () -> Unit,
    showCreateSeriesDialog: Boolean,
    onDismissCreateSeriesDialog: () -> Unit,
    onValidationChange: ((String?) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Genres & Series",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Select Genres",
            style = MaterialTheme.typography.titleMedium
        )

        val sortedGenres = remember(genres, selectedGenres) {
            genres.sortedBy { genre ->
                if (selectedGenres.contains(genre.id)) 0 else 1
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedGenres) { genre ->
                val isSelected = selectedGenres.contains(genre.id)
                Card(
                    modifier = Modifier
                        .clickable {
                            val newSelection = if (isSelected) {
                                selectedGenres - genre.id
                            } else {
                                selectedGenres + genre.id
                            }
                            onUpdate(newSelection, selectedSeries, seriesOrder)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = genre.name,
                        modifier = Modifier.padding(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = "Select Series (Optional)",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Choose an existing series or create a new one:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onShowCreateSeriesDialog,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Create New Series",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Series")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selectedSeries == null,
                    onClick = { onUpdate(selectedGenres, null, seriesOrder) },
                    role = Role.RadioButton
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedSeries == null,
                onClick = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "No Series",
                style = MaterialTheme.typography.titleSmall
            )
        }

        if (mySeries.isNotEmpty()) {
            Text(
                text = "Existing Series:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Column(modifier = Modifier.selectableGroup()) {
                mySeries.forEach { series ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedSeries?.id == series.id,
                                onClick = { onUpdate(selectedGenres, series, seriesOrder) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSeries?.id == series.id,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = series.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                            series.description?.let { desc ->
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedSeries != null) {
            OutlinedTextField(
                value = seriesOrder,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        onUpdate(selectedGenres, selectedSeries, newValue)
                        onValidationChange?.invoke(validateSeriesOrder(newValue))
                    }
                },
                label = { Text("Order in Series") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = seriesOrderError != null,
                supportingText = seriesOrderError?.let {
                    {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } ?: {
                    Text("Optional: Minimum 1")
                }
            )
        }
    }

    if (showCreateSeriesDialog) {
        BookCreationCreateSeriesDialog(
            onDismiss = onDismissCreateSeriesDialog,
            onCreateSeries = { name: String, description: String ->
                onCreateSeries(name, description)
                onDismissCreateSeriesDialog()
            }
        )
    }
}

private fun validateSeriesOrder(order: String): String? {
    val trimmed = order.trim()
    return if (trimmed.isNotBlank()) {
        trimmed.toIntOrNull()?.let { num ->
            when {
                num < 1 -> "Series order must be at least 1"
                else -> null
            }
        }
    } else null
}
