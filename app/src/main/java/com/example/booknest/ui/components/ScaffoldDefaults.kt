package com.example.booknest.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val AppScaffoldContentInsets: WindowInsets = WindowInsets(0, 0, 0, 0)

val AuthorBottomNavListEndPadding = 16.dp

fun Modifier.paddingTopFromScaffold(paddingValues: PaddingValues): Modifier = composed {
    padding(top = paddingValues.calculateTopPadding())
}

fun authorListContentPadding(
    start: Dp = 16.dp,
    top: Dp = 16.dp,
    end: Dp = 16.dp,
    bottom: Dp = AuthorBottomNavListEndPadding,
): PaddingValues = PaddingValues(start, top, end, bottom)

@Composable
fun appListContentPadding(
    horizontal: Dp = 16.dp,
    top: Dp = 16.dp,
    bottom: Dp = 16.dp,
): PaddingValues {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return PaddingValues(
        start = horizontal,
        top = top,
        end = horizontal,
        bottom = bottom + navigationBarBottom,
    )
}
