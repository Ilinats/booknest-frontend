package com.example.booknest.ui.components.genres.utils

import com.example.booknest.domain.model.response.GenreResponse

fun estimateButtonWidth(text: String, horizontalPadding: Float): Float {
    val charWidthPx = 15f
    val textWidth = text.length * charWidthPx
    return textWidth + horizontalPadding
}

fun createSmartRows(
    genres: List<GenreResponse>,
    availableWidth: Float,
    minButtonWidth: Float,
    buttonSpacing: Float,
    horizontalPadding: Float
): List<List<GenreResponse>> {
    val rows = mutableListOf<List<GenreResponse>>()
    var index = 0

    while (index < genres.size) {
        val remainingGenres = genres.subList(index, genres.size)

        if (remainingGenres.size >= 3) {
            val threeButtons = remainingGenres.take(3)
            val widths = threeButtons.map { estimateButtonWidth(it.name, horizontalPadding) }
            val totalWidth = widths.sum() + buttonSpacing * 2
            val minWidthInRow = widths.minOrNull() ?: 0f
            val avgWidth = totalWidth / 3

            if (totalWidth <= availableWidth && minWidthInRow >= minButtonWidth && avgWidth >= minButtonWidth) {
                rows.add(threeButtons)
                index += 3
                continue
            }
        }

        if (remainingGenres.size >= 2) {
            val twoButtons = remainingGenres.take(2)
            val widths = twoButtons.map { estimateButtonWidth(it.name, horizontalPadding) }
            val totalWidth = widths.sum() + buttonSpacing
            val minWidthInRow = widths.minOrNull() ?: 0f
            val avgWidth = totalWidth / 2

            if (totalWidth <= availableWidth && minWidthInRow >= minButtonWidth && avgWidth >= minButtonWidth) {
                rows.add(twoButtons)
                index += 2
                continue
            }
        }

        rows.add(listOf(remainingGenres.first()))
        index += 1
    }

    return rows
}

