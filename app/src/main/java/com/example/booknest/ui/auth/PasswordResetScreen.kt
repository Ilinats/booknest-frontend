package com.example.booknest.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.getViewModel
import com.example.booknest.ui.components.CodeInputField
import com.example.booknest.ui.components.ResendCodeButton
import com.example.booknest.data.session.SessionManager
import com.example.booknest.viewmodel.PasswordResetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordResetScreen(
    navController: NavController,
    sessionManager: SessionManager,
    email: String,
    viewModel: PasswordResetViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPasswordInput by remember { mutableStateOf(false) }
    var enteredCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isPasswordResetSuccessful) {
        if (uiState.isPasswordResetSuccessful) {
            navController.navigate(com.example.booknest.navigation.Screen.Login.route) {
                popUpTo(com.example.booknest.navigation.Screen.PasswordReset.createRoute(email)) {
                    inclusive = true
                }
            }
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (showPasswordInput) "Enter new password" else "Enter reset code",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (showPasswordInput)
                    "Create a new password for your account"
                else
                    "We sent a 6-digit code to\n$email",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!showPasswordInput) {
                CodeInputField(
                    onCodeChange = { code ->
                        enteredCode = code
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (enteredCode.length == 6 && enteredCode.all { it.isDigit() }) {
                            viewModel.verifyResetCode(enteredCode)
                            showPasswordInput = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enteredCode.length == 6 && enteredCode.all { it.isDigit() } && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Verify Code")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ResendCodeButton(
                    onResend = {
                        viewModel.resendResetCode(email)
                    },
                    cooldownSeconds = 60,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (newPassword == confirmPassword && newPassword.length >= 6) {
                            viewModel.resetPassword(enteredCode, newPassword)
                        } else {
                            viewModel.showError("Passwords don't match or are too short")
                        }
                    },
                    enabled = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Reset Password")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!showPasswordInput) {
                Text(
                    text = "Didn't receive the code? Check your spam folder or try resending.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}
