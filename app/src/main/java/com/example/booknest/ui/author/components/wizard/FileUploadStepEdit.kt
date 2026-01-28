package com.example.booknest.ui.author.components.wizard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.ui.author.components.common.DistributionType

@Composable
fun FileUploadStepEdit(
    bookFileUri: Uri?,
    bookFileName: String?,
    bookFileSize: Long?,
    distributionType: DistributionType?,
    existingFileUrl: String?,
    existingFileName: String?,
    existingFileSize: Long?,
    onFileSelected: (Uri?, String?, Long?) -> Unit
) {
    val context = LocalContext.current
    var fileError by remember { mutableStateOf<String?>(null) }

    val isRequired =
        distributionType == DistributionType.DIGITAL || distributionType == DistributionType.BOTH
    val maxFileSize = 50 * 1024 * 1024L
    val hasExistingFile = !existingFileUrl.isNullOrBlank()
    val hasNewFile = bookFileUri != null

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    it.moveToFirst()
                    val fileName = it.getString(nameIndex) ?: "Unknown file"
                    val fileSize = it.getLong(sizeIndex)

                    val fileExtension = fileName.substringAfterLast('.', "").lowercase()
                    if (fileExtension !in listOf("pdf", "epub")) {
                        fileError = "Only PDF and EPUB files are supported"
                        onFileSelected(null, null, null)
                        return@let
                    }

                    if (fileSize > maxFileSize) {
                        fileError = "File size exceeds maximum of 50MB"
                        onFileSelected(null, null, null)
                        return@let
                    }

                    fileError = null
                    onFileSelected(uri, fileName, fileSize)
                } ?: run {
                    fileError = "Could not read file information"
                    onFileSelected(null, null, null)
                }
            } catch (e: Exception) {
                fileError = "Error reading file: ${e.message}"
                onFileSelected(null, null, null)
            }
        } ?: run {
            onFileSelected(null, null, null)
        }
    }

    fun formatFileSize(bytes: Long?): String {
        if (bytes == null) return "Unknown size"
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isRequired) "Book File Upload *" else "Book File Upload",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (isRequired) {
                Text(
                    text = "Required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

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
                        text = "File Requirements",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "• Supported formats: PDF, EPUB only\n• Maximum file size: ${
                        formatFileSize(
                            maxFileSize
                        )
                    }\n• File will be validated before upload",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (fileError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = fileError!!,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasNewFile || hasExistingFile)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    Color.Transparent
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasExistingFile && !hasNewFile) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current File",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = existingFileName ?: "Book file",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                existingFileSize?.let { size ->
                                    Text(
                                        text = "Size: ${formatFileSize(size)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Upload a new file to replace the current one",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (hasNewFile && bookFileName != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "New File Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = bookFileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                bookFileSize?.let { size ->
                                    Text(
                                        text = "Size: ${formatFileSize(size)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = {
                                onFileSelected(null, null, null)
                                fileError = null
                            }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Remove File",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                if (!hasNewFile) {
                    Button(
                        onClick = {
                            filePickerLauncher.launch("application/pdf,application/epub+zip")
                            fileError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Select File",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (hasExistingFile) "Replace File" else "Select Book File",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
