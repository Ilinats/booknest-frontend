package com.example.booknest.ui.books.components.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AgeRatingFilter(
    selectedAgeRating: String?,
    onAgeRatingSelected: (String?) -> Unit
) {
    val ageRatings = listOf("all", "13+", "16+", "18+")
    val ageRatingDisplayNames = mapOf(
        "all" to "All Ages",
        "13+" to "13+",
        "16+" to "16+",
        "18+" to "18+"
    )
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Age Rating",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedAgeRating?.let { ageRatingDisplayNames[it] ?: it }
                    ?: "Any",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Any") },
                    onClick = {
                        onAgeRatingSelected(null)
                        expanded = false
                    }
                )
                ageRatings.forEach { rating ->
                    DropdownMenuItem(
                        text = { Text(ageRatingDisplayNames[rating] ?: rating) },
                        onClick = {
                            onAgeRatingSelected(rating)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

