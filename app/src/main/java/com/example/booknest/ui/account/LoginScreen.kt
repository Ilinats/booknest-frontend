package com.example.booknest.ui.account

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.auth.GoogleSignInButton
import com.example.booknest.ui.auth.UserTypeSelectionDialog
import com.example.booknest.ui.components.ErrorToast
import com.example.booknest.ui.theme.BackgroundWhite
import com.example.booknest.ui.theme.DarkNavyBlue
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import com.example.booknest.viewmodel.GoogleAuthViewModel
import com.example.booknest.viewmodel.LoginUiState
import com.example.booknest.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel,
    sessionManager: SessionManager
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showUserTypeDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var pendingGoogleAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Error) {
            val error = (loginState as LoginUiState.Error).error
            errorMessage = getErrorMessage(error)
        }
    }
    val googleAuthViewModel: GoogleAuthViewModel = org.koin.androidx.compose.getViewModel()
    val googleAuthState by googleAuthViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        println("DEBUG: Google Sign-In result received - resultCode: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            println("DEBUG: Google Sign-In successful, processing account...")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                println("DEBUG: Got Google account: ${account.email}")
                pendingGoogleAccount = account
                showUserTypeDialog = true
            } catch (e: ApiException) {
                println("Google sign-in failed: ${e.statusCode}")
            }
        } else {
            println("DEBUG: Google Sign-In cancelled or failed - resultCode: ${result.resultCode}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBluePeriwinkle)
    ) {
        ErrorToast(
            message = errorMessage,
            onDismiss = { errorMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                TextButton(
                    onClick = {
                        navController.navigate(Screen.AccountType.route) {
                            popUpTo(Screen.Landing.route) { inclusive = false }
                        }
                    }
                ) {
                    Text(
                        "Sign Up",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.2f))

                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 90.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp),
                    textAlign = TextAlign.Left
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Don't have an account? Don't worry!\nSign up now!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp),
                    textAlign = TextAlign.Left,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = BackgroundWhite
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 50.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .background(Color(0xFFE8DFE4), RoundedCornerShape(28.dp))
                ) {
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        placeholder = {
                            Text(
                                "Email",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF757575)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 5.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color(0xFF757575),
                            unfocusedPlaceholderColor = Color(0xFF757575),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .background(Color(0xFFE8DFE4), RoundedCornerShape(28.dp))
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = {
                            Text(
                                "Password",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF757575)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 5.dp),
                        shape = RoundedCornerShape(28.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color(0xFF757575),
                            unfocusedPlaceholderColor = Color(0xFF757575),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color(0xFF757575)
                                )
                            }
                        },
                        singleLine = true
                    )
                }

                TextButton(
                    onClick = { showForgotPasswordDialog = true },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        "Forgot Password?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        ),
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    enabled = loginState !is LoginUiState.Loading,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkNavyBlue
                    )
                ) {
                    if (loginState is LoginUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            "Sign In",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFBDBDBD)
                    )
                    Text(
                        text = "or",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFBDBDBD)
                    )
                }

                Button(
                    onClick = {
                        val signInIntent = try {
                            println("DEBUG: Starting Google Sign-In...")
                            GoogleAuthViewModel.getGoogleSignInClient(context).signInIntent
                        } catch (e: Exception) {
                            println("DEBUG: Google Sign-In error: ${e.message}")
                            e.printStackTrace()
                            null
                        }
                        signInIntent?.let {
                            println("DEBUG: Got sign-in intent: $it")
                            googleSignInLauncher.launch(it)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    enabled = !googleAuthState.isLoading,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8DFE4),
                        contentColor = Color.Black
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4285F4)
                            )
                        }

                        Text(
                            "Continue with Google",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

            }
        }
    }

    if (showUserTypeDialog) {
        UserTypeSelectionDialog(
            onDismiss = {
                showUserTypeDialog = false
                pendingGoogleAccount = null
            },
            onUserTypeSelected = { userType ->
                pendingGoogleAccount?.let { account ->
                    googleAuthViewModel.authenticateWithGoogle(
                        account = account,
                        userType = userType,
                        onSuccess = { response ->
                            showUserTypeDialog = false
                            pendingGoogleAccount = null
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Landing.route) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            showUserTypeDialog = false
                            pendingGoogleAccount = null
                        }
                    )
                }
            }
        )
    }

    LaunchedEffect(googleAuthState.errorMessage) {
        googleAuthState.errorMessage?.let {
            googleAuthViewModel.clearMessages()
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onEmailSubmitted = { email ->
                showForgotPasswordDialog = false
                navController.navigate(Screen.PasswordReset.createRoute(email)) {
                    popUpTo(Screen.Login.route) { inclusive = false }
                }
            }
        )
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onEmailSubmitted: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val passwordResetViewModel: com.example.booknest.viewmodel.PasswordResetViewModel =
        org.koin.androidx.compose.getViewModel()
    val uiState by passwordResetViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            if (message.contains("sent", ignoreCase = true)) {
                onEmailSubmitted(email)
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            errorMessage = error
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter your email address and we'll send you a code to reset your password.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    shape = RoundedCornerShape(24.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                            .matches()
                    ) {
                        errorMessage = null
                        passwordResetViewModel.resendResetCode(email)
                    } else {
                        errorMessage = "Please enter a valid email address"
                    }
                },
                enabled = !uiState.isLoading && email.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send Code")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getErrorMessage(error: String?): String {
    return when {
        error == null -> "An error occurred"
        error.contains("Invalid credentials", ignoreCase = true) ->
            "Invalid username/email or password. Please check your credentials and try again."

        error.contains("User not found", ignoreCase = true) ->
            "No account found with this username or email."

        error.contains("Account not verified", ignoreCase = true) ->
            "Your account is not verified. Please check your email for verification instructions."

        error.contains("Account is disabled", ignoreCase = true) ->
            "Your account has been disabled. Please contact support."

        error.contains("Network", ignoreCase = true) || error.contains(
            "connection",
            ignoreCase = true
        ) ->
            "Network error. Please check your internet connection and try again."

        error.contains("timeout", ignoreCase = true) ->
            "Request timed out. Please try again."

        else -> error
    }
}
