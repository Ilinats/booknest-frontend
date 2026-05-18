package com.example.booknest.ui.author.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.BookLeakFingerprintResponse
import com.example.booknest.presentation.common.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LeakFingerprintDecodeSection(
    leakFingerprintState: UiState<BookLeakFingerprintResponse>,
    onFileChosen: (Uri) -> Unit,
    onDismissResult: () -> Unit
) {
    val context = LocalContext.current
    var fileHint by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                it.moveToFirst()
                val fileName = it.getString(nameIndex) ?: "file"
                val fileSize = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
                val ext = fileName.substringAfterLast('.', "").lowercase()
                if (ext !in listOf("pdf", "epub")) {
                    fileHint = "Choose a PDF or EPUB file"
                    return@use
                }
                val max = 100L * 1024 * 1024
                if (fileSize > max) {
                    fileHint = "File must be at most 100 MB"
                    return@use
                }
                fileHint = null
                onFileChosen(uri)
            } ?: run { fileHint = "Could not read file information" }
        } catch (e: Exception) {
            fileHint = e.message ?: "Could not open file"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Trace leaked copy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = "Upload a PDF or EPUB that was shared outside BookNest. If it contains " +
                    "your per-reader watermark, the reader account used for that download is shown.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            fileHint?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            when (leakFingerprintState) {
                is UiState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                is UiState.Success -> {
                    val data = leakFingerprintState.data
                    val whenMarked = try {
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            .format(Date(data.issuedAt * 1000L))
                    } catch (_: Exception) {
                        data.issuedAt.toString()
                    }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Decoded fingerprint",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Reader ID: ${data.readerId}", style = MaterialTheme.typography.bodyMedium)
                            Text("Format: ${data.format}", style = MaterialTheme.typography.bodyMedium)
                            Text("Marked at: $whenMarked", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    OutlinedButton(onClick = onDismissResult) {
                        Text("Dismiss")
                    }
                }

                is UiState.Error -> {
                    Text(
                        text = leakFingerprintState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(onClick = onDismissResult) {
                        Text("Dismiss")
                    }
                }

                UiState.Idle -> {
                    Button(
                        onClick = {
                            fileHint = null
                        picker.launch("*/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose PDF or EPUB…")
                    }
                }
            }
        }
    }
}
