package com.example.booknest.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.ui.unit.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.ui.components.CodeInputField
import com.example.booknest.ui.components.ResendCodeButton
import com.example.booknest.data.AuthManager
import com.example.booknest.viewmodel.EmailVerificationViewModel
import com.example.booknest.viewmodel.EmailVerificationViewModelFactory
import com.example.booknest.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    navController: NavController,
    authManager: AuthManager,
    userEmail: String? = null,
    viewModel: EmailVerificationViewModel = viewModel(
        factory = EmailVerificationViewModelFactory(authManager, userEmail)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Get current user email for display
    val currentUser = authManager.getCurrentUser()
    val displayEmail = userEmail ?: currentUser?.email ?: "your email"
    
    // Handle verification success
    LaunchedEffect(uiState.isVerificationSuccessful) {
        println("DEBUG: LaunchedEffect triggered - isVerificationSuccessful: ${uiState.isVerificationSuccessful}")
        if (uiState.isVerificationSuccessful) {
            println("DEBUG: Email verification successful, navigating...")
            // Check if user is a reader (needs genre selection) or author (goes to main)
            val currentUser = authManager.getCurrentUser()
            println("DEBUG: Current user type: ${currentUser?.userType}")
            if (currentUser?.userType == "reader") {
                println("DEBUG: Navigating to genres screen")
                navController.navigate(Screen.Genres.route) {
                    popUpTo("email_verification") { inclusive = true }
                }
            } else {
                println("DEBUG: Navigating to main screen")
                navController.navigate("main") {
                    popUpTo("email_verification") { inclusive = true }
                }
            }
        }
    }
    
    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Email Verification") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
            // Header
            Text(
                text = "Check your email",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "We sent a 6-digit code to\n$displayEmail",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email icon
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 6-digit code input
            var enteredCode by remember { mutableStateOf("") }
            CodeInputField(
                onCodeChange = { code ->
                    enteredCode = code
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Verify button
            Button(
                onClick = {
                    if (enteredCode.length == 6 && enteredCode.all { it.isDigit() }) {
                        viewModel.verifyEmail(enteredCode)
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
            
            // Resend code button
            ResendCodeButton(
                onResend = {
                    viewModel.resendVerificationCode()
                },
                cooldownSeconds = 60,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Help text
            Text(
                text = "Didn't receive the code? Check your spam folder or try resending.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skip for now button
            TextButton(
                onClick = {
                    // Skip email verification and proceed to next step
                    val currentUser = authManager.getCurrentUser()
                    if (currentUser?.userType == "reader") {
                        navController.navigate(Screen.Genres.route) {
                            popUpTo("email_verification") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("email_verification") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}