package com.example.booknest.ui.books.components.application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class ApplicationStatusMessageVariant {
    Success,
    Error,
    Warning,
    Info,
    Neutral,
}

@Composable
fun ApplicationStatusMessageCard(
    title: String,
    message: String,
    variant: ApplicationStatusMessageVariant,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val (containerColor, contentColor, defaultIcon) = variantColors(variant)
    val displayIcon = icon ?: defaultIcon

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = displayIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.9f),
                    )
                }
            }
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (variant) {
                            ApplicationStatusMessageVariant.Error,
                            ApplicationStatusMessageVariant.Warning,
                            -> MaterialTheme.colorScheme.error

                            else -> MaterialTheme.colorScheme.primary
                        },
                        contentColor = when (variant) {
                            ApplicationStatusMessageVariant.Error,
                            ApplicationStatusMessageVariant.Warning,
                            -> MaterialTheme.colorScheme.onError

                            else -> MaterialTheme.colorScheme.onPrimary
                        },
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun variantColors(
    variant: ApplicationStatusMessageVariant,
): Triple<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color, ImageVector> {
    val colorScheme = MaterialTheme.colorScheme
    return when (variant) {
        ApplicationStatusMessageVariant.Success -> Triple(
            colorScheme.primaryContainer,
            colorScheme.onPrimaryContainer,
            Icons.Filled.CheckCircle,
        )
        ApplicationStatusMessageVariant.Error -> Triple(
            colorScheme.errorContainer,
            colorScheme.onErrorContainer,
            Icons.Filled.Cancel,
        )
        ApplicationStatusMessageVariant.Warning -> Triple(
            colorScheme.tertiaryContainer,
            colorScheme.onTertiaryContainer,
            Icons.Filled.Warning,
        )
        ApplicationStatusMessageVariant.Info -> Triple(
            colorScheme.secondaryContainer,
            colorScheme.onSecondaryContainer,
            Icons.Filled.HourglassTop,
        )
        ApplicationStatusMessageVariant.Neutral -> Triple(
            colorScheme.surfaceVariant,
            colorScheme.onSurfaceVariant,
            Icons.Filled.Undo,
        )
    }
}

@Composable
fun ApplicationFullyBookedMessageCard(modifier: Modifier = Modifier) {
    ApplicationStatusMessageCard(
        title = "Fully booked",
        message = "All review copies have been claimed. Check back later or browse other books.",
        variant = ApplicationStatusMessageVariant.Warning,
        icon = Icons.Filled.Block,
        modifier = modifier,
    )
}

@Composable
fun ApplicationDeadlinePassedMessageCard(modifier: Modifier = Modifier) {
    ApplicationStatusMessageCard(
        title = "Applications closed",
        message = "The application deadline for this book has passed. New applications are no longer accepted.",
        variant = ApplicationStatusMessageVariant.Warning,
        modifier = modifier,
    )
}

@Composable
fun ApplicationApprovedMessageCard(modifier: Modifier = Modifier) {
    ApplicationStatusMessageCard(
        title = "Application approved",
        message = "Your application was accepted. Open My Applications to access your copy and update your reading status.",
        variant = ApplicationStatusMessageVariant.Success,
        modifier = modifier,
    )
}

@Composable
fun ApplicationRejectedMessageCard(modifier: Modifier = Modifier) {
    ApplicationStatusMessageCard(
        title = "Application not accepted",
        message = "The author did not select your application for this book. You can still discover other titles in Browse.",
        variant = ApplicationStatusMessageVariant.Error,
        modifier = modifier,
    )
}

@Composable
fun ApplicationWithdrawnMessageCard(modifier: Modifier = Modifier) {
    ApplicationStatusMessageCard(
        title = "Application withdrawn",
        message = "You withdrew your application. You can apply again if copies are still available.",
        variant = ApplicationStatusMessageVariant.Neutral,
        modifier = modifier,
    )
}

@Composable
fun ApplicationPendingMessageCard(modifier: Modifier = Modifier) {
    ApplicationStatusMessageCard(
        title = "Application pending",
        message = "Your application is with the author. You will be notified when they make a decision.",
        variant = ApplicationStatusMessageVariant.Info,
        modifier = modifier,
    )
}

@Composable
fun EmailVerificationRequiredMessageCard(
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ApplicationStatusMessageCard(
        title = "Verify your email",
        message = "Email verification is required before you can apply for books.",
        variant = ApplicationStatusMessageVariant.Warning,
        modifier = modifier,
        actionLabel = "Verify email",
        onAction = onVerifyClick,
    )
}

@Composable
fun ShippingAddressRequiredMessageCard(
    onAddAddressClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ApplicationStatusMessageCard(
        title = "Shipping address required",
        message = "Add a delivery address in your profile to apply for physical review copies.",
        variant = ApplicationStatusMessageVariant.Warning,
        modifier = modifier,
        actionLabel = "Add address",
        onAction = onAddAddressClick,
    )
}
