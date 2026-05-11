package com.example.booknest.ui.auth.components.dialogs

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.viewmodel.auth.PasswordResetViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onEmailSubmitted: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val passwordResetViewModel: PasswordResetViewModel = getViewModel()
    val uiState by passwordResetViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            errorMessage = error
        }
    }

    LaunchedEffect(uiState.isCodeSent) {
        if (uiState.isCodeSent && email.isNotBlank()) {
            onEmailSubmitted(email)
            passwordResetViewModel.clearCodeSentState()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter your email address and we'll send you a code to reset your password.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    shape = RoundedCornerShape(24.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email)
                            .matches()
                    ) {
                        errorMessage = null
                        passwordResetViewModel.resendResetCode(email)
                    } else {
                        errorMessage = "Please enter a valid email address"
                    }
                },
                enabled = !uiState.isLoading && email.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send Code")
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

