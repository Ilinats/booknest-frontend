package com.example.booknest.ui.account

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
import com.example.booknest.viewmodel.SignupUiState
import com.example.booknest.viewmodel.SignupViewModel

@Composable
fun BioScreen(navController: NavController, viewModel: SignupViewModel) {
    var bio by remember { mutableStateOf("") }
    val signupState by viewModel.signupState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Tell us about yourself",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio (optional)") },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(120.dp),
                maxLines = 4
            )

            Button(
                onClick = {
                    viewModel.updateBio(bio.ifBlank { null }, null)
                    viewModel.submitSignup { success, error ->
                        if (success) {
                            if (viewModel.signupData.accountType == "reader") {
                                navController.navigate(Screen.Genres.route) {
                                    popUpTo(Screen.AccountType.route) { inclusive = false }
                                }
                            } else {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.AccountType.route) { inclusive = true }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Finish Signup")
            }

            when (signupState) {
                is SignupUiState.Loading -> CircularProgressIndicator()
                is SignupUiState.Error -> Text("Error: ${(signupState as SignupUiState.Error).error}", color = MaterialTheme.colorScheme.error)
                is SignupUiState.Success -> Text((signupState as SignupUiState.Success).message ?: "Signed up successfully!", color = MaterialTheme.colorScheme.primary)
                else -> {}
            }
        }
    }
}