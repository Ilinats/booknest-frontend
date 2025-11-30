package com.example.booknest

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.booknest.viewmodel.SignupViewModelFactory
import com.example.booknest.viewmodel.NotificationViewModel
import com.example.booknest.viewmodel.NotificationViewModelFactory
import com.example.booknest.utils.FCMTokenManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
        
        try {
            // Create AuthManager with the API service
            // This will synchronously load the token from storage
            authManager = AuthManager.getInstance(this, RetrofitInstance.api)
            println("DEBUG: AuthManager initialized")
            
            // Set token refresh callback for the interceptor
            RetrofitInstance.setTokenRefreshCallback {
                authManager.refreshTokenIfNeeded()
            }
            println("DEBUG: Token refresh callback set")
        } catch (e: Exception) {
            println("ERROR: Failed to initialize app components: ${e.message}")
            e.printStackTrace()
            // Continue with app startup even if initialization fails
        }
        
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                var pendingDeepLink by remember { mutableStateOf<String?>(null) }
                var pendingNotificationNavigation by remember { mutableStateOf(false) }
                
                // Check if app was opened from a notification
                LaunchedEffect(Unit) {
                    // Check for notification extras in intent
                    val hasNotificationExtras = intent?.hasExtra("notificationId") == true ||
                            intent?.hasExtra("notificationType") == true
                    
                    if (hasNotificationExtras) {
                        pendingNotificationNavigation = true
                        println("DEBUG: App opened from notification, will navigate to notifications screen")
                    }
                    
                    // Handle deep links
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
                val signupViewModel: SignupViewModel = viewModel(
                    factory = SignupViewModelFactory(authManager)
                )
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(authManager)
                )
                val notificationViewModel: NotificationViewModel = viewModel(
                    factory = NotificationViewModelFactory(authManager)
                )
                
                val isLoggedIn by authManager.isLoggedIn.collectAsState()
                
                // Register FCM token when user is logged in
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        val token = FCMTokenManager.getToken()
                        if (token != null) {
                            val deviceId = FCMTokenManager.getDeviceId(this@MainActivity)
                            val appVersion = FCMTokenManager.getAppVersion(this@MainActivity)
                            notificationViewModel.registerDeviceToken(token, deviceId, appVersion)
                        }
                    }
                }
                
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
        
        // Check if this is a notification click
        val hasNotificationExtras = intent.hasExtra("notificationId") || intent.hasExtra("notificationType")
        if (hasNotificationExtras) {
            println("DEBUG: New intent received from notification click")
            // Trigger recomposition to handle navigation
            // This will be handled by the LaunchedEffect in the Compose content
        }
        
        // Handle new deep link intent
        intent.data?.toString()?.let { deepLink ->
            if (ComposeDeepLinkHandler.isValidEmailVerificationDeepLink(deepLink)) {
                // This will be handled by the LaunchedEffect in the Compose content
                // when the intent changes
            }
        }
    }
}
