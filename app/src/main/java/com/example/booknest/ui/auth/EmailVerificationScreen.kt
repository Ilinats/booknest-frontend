package com.example.booknest.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.viewmodel.EmailVerificationViewModel
import com.example.booknest.viewmodel.EmailVerificationViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    navController: NavController,
    authManager: AuthManager,
    verificationToken: String? = null,
    viewModel: EmailVerificationViewModel = viewModel(
        factory = EmailVerificationViewModelFactory(authManager)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var tokenText by remember { mutableStateOf(verificationToken ?: "") }
    var isTokenVisible by remember { mutableStateOf(false) }
    
    // Handle initial token from deep link
    LaunchedEffect(verificationToken) {
        verificationToken?.let { token ->
            tokenText = token
            viewModel.verifyEmail(token)
        }
    }
    
    // Handle UI state changes
    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            // Navigate back or to main screen after successful verification
            navController.popBackStack()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Logo/Icon
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Email Verification",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        // Title
        Text(
            text = "Verify Your Email",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        // Subtitle
        Text(
            text = "Please verify your email address to continue using BookNest",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        // Status Text
        Text(
            text = when {
                uiState.isLoading -> "Verifying your email..."
                uiState.isVerified -> "✓ Email verified successfully!"
                uiState.error != null -> uiState.error!!
                uiState.message != null -> uiState.message!!
                else -> "Enter the verification token from your email or click the verification link"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                uiState.isVerified -> Color(0xFF4CAF50) // Green for success
                uiState.error != null -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Token Input Field
        OutlinedTextField(
            value = tokenText,
            onValueChange = { tokenText = it },
            label = { Text("Verification Token") },
            placeholder = { Text("Enter verification token") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Token"
                )
            },
            trailingIcon = {
                IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                    Icon(
                        imageVector = if (isTokenVisible) Icons.Default.Face else Icons.Default.Delete,
                        contentDescription = if (isTokenVisible) "Hide token" else "Show token"
                    )
                }
            },
            visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            enabled = !uiState.isLoading && !uiState.isVerified,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Progress Indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Verify Button
        Button(
            onClick = {
                if (tokenText.isNotBlank()) {
                    viewModel.verifyEmail(tokenText)
                }
            },
            enabled = !uiState.isLoading && !uiState.isVerified && tokenText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify Email")
        }
        
        // Secondary Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Resend Email Button
            OutlinedButton(
                onClick = {
                    val currentUser = authManager.getCurrentUser()
                    currentUser?.email?.let { email ->
                        viewModel.resendVerificationEmail(email)
                    }
                },
                enabled = !uiState.isLoading && !uiState.isVerified,
                modifier = Modifier.weight(1f)
            ) {
                Text("Resend Email")
            }
            
            // Check Status Button
            OutlinedButton(
                onClick = {
                    val currentUser = authManager.getCurrentUser()
                    currentUser?.let { user ->
                        viewModel.checkVerificationStatus(user.id)
                    }
                },
                enabled = !uiState.isLoading && !uiState.isVerified,
                modifier = Modifier.weight(1f)
            ) {
                Text("Check Status")
            }
        }
        
        // Back to Login Button
        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !uiState.isLoading
        ) {
            Text("Back to Login")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Help Text
        Text(
            text = "If you didn't receive the email, check your spam folder or try resending it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
