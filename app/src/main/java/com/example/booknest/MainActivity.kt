package com.example.booknest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.booknest.data.AuthManager
import com.example.booknest.navigation.NavGraph
import com.example.booknest.network.RetrofitInstance
import com.example.booknest.utils.ComposeDeepLinkHandler
import com.example.booknest.viewmodel.LoginViewModel
import com.example.booknest.viewmodel.LoginViewModelFactory
import com.example.booknest.viewmodel.SignupViewModel

class MainActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Initialize RetrofitInstance first
            RetrofitInstance.initialize(null)
            
            // Then create AuthManager with the initialized API
            authManager = AuthManager.getInstance(this, RetrofitInstance.api)
            
            // Re-initialize RetrofitInstance with AuthManager for interceptor
            RetrofitInstance.initialize(authManager)
        } catch (e: Exception) {
            println("ERROR: Failed to initialize app components: ${e.message}")
            e.printStackTrace()
            // Continue with app startup even if initialization fails
        }
        
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                var pendingDeepLink by remember { mutableStateOf<String?>(null) }
                
                // Handle deep links
                LaunchedEffect(Unit) {
                    // Handle initial intent
                    intent?.data?.toString()?.let { deepLink ->
                        if (ComposeDeepLinkHandler.isValidEmailVerificationDeepLink(deepLink)) {
                            pendingDeepLink = deepLink
                        }
                    }
                }
                
                // Process pending deep link
                LaunchedEffect(pendingDeepLink) {
                    pendingDeepLink?.let { deepLink ->
                        ComposeDeepLinkHandler.handleEmailVerificationDeepLink(navController, deepLink)
                        pendingDeepLink = null
                    }
                }
                
                // Create ViewModels with factories
                val signupViewModel: SignupViewModel by viewModels()
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(authManager)
                )
                
                NavGraph(
                    navController = navController,
                    signupViewModel = signupViewModel,
                    loginViewModel = loginViewModel,
                    authManager = authManager
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        // Handle new deep link intent
        intent.data?.toString()?.let { deepLink ->
            if (ComposeDeepLinkHandler.isValidEmailVerificationDeepLink(deepLink)) {
                // This will be handled by the LaunchedEffect in the Compose content
                // when the intent changes
            }
        }
    }
}
