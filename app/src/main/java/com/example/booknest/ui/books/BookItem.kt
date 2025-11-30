package com.example.booknest.ui.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.network.Book
import com.example.booknest.navigation.Screen

@Composable
fun BookItem(
    book: Book,
    navController: NavController? = null
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { 
                println("DEBUG: BookItem clicked - book.id: ${book.id}, book.title: ${book.title}")
                println("DEBUG: Navigating to: ${Screen.BookDetails.createRoute(book.id)}")
                navController?.navigate(Screen.BookDetails.createRoute(book.id))
            },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Placeholder for book cover
            Text(
                text = book.title,
                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "by ${book.author?.name ?: book.authorName ?: "Unknown Author"}",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}