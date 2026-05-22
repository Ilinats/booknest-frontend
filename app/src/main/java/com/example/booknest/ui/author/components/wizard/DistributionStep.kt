package com.example.booknest.ui.author.components.wizard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.validation.BookFormRules
import com.example.booknest.ui.author.components.common.AgeRating
import com.example.booknest.ui.author.components.common.bookFormFieldSupportingText
import com.example.booknest.ui.author.components.common.DistributionType

@Composable
fun DistributionStep(
    selectedAgeRating: AgeRating?,
    selectedDistributionType: DistributionType?,
    totalCopies: String,
    totalCopiesError: String? = null,
    onUpdate: (AgeRating?, DistributionType?, String) -> Unit,
    onValidationChange: ((String?) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Distribution Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Age Rating *",
            style = MaterialTheme.typography.titleMedium
        )

        Column(modifier = Modifier.selectableGroup()) {
            AgeRating.values().forEach { rating ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedAgeRating == rating,
                            onClick = { onUpdate(rating, selectedDistributionType, totalCopies) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedAgeRating == rating,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rating.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }

        Text(
            text = "Distribution Type *",
            style = MaterialTheme.typography.titleMedium
        )

        Column(modifier = Modifier.selectableGroup()) {
            DistributionType.values().forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedDistributionType == type,
                            onClick = { onUpdate(selectedAgeRating, type, totalCopies) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedDistributionType == type,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = type.name.lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }

        OutlinedTextField(
            value = totalCopies,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onUpdate(selectedAgeRating, selectedDistributionType, newValue)
                    onValidationChange?.invoke(BookFormRules.validateTotalCopies(newValue))
                }
            },
            label = { Text("Total Copies") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = totalCopiesError != null,
            supportingText = bookFormFieldSupportingText(totalCopiesError),
        )
    }
}
