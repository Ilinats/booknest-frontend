package com.example.booknest.ui.reviews.components.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun EnhancedRatingSelector(
    currentRating: Float,
    onRatingChange: (Float) -> Unit
) {
    var ratingText by remember {
        mutableStateOf(String.format("%.2f", currentRating))
    }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(currentRating) {
        if (!isFocused) {
            ratingText = String.format("%.2f", currentRating)
        }
    }

    fun validateAndFormatInput(input: String): String? {
        if (input.isEmpty()) return ""

        val filtered = input.filter { it.isDigit() || it == '.' }
        if (filtered != input) return null

        val parts = filtered.split('.')
        if (parts.size > 2) return null
        if (parts.size == 2 && parts[1].length > 2) return null

        if (filtered == "." || filtered.isEmpty()) return filtered

        val value = filtered.toFloatOrNull()
        if (value != null && (value < 0f || value > 5f)) return null

        return filtered
    }

    val integerPart = currentRating.coerceIn(0f, 5f).toInt()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { star ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            val newRating = star.toFloat()
                            onRatingChange(newRating)
                            ratingText = String.format("%.2f", newRating)
                        }
                ) {
                    Icon(
                        imageVector = if (star <= integerPart) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "$star stars",
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center),
                        tint = if (star <= integerPart)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                val isFocusedState = interactionSource.collectIsFocusedAsState()

                LaunchedEffect(isFocusedState.value) {
                    if (!isFocusedState.value) {
                        val value = ratingText.toFloatOrNull()?.coerceIn(0f, 5f)
                        if (value != null) {
                            ratingText = String.format("%.2f", value)
                            onRatingChange(value)
                        } else if (ratingText.isNotBlank()) {
                            ratingText = String.format("%.2f", currentRating)
                        } else {
                            ratingText = "0.00"
                            onRatingChange(0f)
                        }
                        isFocused = false
                    } else {
                        isFocused = true
                    }
                }

                OutlinedTextField(
                    value = ratingText,
                    onValueChange = { newValue ->
                        val validated = validateAndFormatInput(newValue)
                        if (validated != null) {
                            ratingText = validated
                            val newRating = validated.toFloatOrNull()?.coerceIn(0f, 5f)
                            if (newRating != null) {
                                val clampedRating = newRating.coerceIn(0f, 5f)
                                onRatingChange(clampedRating)
                            }
                        }
                    },
                    modifier = Modifier.width(100.dp),
                    label = { Text("Rating (0-5)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    interactionSource = interactionSource
                )

            }

            Text(
                text = "Enter rating: 0.00 - 5.00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

