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
import com.example.booknest.domain.validation.BookFormRules
import com.example.booknest.ui.author.components.common.bookFormFieldSupportingText
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
                    BookFormRules.validateTitle(it),
                    BookFormRules.validateShortDescription(shortDescription),
                    BookFormRules.validateFullDescription(fullDescription),
                    pageCountError
                )
            },
            label = { Text("Title *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = titleError != null,
            supportingText = bookFormFieldSupportingText(titleError),
        )

        OutlinedTextField(
            value = shortDescription,
            onValueChange = {
                onUpdate(title, it, fullDescription, pageCount, coverImageUri, coverImageUrl)
                onValidationChange?.invoke(
                    BookFormRules.validateTitle(title),
                    BookFormRules.validateShortDescription(it),
                    BookFormRules.validateFullDescription(fullDescription),
                    pageCountError
                )
            },
            label = { Text("Short Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            isError = shortDescriptionError != null,
            supportingText = bookFormFieldSupportingText(shortDescriptionError),
        )

        OutlinedTextField(
            value = fullDescription,
            onValueChange = {
                onUpdate(title, shortDescription, it, pageCount, coverImageUri, coverImageUrl)
                onValidationChange?.invoke(
                    BookFormRules.validateTitle(title),
                    BookFormRules.validateShortDescription(shortDescription),
                    BookFormRules.validateFullDescription(it),
                    pageCountError
                )
            },
            label = { Text("Full Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 10,
            isError = fullDescriptionError != null,
            supportingText = bookFormFieldSupportingText(fullDescriptionError),
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
                    onValidationChange?.invoke(
                        BookFormRules.validateTitle(title),
                        BookFormRules.validateShortDescription(shortDescription),
                        BookFormRules.validateFullDescription(fullDescription),
                        BookFormRules.validatePageCount(newValue),
                    )
                }
            },
            label = { Text("Page Count") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = pageCountError != null,
            supportingText = bookFormFieldSupportingText(pageCountError),
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
