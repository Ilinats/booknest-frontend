package com.example.booknest.ui.author.components.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BookStatusInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Understanding Book Statuses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusInfoItem(
                    status = "Draft",
                    description = "Your book is being created or edited. It's not visible to readers yet. You can edit all details and publish when ready."
                )
                
                StatusInfoItem(
                    status = "Active",
                    description = "Your book is published and accepting applications from readers. Readers can see and apply for your book."
                )
                
                StatusInfoItem(
                    status = "In Progress",
                    description = "Your book has approved applications and readers are currently reading it. You can track reading progress and reviews."
                )
                
                StatusInfoItem(
                    status = "Completed",
                    description = "The review period has ended. All reviews have been submitted. You can view final statistics and analytics."
                )
            }
        }
    }
}

@Composable
private fun StatusInfoItem(
    status: String,
    description: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$status:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
        )
    }
}
