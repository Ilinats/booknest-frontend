package com.example.booknest.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.viewmodel.FavoriteGenresViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteGenresScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    viewModel: FavoriteGenresViewModel = getViewModel()
) {
    val genres by viewModel.genres.collectAsState()
    val selected by viewModel.selectedGenreIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var initialSelected by remember { mutableStateOf<Set<Int>?>(null) }

    LaunchedEffect(selected, genres, isLoading) {
        if (initialSelected == null && !isLoading && genres.isNotEmpty()) {
            initialSelected = selected.toSet()
        }
    }

    val hasChanges = remember(selected, initialSelected) {
        if (initialSelected == null) {
            false
        } else {
            selected.size != initialSelected!!.size || selected != initialSelected!!
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadGenres()
    }

    val message by viewModel.message.collectAsState()
    LaunchedEffect(message) {
        message?.let { msg ->
            if (msg == "Favorite genres saved.") {
                initialSelected = selected.toSet()
                viewModel.clearMessage()
                navController.popBackStack()
            } else {
                viewModel.clearMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp)
            ) {
                TopAppBar(
                    title = { Text("Favorite Genres") },
                    navigationIcon = {
                        BackButton(onClick = { navController.popBackStack() })
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-175).dp, y = (-175).dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-135).dp, y = (-135).dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 175.dp, y = 175.dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 135.dp, y = 135.dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )

            if (isLoading && genres.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Select your favorite genres",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Choose the genres you enjoy reading",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        if (genres.isEmpty()) {
                            Text(
                                text = "No genres available at the moment.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxWidth()
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
                                                        isSelected = selected.contains(genre.id),
                                                        onClick = { viewModel.toggleGenre(genre.id) }
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
                        val isButtonEnabled =
                            selected.isNotEmpty() && !isLoading && genres.isNotEmpty() && hasChanges
                        Button(
                            onClick = {
                                viewModel.savePreferences()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = isButtonEnabled,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isButtonEnabled)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isButtonEnabled)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            if (isLoading && selected.isNotEmpty()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                when {
                                    isLoading && selected.isNotEmpty() -> "Saving..."
                                    selected.isEmpty() -> "Select at least one genre"
                                    !hasChanges && initialSelected != null -> "No changes to save"
                                    else -> "Save Preferences"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun estimateButtonWidth(text: String, horizontalPadding: Float): Float {
    val avgCharWidthPx = 8.5f
    val textWidth = text.length * avgCharWidthPx * 1.05f
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
        val currentRow = mutableListOf<GenreResponse>()
        var currentWidth = 0f

        for (genre in remainingGenres) {
            if (currentRow.size >= 3) {
                break
            }

            val buttonWidth = estimateButtonWidth(genre.name, horizontalPadding)
            val spacingNeeded = if (currentRow.isNotEmpty()) buttonSpacing else 0f
            val totalWidthWithNewButton = currentWidth + spacingNeeded + buttonWidth

            if (totalWidthWithNewButton <= availableWidth && buttonWidth >= minButtonWidth * 0.7f) {
                currentRow.add(genre)
                currentWidth = totalWidthWithNewButton
            } else {
                break
            }
        }

        if (currentRow.isEmpty() && remainingGenres.isNotEmpty()) {
            currentRow.add(remainingGenres.first())
        }

        rows.add(currentRow)
        index += currentRow.size
    }

    return rows
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
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(30.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
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
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

