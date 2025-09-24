package com.example.booknest.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.LoginUiState
import com.example.booknest.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text("Log In", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text("Username or Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Button(
                onClick = {
                    viewModel.loginUser(identifier, password) { success ->
                        if (success) {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Landing.route) { inclusive = true }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f).height(48.dp),
                enabled = loginState !is LoginUiState.Loading
            ) {
                if (loginState is LoginUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Log In")
                }
            }

            when (val state = loginState) {
                is LoginUiState.Error -> {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                else -> {} 
            }

            TextButton(onClick = { 
                navController.navigate(Screen.AccountType.route) {
                     popUpTo(Screen.Landing.route) { inclusive = false }
                }
            }) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}