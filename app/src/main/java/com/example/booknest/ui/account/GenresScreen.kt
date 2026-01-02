package com.example.booknest.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.theme.DarkNavyBlue
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import com.example.booknest.viewmodel.SignupViewModel
import kotlinx.coroutines.launch

private fun estimateButtonWidth(text: String, horizontalPadding: Float): Float {
    val charWidthPx = 15f
    val textWidth = text.length * charWidthPx
    return textWidth + horizontalPadding
}

private fun createSmartRows(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(navController: NavController, viewModel: SignupViewModel) {
    val availableGenresDtoList by viewModel.availableGenres.collectAsState()
    val selectedGenres = remember { mutableStateListOf<String>() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedGenres.toList()) {
        viewModel.updateGenres(selectedGenres.toList())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1E9EE))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-175).dp, y = (-175).dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-135).dp, y = (-135).dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 175.dp, y = 175.dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 135.dp, y = 135.dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = "Let's select\nyour interests.",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DarkNavyBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Please select two or more to proceed.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (availableGenresDtoList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val density = LocalDensity.current
                        val availableWidth = with(density) {
                            maxWidth.toPx() - 48.dp.toPx()
                        }
                        val minButtonWidth = 80.dp
                        val buttonSpacing = 6.dp
                        val horizontalPadding = 30.dp * 2

                        val minButtonWidthPx = with(density) { minButtonWidth.toPx() }
                        val buttonSpacingPx = with(density) { buttonSpacing.toPx() }
                        val horizontalPaddingPx = with(density) { horizontalPadding.toPx() }

                        val rows = remember(
                            availableGenresDtoList,
                            availableWidth,
                            minButtonWidthPx,
                            buttonSpacingPx,
                            horizontalPaddingPx
                        ) {
                            createSmartRows(
                                genres = availableGenresDtoList,
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
                                        rowGenres.forEach { genreDto ->
                                            GenreButton(
                                                genreName = genreDto.name,
                                                isSelected = selectedGenres.contains(genreDto.name),
                                                onClick = {
                                                    if (selectedGenres.contains(genreDto.name)) {
                                                        selectedGenres.remove(genreDto.name)
                                                    } else {
                                                        selectedGenres.add(genreDto.name)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        viewModel.saveGenres { success, message ->
                            if (success) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message ?: "Genres saved successfully!"
                                    )
                                }
                                navController.navigate(Screen.SocialMedia.route) {
                                    popUpTo(Screen.Genres.route) { inclusive = true }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message ?: "Failed to save genres."
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    enabled = selectedGenres.size >= 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedGenres.size >= 2) DarkNavyBlue else Color(
                            0xFFE0E0E0
                        ),
                        disabledContainerColor = Color(0xFFE0E0E0)
                    )
                ) {
                    Text(
                        "Continue",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = if (selectedGenres.size >= 2) Color.White else Color(0xFF757575)
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(snackbarHostState)
    }
}

@Composable
fun GenreButton(
    genreName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(
                if (isSelected) Color(0xFFE8DFE4) else Color.White,
                RoundedCornerShape(30.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) DarkNavyBlue else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(30.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = genreName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                fontSize = 13.sp
            ),
            color = if (isSelected) DarkNavyBlue else Color(0xFF757575),
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 8.dp)
        )
    }
}
