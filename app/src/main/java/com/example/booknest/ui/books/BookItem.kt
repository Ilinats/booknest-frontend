package com.example.booknest.ui.books

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.booknest.ui.theme.DarkNavyBlue
import com.example.booknest.ui.theme.DarkTealSlate
import com.example.booknest.ui.theme.DarkTealSlate

@Composable
fun BookItem(
    book: RecommendedBookResponse,
    navController: NavController? = null,
    isFullWidth: Boolean = false
) {
    Card(
        modifier = Modifier
            .then(
                if (isFullWidth) {
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController?.navigate(Screen.BookDetails.createRoute(book.id))
                        }
                } else {
                    Modifier
                        .width(230.dp)
                        .clickable {
                            navController?.navigate(Screen.BookDetails.createRoute(book.id))
                        }
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        if (isFullWidth) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 95.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 25.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavyBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            book.resolvedAuthorName?.takeIf { it.isNotBlank() && it != "Unknown Author" }
                                ?.let { authorName ->
                                    Text(
                                        text = authorName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF666666),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val rating = book.rating ?: 0.0
                                val fullStars = rating.toInt()
                                val hasHalfStar = (rating - fullStars) >= 0.5

                                repeat(5) { index ->
                                    when {
                                        index < fullStars -> {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color(0xFFB8860B)
                                            )
                                        }

                                        index == fullStars && hasHalfStar -> {
                                            Icon(
                                                imageVector = Icons.Filled.StarHalf,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color(0xFFB8860B)
                                            )
                                        }

                                        else -> {
                                            Icon(
                                                imageVector = Icons.Outlined.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color(0xFFD4C4B0)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                book.distributionType?.let { distType ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFF5F0ED)
                                    ) {
                                        Text(
                                            text = distType.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = DarkNavyBlue,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }
                                }

                                if (book.availableCopies != null && book.totalCopies != null && book.totalCopies > 0) {
                                    val filled = book.totalCopies - book.availableCopies
                                    val slotsText = "${filled}/${book.totalCopies} filled"
                                    val isLowAvailability = book.availableCopies <= 3

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isLowAvailability) Color(0xFFFFEBEE) else Color(
                                            0xFFF5F0ED
                                        ),
                                        border = if (isLowAvailability) BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.error
                                        ) else null
                                    ) {
                                        Text(
                                            text = slotsText,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isLowAvailability) MaterialTheme.colorScheme.error else DarkNavyBlue,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .width(75.dp)
                        .height(112.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(8.dp),
                            clip = false
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5EDE8)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!book.coverImageUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(book.coverImageUrl),
                            contentDescription = book.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = "No cover",
                            modifier = Modifier.size(28.dp),
                            tint = DarkNavyBlue
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .shadow(4.dp, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF5EDE8)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!book.coverImageUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(book.coverImageUrl),
                            contentDescription = book.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Book,
                            contentDescription = "No cover",
                            modifier = Modifier.size(28.dp),
                            tint = DarkNavyBlue
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val rating = book.rating ?: 0.0
                        val fullStars = rating.toInt()
                        val hasHalfStar = (rating - fullStars) >= 0.5

                        repeat(5) { index ->
                            when {
                                index < fullStars -> {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFB8860B)
                                    )
                                }

                                index == fullStars && hasHalfStar -> {
                                    Icon(
                                        imageVector = Icons.Filled.StarHalf,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFB8860B)
                                    )
                                }

                                else -> {
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFD4C4B0)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    book.resolvedAuthorName?.takeIf { it.isNotBlank() && it != "Unknown Author" }
                        ?.let { authorName ->
                            Text(
                                text = "By $authorName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF757575),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                    Spacer(modifier = Modifier.weight(2f))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        book.distributionType?.let { distType ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, DarkTealSlate)
                            ) {
                                Text(
                                    text = distType.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkNavyBlue,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (book.availableCopies != null && book.totalCopies != null && book.totalCopies > 0) {
                            val filled = book.totalCopies - book.availableCopies
                            val slotsText = "${filled}/${book.totalCopies} filled"
                            val isLowAvailability = book.availableCopies <= 3

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    1.dp,
                                    if (isLowAvailability) MaterialTheme.colorScheme.error else Color(
                                        0xFFCCCCCC
                                    )
                                )
                            ) {
                                Text(
                                    text = slotsText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLowAvailability) MaterialTheme.colorScheme.error else DarkNavyBlue,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleBookItem(
    book: RecommendedBookResponse,
    navController: NavController? = null
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .wrapContentHeight()
            .clickable {
                navController?.navigate(Screen.BookDetails.createRoute(book.id))
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier
                .width(80.dp)
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(120.dp)
                    .width(80.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(Color(0xFFF5EDE8)),
                contentAlignment = Alignment.Center
            ) {
                if (!book.coverImageUrl.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(book.coverImageUrl),
                        contentDescription = book.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Book,
                        contentDescription = "No cover",
                        modifier = Modifier.size(32.dp),
                        tint = DarkNavyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DarkNavyBlue,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(80.dp)
            )

            book.resolvedAuthorName?.takeIf { it.isNotBlank() && it != "Unknown Author" }
                ?.let { authorName ->
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF757575),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(80.dp)
                            .padding(horizontal = 2.dp)
                    )
                }
        }
    }
}
