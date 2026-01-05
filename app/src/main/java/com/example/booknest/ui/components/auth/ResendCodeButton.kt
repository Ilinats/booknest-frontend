package com.example.booknest.ui.components.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun ResendCodeButton(
    onResend: () -> Unit,
    cooldownSeconds: Int = 60,
    modifier: Modifier = Modifier
) {
    var timeLeft by remember { mutableStateOf(0) }
    var isEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else {
            isEnabled = true
        }
    }

    LaunchedEffect(Unit) {
        timeLeft = cooldownSeconds
        isEnabled = false
    }

    TextButton(
        onClick = {
            if (isEnabled) {
                onResend()
                timeLeft = cooldownSeconds
                isEnabled = false
            }
        },
        enabled = isEnabled,
        modifier = modifier
    ) {
        Text(
            text = if (isEnabled) "Resend code" else "Resend code in ${timeLeft}s",
            color = if (isEnabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

