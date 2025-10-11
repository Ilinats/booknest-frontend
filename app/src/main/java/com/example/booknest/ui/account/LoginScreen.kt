package com.example.booknest.ui.account

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.data.GoogleAuthManager
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.auth.GoogleSignInButton
import com.example.booknest.ui.auth.UserTypeSelectionDialog
import com.example.booknest.viewmodel.GoogleAuthViewModel
import com.example.booknest.viewmodel.GoogleAuthViewModelFactory
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
    authManager: AuthManager
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showUserTypeDialog by remember { mutableStateOf(false) }
    var pendingGoogleAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }
    
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    
    // Initialize Google Auth Manager
    val googleAuthManager = remember { GoogleAuthManager(context, com.example.booknest.network.RetrofitInstance.api) }
    val googleAuthViewModel = remember { 
        GoogleAuthViewModel(googleAuthManager, authManager) 
    }
    val googleAuthState by googleAuthViewModel.uiState.collectAsState()
    
    // Google Sign-In Launcher
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
                // Handle error
                println("Google sign-in failed: ${e.statusCode}")
            }
        } else {
            println("DEBUG: Google Sign-In cancelled or failed - resultCode: ${result.resultCode}")
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
            
            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            
            // Google Sign-In Button
            GoogleSignInButton(
                onClick = {
                    try {
                        println("DEBUG: Starting Google Sign-In...")
                        val signInIntent = googleAuthManager.signIn()
                        println("DEBUG: Got sign-in intent: $signInIntent")
                        googleSignInLauncher.launch(signInIntent)
                    } catch (e: Exception) {
                        println("DEBUG: Google Sign-In error: ${e.message}")
                        e.printStackTrace()
                    }
                },
                enabled = !googleAuthState.isLoading,
                modifier = Modifier.fillMaxWidth(0.7f)
            )

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
    
    // User Type Selection Dialog
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
                            // Handle error - could show a snackbar or toast
                            showUserTypeDialog = false
                            pendingGoogleAccount = null
                        }
                    )
                }
            }
        )
    }
    
    // Handle Google Auth State
    LaunchedEffect(googleAuthState.errorMessage) {
        googleAuthState.errorMessage?.let {
            // Handle error - could show a snackbar
            googleAuthViewModel.clearMessages()
        }
    }
}