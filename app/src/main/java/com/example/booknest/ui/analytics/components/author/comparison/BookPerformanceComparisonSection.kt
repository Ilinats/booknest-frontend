package com.example.booknest.ui.author.components.analytics.comparison

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.navigation.Screen

@Composable
fun BookPerformanceComparisonSection(
    books: List<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse>,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Book Performance Comparison",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (books.isEmpty()) {
                Text(
                    text = "No books to compare",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                val sortedBooks = books.sortedWith(
                    compareByDescending<com.example.booknest.domain.model.response.BookPerformanceComparisonResponse> { it.reviews.averageRating }
                        .thenByDescending { it.applications.totalApplications }
                )

                val bestPerformer = sortedBooks.firstOrNull()
                val worstPerformer = sortedBooks.lastOrNull()

                if (bestPerformer != null && worstPerformer != null && bestPerformer != worstPerformer) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PerformanceHighlightCard(
                            title = "Best Performer",
                            book = bestPerformer,
                            isBest = true,
                            onClick = {
                                navController.navigate(
                                    Screen.BookApplicationDetail.createRoute(
                                        bestPerformer.bookId
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PerformanceHighlightCard(
                            title = "Needs Improvement",
                            book = worstPerformer,
                            isBest = false,
                            onClick = {
                                navController.navigate(
                                    Screen.BookApplicationDetail.createRoute(
                                        worstPerformer.bookId
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "All Books",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                sortedBooks.forEach { book ->
                    BookPerformanceRow(
                        book = book,
                        onClick = {
                            navController.navigate(
                                Screen.BookApplicationDetail.createRoute(
                                    book.bookId
                                )
                            )
                        }
                    )
                    if (book != sortedBooks.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceHighlightCard(
    title: String,
    book: com.example.booknest.domain.model.response.BookPerformanceComparisonResponse,
    isBest: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isBest)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isBest)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = book.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "Rating",
                        fontSize = 10.sp,
                        color = if (isBest)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = String.format("%.1f", book.reviews.averageRating),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "Apps",
                        fontSize = 10.sp,
                        color = if (isBest)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = book.applications.totalApplications.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BookPerformanceRow(
    book: com.example.booknest.domain.model.response.BookPerformanceComparisonResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.coverImageUrl,
                contentDescription = book.title,
                modifier = Modifier.size(48.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PerformanceMetricSmall(
                        label = "Rating",
                        value = String.format("%.1f", book.reviews.averageRating),
                        icon = Icons.Default.Star
                    )
                    PerformanceMetricSmall(
                        label = "Reviews",
                        value = book.reviews.reviewCount.toString(),
                        icon = Icons.Default.Comment
                    )
                    PerformanceMetricSmall(
                        label = "Applications",
                        value = book.applications.totalApplications.toString(),
                        icon = Icons.Default.Menu
                    )
                    PerformanceMetricSmall(
                        label = "Approval",
                        value = "${book.approvalRate}%",
                        icon = Icons.Default.CheckCircle
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceMetricSmall(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.widthIn(min = 60.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier.widthIn(max = 50.dp)
        ) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
