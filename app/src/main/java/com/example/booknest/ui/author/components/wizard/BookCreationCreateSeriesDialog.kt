package com.example.booknest.ui.author.components.wizard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.validation.BookFormRules
import com.example.booknest.ui.author.components.common.bookFormFieldSupportingText

@Composable
fun BookCreationCreateSeriesDialog(
    onDismiss: () -> Unit,
    onCreateSeries: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Series") },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = BookFormRules.validateSeriesName(it)
                        descriptionError = BookFormRules.validateSeriesDescription(description)
                    },
                    label = { Text("Series Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = bookFormFieldSupportingText(nameError),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = BookFormRules.validateSeriesDescription(it)
                        nameError = BookFormRules.validateSeriesName(name)
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    isError = descriptionError != null,
                    supportingText = bookFormFieldSupportingText(descriptionError),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateSeries(name, description) },
                enabled = name.isNotBlank() &&
                    nameError == null &&
                    descriptionError == null
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
