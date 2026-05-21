package com.example.booknest.ui.onboarding.components.personalinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.ui.components.models.UsernameStatus

@Composable
fun PersonalInfoUsernameField(
    username: String,
    onUsernameChange: (String) -> Unit,
    isCheckingUsername: Boolean,
    usernameStatus: UsernameStatus,
    usernameAvailable: Boolean?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Username *",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = { Text("Username", color = Color(0xFF757575)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(28.dp)
                ),
            singleLine = true,
            trailingIcon = {
                when {
                    isCheckingUsername -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    usernameStatus is UsernameStatus.ValidFormat && usernameAvailable == true -> {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Available",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    usernameStatus is UsernameStatus.ValidFormat && usernameAvailable == false -> {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Taken",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    usernameStatus is UsernameStatus.ValidFormat -> {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Valid",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    else -> {}
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        when (val status = usernameStatus) {
            is UsernameStatus.Idle -> Text(
                "3-50 characters, letters, numbers, _, ., -",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            is UsernameStatus.TooShort -> Text(
                "Username must be at least 3 characters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            is UsernameStatus.TooLong -> Text(
                "Username must be 50 characters or less",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            is UsernameStatus.InvalidFormat -> Text(
                "Invalid format. Use letters, numbers, _, ., -",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            is UsernameStatus.ValidFormat -> {
                when {
                    isCheckingUsername -> Text(
                        "Checking availability...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    usernameAvailable == false -> Text(
                        "Username is already taken",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    usernameAvailable == true -> Text(
                        "✓ Username is available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    else -> Text(
                        "✓ Format valid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
