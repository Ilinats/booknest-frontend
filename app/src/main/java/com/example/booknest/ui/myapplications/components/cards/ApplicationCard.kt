package com.example.booknest.ui.myapplications.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.myapplications.components.common.StatusBadge
import com.example.booknest.ui.myapplications.utils.formatDate
import androidx.navigation.NavController
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.FileViewModel

@Composable
fun ApplicationCard(
    application: ApplicationResponse,
    showFullDetails: Boolean,
    applicationViewModel: ApplicationViewModel,
    fileViewModel: FileViewModel,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                application.bookId?.let {
                    navController.navigate(Screen.BookDetails.createRoute(it))
                }
            },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val coverImageUrl = application.bookCoverImageUrl ?: application.book?.coverImageUrl
                if (!coverImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = coverImageUrl,
                        contentDescription = application.bookTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Book,
                        contentDescription = "No cover",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = application.bookTitle ?: application.book?.title ?: "Unknown Book",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable {
                        application.bookId?.let {
                            navController.navigate(Screen.BookDetails.createRoute(it))
                        }
                    }
                )

                Text(
                    text = "by ${application.authorName ?: application.book?.authorName ?: application.book?.author?.displayName ?: "Unknown Author"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                    }
                )

                Text(
                    text = "Applied: ${formatDate(application.appliedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                StatusBadge(status = application.status)
            }
        }
    }
}

