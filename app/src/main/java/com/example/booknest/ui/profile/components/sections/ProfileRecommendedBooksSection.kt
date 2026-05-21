package com.example.booknest.ui.profile.components.sections

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.R
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.ui.books.components.list.SimpleBookItem

@Composable
fun ProfileRecommendedBooksSection(
    books: List<RecommendedBookResponse>,
    isLoading: Boolean,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Books",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (books.size > 5) {
                TextButton(onClick = onExpandToggle) {
                    Text(if (isExpanded) "Show Less" else "Show All (${books.size})")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) {
                            stringResource(R.string.cd_collapse_book_list)
                        } else {
                            stringResource(R.string.cd_expand_book_list)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (books.isEmpty()) {
            Text(
                text = "No books available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            val booksToShow = if (isExpanded || books.size <= 5) books else books.take(5)

            val rowScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rowScroll),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                booksToShow.forEach { book ->
                    SimpleBookItem(book = book, navController = navController)
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}
