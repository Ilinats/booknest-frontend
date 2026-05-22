package com.example.booknest.ui.author.components.series

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.validation.BookFormRules
import com.example.booknest.ui.author.components.common.bookFormFieldSupportingText

@Composable
fun CreateSeriesDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Series") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    isError = descriptionError != null,
                    supportingText = bookFormFieldSupportingText(descriptionError),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), description.trim().takeIf { it.isNotBlank() }) },
                enabled = name.trim().isNotBlank() && nameError == null && descriptionError == null,
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
