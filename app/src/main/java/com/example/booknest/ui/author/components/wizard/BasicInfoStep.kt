package com.example.booknest.ui.author.components.wizard

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.booknest.ui.author.components.common.CoverImagePicker

@Composable
fun BasicInfoStep(
    title: String,
    shortDescription: String,
    fullDescription: String,
    pageCount: String,
    coverImageUri: android.net.Uri?,
    coverImageUrl: String?,
    titleError: String? = null,
    shortDescriptionError: String? = null,
    fullDescriptionError: String? = null,
    pageCountError: String? = null,
    onUpdate: (String, String, String, String, android.net.Uri?, String?) -> Unit,
    onValidationChange: ((String?, String?, String?, String?) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = title,
            onValueChange = {
                onUpdate(
                    it,
                    shortDescription,
                    fullDescription,
                    pageCount,
                    coverImageUri,
                    coverImageUrl
                )
                onValidationChange?.invoke(
                    validateTitle(it),
                    validateShortDescription(shortDescription),
                    validateFullDescription(fullDescription),
                    pageCountError
                )
            },
            label = { Text("Title *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = titleError != null,
            supportingText = titleError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                Text("${title.length}/255 characters")
            }
        )

        OutlinedTextField(
            value = shortDescription,
            onValueChange = {
                onUpdate(title, it, fullDescription, pageCount, coverImageUri, coverImageUrl)
                onValidationChange?.invoke(
                    validateTitle(title),
                    validateShortDescription(it),
                    validateFullDescription(fullDescription),
                    pageCountError
                )
            },
            label = { Text("Short Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            isError = shortDescriptionError != null,
            supportingText = shortDescriptionError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                Text("${shortDescription.length}/500 characters (optional)")
            }
        )

        OutlinedTextField(
            value = fullDescription,
            onValueChange = {
                onUpdate(title, shortDescription, it, pageCount, coverImageUri, coverImageUrl)
                onValidationChange?.invoke(
                    validateTitle(title),
                    validateShortDescription(shortDescription),
                    validateFullDescription(it),
                    pageCountError
                )
            },
            label = { Text("Full Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 10,
            isError = fullDescriptionError != null,
            supportingText = fullDescriptionError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                Text("${fullDescription.length}/10,000 characters (optional)")
            }
        )

        OutlinedTextField(
            value = pageCount,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onUpdate(
                        title,
                        shortDescription,
                        fullDescription,
                        newValue,
                        coverImageUri,
                        coverImageUrl
                    )

                    val validationResult = validatePageCount(newValue)
                    val titleValidation = validateTitle(title)
                    val shortDescValidation = validateShortDescription(shortDescription)
                    val fullDescValidation = validateFullDescription(fullDescription)

                    println("DEBUG: onValueChange with newValue: '$newValue'")
                    println("DEBUG: validationResult: $validationResult")
                    println("DEBUG: calling onValidationChange with:")
                    println("  titleError: $titleValidation")
                    println("  shortDescError: $shortDescValidation")
                    println("  fullDescError: $fullDescValidation")
                    println("  pageCountError: $validationResult")

                    onValidationChange?.invoke(
                        titleValidation,
                        shortDescValidation,
                        fullDescValidation,
                        validationResult
                    )
                }
            },
            label = { Text("Page Count") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = pageCountError != null,
            supportingText = pageCountError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                if (pageCount.isNotBlank() && pageCountError == null) {
                    Text(
                        "Valid page count",
                        color = MaterialTheme.colorScheme.primary
                    )
                } else null
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Book Cover Image",
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Info",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Image Guidelines",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "• Recommended dimensions: 1200x1800px (2:3 aspect ratio)\n• Maximum file size: 10MB\n• Supported formats: JPG, PNG, GIF, WEBP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        CoverImagePicker(
            imageUri = coverImageUri,
            imageUrl = coverImageUrl,
            onImageSelected = { uri: Uri?, url: String? ->
                onUpdate(title, shortDescription, fullDescription, pageCount, uri, url)
            }
        )
    }
}

private fun validateTitle(title: String): String? {
    return when {
        title.isBlank() -> "Title is required"
        title.length > 255 -> "Title must be 255 characters or less"
        else -> null
    }
}

private fun validateShortDescription(description: String): String? {
    return if (description.length > 500) {
        "Short description must be 500 characters or less"
    } else null
}

private fun validateFullDescription(description: String): String? {
    return if (description.length > 10000) {
        "Full description must be 10,000 characters or less"
    } else null
}

private fun validatePageCount(pageCount: String): String? {
    return if (pageCount.isNotBlank()) {
        val parsed = pageCount.toIntOrNull()
        parsed?.let { pages ->
            val result = when {
                pages < 1 -> "Page count must be at least 1"
                pages > 100000 -> "Page count must be 100,000 or less"
                else -> null
            }
            result
        } ?: "Please enter a valid number"
    } else {
        null
    }
}
