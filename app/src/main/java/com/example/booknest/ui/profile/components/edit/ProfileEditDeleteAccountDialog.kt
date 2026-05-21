package com.example.booknest.ui.profile.components.edit

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.viewmodel.profile.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileEditDeleteAccountDialog(
    profileViewModel: ProfileViewModel,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Text(
                "Are you sure you want to delete your account? This action cannot be undone. All your data, applications, and reviews will be permanently deleted."
            )
        },
        confirmButton = {
            val scope = rememberCoroutineScope()
            var isDeleting by remember { mutableStateOf(false) }
            TextButton(
                onClick = {
                    if (!isDeleting) {
                        isDeleting = true
                        scope.launch {
                            profileViewModel.deleteAccount()
                            onDismiss()
                        }
                    }
                },
                enabled = !isDeleting,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.error,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
