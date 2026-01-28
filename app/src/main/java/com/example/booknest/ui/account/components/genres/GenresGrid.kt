package com.example.booknest.ui.account.components.genres

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.ui.components.genres.utils.createSmartRows
import com.example.booknest.ui.components.genres.GenreButton

@Composable
fun GenresGrid(
    genres: List<GenreResponse>,
    selectedGenreIds: Set<Int>,
    onGenreToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val density = LocalDensity.current
        val availableWidth = with(density) {
            maxWidth.toPx() - 48.dp.toPx()
        }
        val minButtonWidth = 60.dp
        val buttonSpacing = 8.dp
        val horizontalPadding = 20.dp * 2

        val minButtonWidthPx = with(density) { minButtonWidth.toPx() }
        val buttonSpacingPx = with(density) { buttonSpacing.toPx() }
        val horizontalPaddingPx = with(density) { horizontalPadding.toPx() }

        val rows = remember(
            genres,
            availableWidth,
            minButtonWidthPx,
            buttonSpacingPx,
            horizontalPaddingPx
        ) {
            createSmartRows(
                genres = genres,
                availableWidth = availableWidth,
                minButtonWidth = minButtonWidthPx,
                buttonSpacing = buttonSpacingPx,
                horizontalPadding = horizontalPaddingPx
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { rowGenres ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowGenres.forEach { genre ->
                            GenreButton(
                                genreName = genre.name,
                                isSelected = selectedGenreIds.contains(genre.id),
                                onClick = { onGenreToggle(genre.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

