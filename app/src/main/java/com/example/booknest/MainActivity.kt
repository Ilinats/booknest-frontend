package com.example.booknest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.navigation.compose.rememberNavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.EXTRA_NOTIFICATION_ID
import com.example.booknest.navigation.EXTRA_NOTIFICATION_TYPE
import com.example.booknest.navigation.NavGraph
import com.example.booknest.navigation.readNotificationLaunchExtras
import com.example.booknest.network.NetworkConnectivityMonitor
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.network.OfflineBlockingOverlay
import com.example.booknest.ui.theme.BookNestTheme
import com.example.booknest.ui.toast.BookNestToastHost
import com.example.booknest.utils.ComposeDeepLinkHandler
import com.example.booknest.utils.FCMTokenManager
import com.example.booknest.viewmodel.auth.LoginViewModel
import com.example.booknest.viewmodel.auth.SignupViewModel
import com.example.booknest.viewmodel.notifications.NotificationViewModel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    private val requestPostNotificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPostNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            BookNestTheme(dynamicColor = false) {
                val navController = rememberNavController()
                var pendingDeepLink by remember { mutableStateOf<String?>(null) }
                var pendingNotificationOpen by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    val hasNotificationExtras = intent?.let {
                        readNotificationLaunchExtras(it).hasNotificationDeepLink
                    } == true
                    if (hasNotificationExtras) {
                        pendingNotificationOpen = true
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
                val isLoggedIn by sessionManager.isLoggedIn.collectAsStateWithLifecycle()

                LaunchedEffect(isLoggedIn, pendingNotificationOpen) {
                    if (isLoggedIn == true && pendingNotificationOpen) {
                        navController.navigate(Screen.Notifications.route) {
                            launchSingleTop = true
                        }
                        pendingNotificationOpen = false
                    }
                }

                val networkMonitor: NetworkConnectivityMonitor = koinInject()
                val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()
                DisposableEffect(networkMonitor) {
                    networkMonitor.start()
                    onDispose { networkMonitor.stop() }
                }

                DisposableEffect(navController) {
                    val listener = Consumer<Intent> { newIntent ->
                        newIntent.data?.toString()?.let { deepLink ->
                            if (ComposeDeepLinkHandler.isValidEmailVerificationDeepLink(deepLink)) {
                                ComposeDeepLinkHandler.handleEmailVerificationDeepLink(
                                    navController,
                                    deepLink
                                )
                            }
                        }
                        if (newIntent.hasExtra(EXTRA_NOTIFICATION_ID) ||
                            newIntent.hasExtra(EXTRA_NOTIFICATION_TYPE)
                        ) {
                            navController.navigate(Screen.Notifications.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                    this@MainActivity.addOnNewIntentListener(listener)
                    onDispose { this@MainActivity.removeOnNewIntentListener(listener) }
                }

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
                        loginViewModel = loginViewModel
                    )
                    OfflineBlockingOverlay(
                        visible = !isOnline,
                        modifier = Modifier.zIndex(1f)
                    )
                    BookNestToastHost(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .zIndex(10f)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
