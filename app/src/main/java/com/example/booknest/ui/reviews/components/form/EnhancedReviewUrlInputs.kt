package com.example.booknest.ui.reviews.components.form

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.ui.components.reviews.ReviewLinkPreview
import com.example.booknest.ui.reviews.utils.isValidUrl

@Composable
fun EnhancedReviewUrlInputs(
    urls: List<String>,
    onUrlsChange: (List<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        urls.forEachIndexed { index, url ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { newUrl ->
                            val newUrls = urls.toMutableList()
                            newUrls[index] = newUrl
                            onUrlsChange(newUrls)
                        },
                        label = { Text("Review URL ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("https://...") },
                        isError = url.isNotBlank() && !isValidUrl(url),
                        supportingText = {
                            if (url.isNotBlank() && !isValidUrl(url)) {
                                Text(
                                    text = "Please enter a valid URL starting with http:// or https://",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        trailingIcon = {
                            if (url.isNotBlank() && isValidUrl(url)) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Valid URL",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    if (urls.size > 1) {
                        IconButton(
                            onClick = {
                                val newUrls = urls.toMutableList()
                                newUrls.removeAt(index)
                                onUrlsChange(newUrls)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Remove URL",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (url.isNotBlank() && isValidUrl(url)) {
                    ReviewLinkPreview(
                        url = url,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (urls.size < 5) {
            OutlinedButton(
                onClick = {
                    onUrlsChange(urls + "")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add URL",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Another URL (${urls.size}/5)")
            }
        }
    }
}

