package com.example.booknest.ui.books.components.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.BookResponse

@Composable
fun GenreTagsSection(book: BookResponse) {
    val genresToShow = book.resolvedGenres.take(3)
    if (genresToShow.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(genresToShow.size) { index ->
                val genre = genresToShow[index]
                GenreTag(
                    text = genre.name,
                    isPrimary = true
                )
            }
        }
    }
}

