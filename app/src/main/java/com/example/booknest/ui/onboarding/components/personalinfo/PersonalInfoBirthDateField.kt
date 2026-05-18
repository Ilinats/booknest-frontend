package com.example.booknest.ui.onboarding.components.personalinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun PersonalInfoBirthDateField(
    formattedDate: String,
    birthDate: String?,
    isBirthDateValid: Boolean,
    firstName: String,
    onOpenDatePicker: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Birth Date *",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        OutlinedTextField(
            value = formattedDate,
            onValueChange = { },
            placeholder = { Text("Birthday", color = Color(0xFF757575)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(28.dp)
                )
                .clickable { onOpenDatePicker() },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = onOpenDatePicker) {
                    Icon(
                        Icons.Filled.CalendarToday,
                        contentDescription = "Select Date",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
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
        when {
            birthDate == null && firstName.isNotBlank() -> Text(
                "Birth date is required",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            birthDate != null && !isBirthDateValid -> {
                val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")
                val errorText = if (datePattern.matches(birthDate ?: "")) {
                    "You must be at least 10 years old"
                } else {
                    "Invalid date format (YYYY-MM-DD)"
                }
                Text(
                    errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            isBirthDateValid -> Text(
                "✓ Valid birth date",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            else -> Text(
                "Select your birth date (YYYY-MM-DD, must be at least 10 years old)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
