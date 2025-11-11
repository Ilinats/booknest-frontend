package com.example.booknest.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.CreateGenreUiState
import com.example.booknest.viewmodel.SignupViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(navController: NavController, viewModel: SignupViewModel) {
    val availableGenresDtoList by viewModel.availableGenres.collectAsState()
    val selectedGenres = remember { mutableStateListOf<String>() }
    var showCreateGenreDialog by remember { mutableStateOf(false) }
    val createGenreState by viewModel.createGenreUiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedGenres.toList()) {
        viewModel.updateGenres(selectedGenres.toList())
    }

    LaunchedEffect(createGenreState) {
        when (val state = createGenreState) {
            is CreateGenreUiState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                viewModel.resetCreateGenreState()
                showCreateGenreDialog = false
            }
            is CreateGenreUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.error)
                }
                viewModel.resetCreateGenreState()
            }
            else -> Unit
        }
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

            Button(onClick = { showCreateGenreDialog = true }) {
                Text("Create New Genre")
            }

            if (availableGenresDtoList.isEmpty()) {
                Text("No genres available. Create one!")
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

    if (showCreateGenreDialog) {
        CreateGenreDialog(
            createGenreState = createGenreState,
            onDismissRequest = { showCreateGenreDialog = false },
            onCreateClick = {
                name, description, colorCode, icon, isActive ->
                viewModel.createGenre(name, description, colorCode, icon, isActive)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGenreDialog(
    createGenreState: CreateGenreUiState,
    onDismissRequest: () -> Unit,
    onCreateClick: (name: String, description: String?, colorCode: String?, icon: String?, isActive: Boolean?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var colorCode by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var nameError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Create New Genre", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Genre Name*") },
                    isError = nameError != null,
                    singleLine = true
                )
                if (nameError != null) {
                    Text(nameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") }
                )
                OutlinedTextField(
                    value = colorCode,
                    onValueChange = { colorCode = it },
                    label = { Text("Color Code (Optional Hex e.g. #FFFFFF)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Icon (Optional URL or identifier)") },
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Is Active")
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest, enabled = createGenreState !is CreateGenreUiState.Loading) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = "Genre name cannot be empty"
                            } else {
                                onCreateClick(
                                    name,
                                    description.ifBlank { null },
                                    colorCode.ifBlank { null },
                                    icon.ifBlank { null },
                                    isActive
                                )
                            }
                        },
                        enabled = createGenreState !is CreateGenreUiState.Loading
                    ) {
                        if (createGenreState is CreateGenreUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}
