package com.example.booknest.ui.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.booknest.port.ToastNotifier
import com.example.booknest.ui.components.Toast
import com.example.booknest.ui.components.ToastMessage
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

/**
 * Collects [ToastNotifier.messages] (including download notifications) into the shared [Toast] overlay.
 */
@Composable
fun BookNestToastHost(
    toastNotifier: ToastNotifier = koinInject(),
    modifier: Modifier = Modifier
) {
    var currentToast by remember { mutableStateOf<ToastMessage?>(null) }

    LaunchedEffect(Unit) {
        toastNotifier.messages.collectLatest { message ->
            currentToast = message
        }
    }

    Toast(
        toastMessage = currentToast,
        onDismiss = { currentToast = null },
        modifier = modifier
    )
}
