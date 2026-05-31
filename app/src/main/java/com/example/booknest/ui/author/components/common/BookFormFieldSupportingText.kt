package com.example.booknest.ui.author.components.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** Shows supporting text only when there is a validation message (e.g. limit exceeded). */
fun bookFormFieldSupportingText(message: String?): (@Composable () -> Unit)? =
    message?.let { text ->
        {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
