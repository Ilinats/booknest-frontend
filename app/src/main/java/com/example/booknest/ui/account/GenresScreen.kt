package com.example.booknest.ui.account

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.navigation.Screen
import com.example.booknest.network.RetrofitInstance
import com.example.booknest.viewmodel.SignupViewModel

@SuppressLint("MutableCollectionMutableState")
@Composable
fun GenresScreen(navController: NavController, viewModel: SignupViewModel) {
    var genres by remember { mutableStateOf(listOf<String>()) }
    var selectedGenres by remember { mutableStateOf(mutableSetOf<String>()) }

    LaunchedEffect(Unit) {
        try {
            genres = RetrofitInstance.api.getGenres()
        } catch (e: Exception) {
            // Handle error
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Select your favorite genres",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                genres.forEach { genre ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = selectedGenres.contains(genre),
                            onCheckedChange = {
                                if (it) selectedGenres.add(genre) else selectedGenres.remove(genre)
                            }
                        )
                        Text(genre, fontSize = 16.sp)
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.updateGenres(selectedGenres.toList())
                    navController.navigate(Screen.Home.route)
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                enabled = selectedGenres.isNotEmpty()
            ) {
                Text("Finish")
            }
        }
    }
}
