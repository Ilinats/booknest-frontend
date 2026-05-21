package com.example.booknest.ui.reviews.components.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp

private fun sanitizeRatingDraft(input: String): String {
    val builder = StringBuilder()
    var dotSeen = false
    var decimalDigits = 0

    for (character in input) {
        when {
            character.isDigit() -> {
                if (dotSeen) {
                    if (decimalDigits >= 2) continue
                    decimalDigits++
                }
                builder.append(character)
            }

            character == '.' -> {
                if (dotSeen) continue
                dotSeen = true
                builder.append(character)
            }
        }
    }

    return builder.toString()
}

/** Parses in-progress text; returns null for incomplete values like "" or "4." */
private fun parseInProgressRating(text: String): Float? {
    val trimmed = text.trim()
    if (trimmed.isEmpty() || trimmed == ".") return null
    if (trimmed.endsWith('.')) return null
    return trimmed.toFloatOrNull()
}

private fun formatRating(value: Float): String = String.format("%.2f", value.coerceIn(0f, 5f))

@Composable
fun EnhancedRatingSelector(
    currentRating: Float,
    onRatingChange: (Float) -> Unit
) {
    var ratingText by remember { mutableStateOf(formatRating(currentRating)) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(currentRating) {
        if (!isFocused) {
            ratingText = formatRating(currentRating)
        }
    }

    LaunchedEffect(isFocused) {
        if (!isFocused) {
            val parsed = parseInProgressRating(ratingText)?.coerceIn(0f, 5f) ?: 0f
            ratingText = formatRating(parsed)
            onRatingChange(parsed)
        }
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
                            ratingText = formatRating(newRating)
                        }
                ) {
                    Icon(
                        imageVector = if (star <= integerPart) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "$star stars",
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center),
                        tint = if (star <= integerPart) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = ratingText,
                onValueChange = { newValue ->
                    ratingText = sanitizeRatingDraft(newValue)
                    parseInProgressRating(ratingText)
                        ?.takeIf { it in 0f..5f }
                        ?.let { onRatingChange(it) }
                },
                modifier = Modifier.width(120.dp),
                label = { Text("Rating (0.00–5.00)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                interactionSource = interactionSource,
                supportingText = {
                    Text("Enter rating: 0.00 - 5.00")
                }
            )
        }
    }
}
