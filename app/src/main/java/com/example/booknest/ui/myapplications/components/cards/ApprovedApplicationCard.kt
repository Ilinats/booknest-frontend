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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.booknest.ui.myapplications.components.deadline.ReviewDeadlineCountdown
import com.example.booknest.ui.myapplications.components.sections.CopyStatusSection
import com.example.booknest.ui.myapplications.components.sections.ProgressTimeline
import com.example.booknest.ui.myapplications.components.selector.ReadingStatusSelector
import com.example.booknest.ui.myapplications.utils.formatDate
import com.example.booknest.viewmodel.applications.ApplicationViewModel
import com.example.booknest.viewmodel.files.FileViewModel
import com.example.booknest.viewmodel.applications.ReadingStatus
import androidx.navigation.NavController
import androidx.compose.foundation.background

@Composable
fun ApprovedApplicationCard(
    application: ApplicationResponse,
    applicationViewModel: ApplicationViewModel,
    fileViewModel: FileViewModel,
    navController: NavController
) {
    val book = application.book
    val isDigital = book?.distributionType?.lowercase() in listOf("digital", "both")
    val isPhysical = book?.distributionType?.lowercase() in listOf("physical", "both")

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = application.bookTitle ?: book?.title ?: "Unknown Book",
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
                        text = "by ${application.authorName ?: book?.authorName ?: book?.author?.displayName ?: "Unknown Author"}",
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

            ProgressTimeline(application = application)

            CopyStatusSection(
                application = application,
                isDigital = isDigital,
                isPhysical = isPhysical,
                onMarkReceived = {
                    applicationViewModel.markCopyReceived(application.id)
                },
                onDownload = {
                    application.bookId?.let { fileViewModel.downloadBook(it) }
                }
            )

            if (application.reviewSubmittedAt == null) {
                ReadingStatusSelector(
                    currentStatus = when (application.readingStatus) {
                        "not_started" -> ReadingStatus.NOT_STARTED
                        "currently_reading" -> ReadingStatus.CURRENTLY_READING
                        "for_review" -> ReadingStatus.FOR_REVIEW
                        "reviewed" -> ReadingStatus.FOR_REVIEW
                        else -> ReadingStatus.NOT_STARTED
                    },
                    onStatusChange = { status ->
                        applicationViewModel.updateReadingStatus(application.id, status)
                    }
                )
            }

            application.book?.reviewDeadline?.let { deadline ->
                ReviewDeadlineCountdown(deadline = deadline)
            }

            if (application.readingStatus == "for_review" ||
                (application.copyReceivedAt != null && application.reviewSubmittedAt == null)
            ) {
                Button(
                    onClick = {
                        navController.navigate(Screen.ReviewSubmission.createRoute(application.id))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Review",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Write Review")
                }
            }

            TextButton(
                onClick = {
                    application.bookId?.let {
                        navController.navigate(Screen.BookDetails.createRoute(it))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Book Details")
            }
        }
    }
}

