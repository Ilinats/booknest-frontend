package com.example.booknest.ui.myapplications.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.myapplications.components.common.StatusBadge
import com.example.booknest.ui.myapplications.utils.formatDate
import androidx.navigation.NavController
import com.example.booknest.viewmodel.files.FileViewModel
import androidx.compose.foundation.background

@Composable
fun CompletedApplicationCard(
    application: ApplicationResponse,
    navController: NavController,
    fileViewModel: FileViewModel
) {
    val review = application.review

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Completed: ${application.reviewSubmittedAt?.let { formatDate(it) } ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    StatusBadge(status = "completed")
                }
            }

            review?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Review",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(it.rating.toInt()) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "Star",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        it.reviewContent?.take(150)?.let { excerpt ->
                            Text(
                                text = excerpt + if (it.reviewContent.length > 150) "..." else "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    review?.id?.let { id ->
                                        navController.navigate(
                                            Screen.ReviewSubmission.createRoute(application.id) + "?reviewId=$id"
                                        )
                                    } ?: run {
                                        navController.navigate(
                                            Screen.ReviewSubmission.createRoute(
                                                application.id
                                            )
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Review")
                            }
                        }
                    }
                }
            }

            if (application.book?.distributionType?.lowercase() in listOf("digital", "both")) {
                OutlinedButton(
                    onClick = {
                        application.bookId?.let { fileViewModel.downloadBook(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = "Re-download",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-download Book")
                }
            }
        }
    }
}

