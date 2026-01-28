package com.example.booknest

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.booknest.ui.theme.BookNestTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.booknest.navigation.NavGraph
import com.example.booknest.utils.ComposeDeepLinkHandler
import com.example.booknest.data.session.SessionManager
import com.example.booknest.viewmodel.LoginViewModel
import com.example.booknest.viewmodel.SignupViewModel
import com.example.booknest.viewmodel.NotificationViewModel
import com.example.booknest.utils.FCMTokenManager
import com.example.booknest.ui.download.GlobalDownloadHandler
import com.example.booknest.ui.toast.GlobalToastHandler
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        setContent {
            BookNestTheme(dynamicColor = false) {
                val navController = rememberNavController()
                var pendingDeepLink by remember { mutableStateOf<String?>(null) }
                var pendingNotificationNavigation by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    val hasNotificationExtras = intent?.hasExtra("notificationId") == true ||
                            intent?.hasExtra("notificationType") == true

                    if (hasNotificationExtras) {
                        pendingNotificationNavigation = true
                        println("DEBUG: App opened from notification, will navigate to notifications screen")
                    }

                    intent?.data?.toString()?.let { deepLink ->
                        if (ComposeDeepLinkHandler.isValidEmailVerificationDeepLink(deepLink)) {
                            pendingDeepLink = deepLink
                        }
                    }
                }

                LaunchedEffect(pendingDeepLink) {
                    pendingDeepLink?.let { deepLink ->
                        ComposeDeepLinkHandler.handleEmailVerificationDeepLink(
                            navController,
                            deepLink
                        )
                        pendingDeepLink = null
                    }
                }

                val signupViewModel: SignupViewModel = getViewModel()
                val loginViewModel: LoginViewModel = getViewModel()
                val notificationViewModel: NotificationViewModel = getViewModel()

                val sessionManager: SessionManager = get()
                val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn == true) {
                        val token = FCMTokenManager.getToken()
                        if (token != null) {
                            val deviceId = FCMTokenManager.getDeviceId(this@MainActivity)
                            val appVersion = FCMTokenManager.getAppVersion(this@MainActivity)
                            notificationViewModel.registerDeviceToken(token, deviceId, appVersion)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph(
                        navController = navController,
                        signupViewModel = signupViewModel,
                        loginViewModel = loginViewModel,
                        sessionManager = sessionManager
                    )
                    GlobalDownloadHandler()
                    GlobalToastHandler()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val hasNotificationExtras =
            intent.hasExtra("notificationId") || intent.hasExtra("notificationType")
        if (hasNotificationExtras) {
            println("DEBUG: New intent received from notification click")
        }

        intent.data?.toString()?.let { deepLink ->
            if (ComposeDeepLinkHandler.isValidEmailVerificationDeepLink(deepLink)) {
            }
        }
    }
}
