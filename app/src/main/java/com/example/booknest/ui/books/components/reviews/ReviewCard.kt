package com.example.booknest.ui.books.components.reviews

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.ui.components.reviews.ReviewLinkPreview
import com.example.booknest.viewmodel.ReviewType
import com.example.booknest.ui.books.utils.formatDate

@Composable
fun ReviewCard(review: ReviewResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reviewer = review.application?.reader
                    val reviewerInitial = reviewer?.username?.firstOrNull()?.uppercase() ?: "R"
                    val reviewerName = reviewer?.username
                        ?: "${reviewer?.firstName ?: ""} ${reviewer?.lastName ?: ""}".trim()
                            .takeIf { it.isNotBlank() }
                        ?: "Reviewer"

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        reviewer?.profilePictureUrl?.let { profileUrl ->
                            AsyncImage(
                                model = profileUrl,
                                contentDescription = reviewerName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Text(
                            text = reviewerInitial,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = reviewerName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatDate(review.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "$star stars",
                            modifier = Modifier.size(16.dp),
                            tint = if (star <= review.rating) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.2f", review.rating.toDouble()),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val reviewType = review.reviewType
            when (reviewType) {
                ReviewType.TEXT.value -> {
                    review.reviewContent?.let { content ->
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                ReviewType.LINK.value -> {
                    review.reviewUrls?.forEach { url ->
                        if (url.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ReviewLinkPreview(
                                url = url,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (review.reviewContent != null && review.reviewUrls != null && review.reviewUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                review.reviewUrls.forEach { url ->
                    if (url.isNotBlank()) {
                        ReviewLinkPreview(
                            url = url,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

