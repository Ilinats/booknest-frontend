package com.example.booknest.ui.books.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.BookResponse

@Composable
fun ApplicationFormDialog(
    book: BookResponse,
    onDismiss: () -> Unit,
    onSubmit: (String?) -> Unit
) {
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply for Review Copy") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("You are applying to review \"${book.title}\"")

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Why do you want to review this book? (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(message.ifBlank { null }) }) {
                Text("Submit Application")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

