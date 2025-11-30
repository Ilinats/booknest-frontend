package com.example.booknest.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.SignupViewModel
import kotlinx.coroutines.launch

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Select Your Favorite Genres",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            if (availableGenresDtoList.isEmpty()) {
                Text("No genres available yet.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(availableGenresDtoList) { genreDto ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = selectedGenres.contains(genreDto.name),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selectedGenres.add(genreDto.name)
                                    } else {
                                        selectedGenres.remove(genreDto.name)
                                    }
                                }
                            )
                            Text(genreDto.name, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.saveGenres { success, message ->
                        if (success) {
                            scope.launch {
                                snackbarHostState.showSnackbar(message ?: "Genres saved successfully!")
                            }
                            navController.navigate(Screen.SocialMedia.route)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(message ?: "Failed to save genres.")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                enabled = selectedGenres.isNotEmpty()
            ) {
                Text("Finish")
            }
        }
    }
}
