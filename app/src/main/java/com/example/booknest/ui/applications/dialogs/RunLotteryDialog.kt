package com.example.booknest.ui.applications.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun RunLotteryDialog(
    availableCopies: Int,
    pendingCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Run Lottery Selection") },
        text = {
            Text(
                "This will randomly select $availableCopies reader(s) from $pendingCount pending application(s). " +
                    "Selected readers will be approved and others will be rejected. This action cannot be undone."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Run Lottery")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
