package com.example.booknest.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS,
    ERROR,
    INFO,
    DOWNLOAD_STARTED,
    DOWNLOAD_COMPLETED
}

data class ToastMessage(
    val message: String,
    val type: ToastType
)

@Composable
fun Toast(
    toastMessage: ToastMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = toastMessage != null && toastMessage.message.isNotBlank()

    LaunchedEffect(toastMessage) {
        val current = toastMessage ?: return@LaunchedEffect
        if (current.message.isBlank()) return@LaunchedEffect
        val duration = when (current.type) {
            ToastType.DOWNLOAD_STARTED -> 3000L
            ToastType.DOWNLOAD_COMPLETED -> 4000L
            ToastType.SUCCESS -> 4000L
            ToastType.ERROR -> 5000L
            ToastType.INFO -> 4000L
        }
        delay(duration)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier.fillMaxWidth()
    ) {
        val (backgroundColor, iconColor, icon) = when (toastMessage?.type) {
            ToastType.SUCCESS -> Triple(
                Color(0xFFE8F5E9),
                Color(0xFF2E7D32),
                Icons.Filled.CheckCircle
            )
            ToastType.ERROR -> Triple(
                Color(0xFFFFF1F1),
                Color(0xFFD32F2F),
                Icons.Filled.Error
            )
            ToastType.INFO -> Triple(
                Color(0xFFE3F2FD),
                Color(0xFF1976D2),
                Icons.Filled.Info
            )
            ToastType.DOWNLOAD_STARTED -> Triple(
                Color(0xFFE3F2FD),
                Color(0xFF1976D2),
                Icons.Filled.Download
            )
            ToastType.DOWNLOAD_COMPLETED -> Triple(
                Color(0xFFE8F5E9),
                Color(0xFF2E7D32),
                Icons.Filled.CheckCircle
            )
            null -> Triple(
                Color(0xFFE3F2FD),
                Color(0xFF1976D2),
                Icons.Filled.Info
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = toastMessage?.message ?: "",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242),
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF424242),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
