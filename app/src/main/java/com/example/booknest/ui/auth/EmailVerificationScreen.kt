package com.example.booknest.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.getViewModel
import com.example.booknest.ui.components.auth.CodeInputField
import com.example.booknest.ui.components.auth.ResendCodeButton
import com.example.booknest.data.session.SessionManager
import com.example.booknest.viewmodel.EmailVerificationViewModel
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.auth.components.AuthBackgroundDecoration

@Composable
fun EmailVerificationScreen(
    navController: NavController,
    sessionManager: SessionManager,
    userEmail: String? = null,
    viewModel: EmailVerificationViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentUser by sessionManager.currentUser.collectAsState()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
    val displayEmail = userEmail ?: currentUser?.email ?: "your email"

    LaunchedEffect(uiState.isVerificationSuccessful) {
        if (uiState.isVerificationSuccessful) {
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            val isSignupFlow = previousRoute == Screen.ProfileDetails.route ||
                    previousRoute == Screen.PersonalInfo.route ||
                    previousRoute == Screen.AccountType.route

            if (isSignupFlow) {
                val userType = currentUser?.userType?.lowercase()
                if (userType == "reader") {
                    navController.navigate(Screen.Genres.route) {
                        popUpTo("email_verification") { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Main.route) {
                        popUpTo("email_verification") { inclusive = true }
                    }
                }
            } else {
                navController.popBackStack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuthBackgroundDecoration(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            Text(
                text = "Check Your\nEmail",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "We sent a 6-digit verification code to\n$displayEmail",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(40.dp))

            var enteredCode by remember { mutableStateOf("") }
            CodeInputField(
                onCodeChange = { code ->
                    enteredCode = code
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    if (enteredCode.length == 6 && enteredCode.all { it.isDigit() }) {
                        viewModel.verifyEmail(enteredCode)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp)
                    ),
                enabled = enteredCode.length == 6 && enteredCode.all { it.isDigit() } && !uiState.isLoading,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        "Verify Code",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            ResendCodeButton(
                onResend = {
                    viewModel.resendVerificationCode()
                },
                cooldownSeconds = 60,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Didn't receive the code? Check your spam folder or try resending.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = {
                    val previousRoute = navController.previousBackStackEntry?.destination?.route
                    val isSignupFlow = previousRoute == Screen.ProfileDetails.route ||
                            previousRoute == Screen.PersonalInfo.route ||
                            previousRoute == Screen.AccountType.route

                    if (isSignupFlow) {
                        val userType = currentUser?.userType?.lowercase()
                        if (userType == "reader") {
                            navController.navigate(Screen.Genres.route) {
                                popUpTo("email_verification") { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Main.route) {
                                popUpTo("email_verification") { inclusive = true }
                            }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Skip for now",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
