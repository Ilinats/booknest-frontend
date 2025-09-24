package com.example.booknest.ui.books

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.network.Book

@Composable
fun BookItem(book: Book) {
    Column(modifier = Modifier.width(120.dp)) {
        // Placeholder for book cover
        Text(text = book.title)
    }
}