package com.example.booknest.ui.author

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.booknest.utils.FileUploadManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileUploadScreen(
    bookId: String,
    onUploadComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uploadManager = remember { FileUploadManager(context) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var validationResult by remember { mutableStateOf<FileUploadManager.ValidationResult?>(null) }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            try {
                val file = File(fileUri.path ?: "")
                selectedFile = file
                validationResult = uploadManager.validateFile(file)
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Failed to select file: ${e.message}"
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Upload Book File",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Upload your book file for readers to download",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // File Selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select File",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose File")
                }
                
                // File Info
                selectedFile?.let { file ->
                    Spacer(modifier = Modifier.height(16.dp))
                    FileInfoCard(
                        file = file,
                        uploadManager = uploadManager,
                        onRemove = { 
                            selectedFile = null
                            validationResult = null
                        }
                    )
                }
            }
        }
        
        // Validation Result
        validationResult?.let { result ->
            when (result) {
                is FileUploadManager.ValidationResult.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "✓ File is valid and ready to upload",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                is FileUploadManager.ValidationResult.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "✗ ${result.message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Upload Progress
        if (isUploading) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Uploading...",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = uploadProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        // Upload Button
        if (selectedFile != null && validationResult is FileUploadManager.ValidationResult.Success && !isUploading) {
            Button(
                onClick = {
                    // TODO: Implement actual upload logic
                    isUploading = true
                    uploadProgress = 0f
                    
                    // Simulate upload progress
                    // In real implementation, this would call the API
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload File")
            }
        }
        
        // Supported Formats Info
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Supported Formats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PDF, EPUB, MOBI, DOC, DOCX, TXT",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Maximum file size: 50MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FileInfoCard(
    file: File,
    uploadManager: FileUploadManager,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uploadManager.getFileType(file),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = uploadManager.getFileSizeString(file),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onRemove
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove file",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
