package com.example.booknest.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.NavHostController
import com.example.booknest.presentation.navigation.Screen
import kotlinx.coroutines.delay

/**
 * Navigates the inner [navController] to the notifications screen when the activity
 * was opened from a push notification tap. Must not be called on the root NavController.
 */
@Composable
fun NotificationLaunchEffect(
    navController: NavHostController,
    isLoggedIn: Boolean?,
    popUpToRoute: String,
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    var launchGeneration by remember { mutableIntStateOf(0) }

    DisposableEffect(activity) {
        val listener = Consumer<Intent> { intent ->
            activity.setIntent(intent)
            if (readNotificationLaunchExtras(intent).hasNotificationDeepLink) {
                launchGeneration++
            }
        }
        if (activity is ComponentActivity) {
            activity.addOnNewIntentListener(listener)
            onDispose { activity.removeOnNewIntentListener(listener) }
        } else {
            onDispose { }
        }
    }

    LaunchedEffect(isLoggedIn, launchGeneration) {
        if (isLoggedIn != true) return@LaunchedEffect
        val intent = activity.intent ?: return@LaunchedEffect
        if (!readNotificationLaunchExtras(intent).hasNotificationDeepLink) return@LaunchedEffect
        delay(300)
        try {
            navController.navigate(Screen.Notifications.route) {
                popUpTo(popUpToRoute) { inclusive = false }
                launchSingleTop = true
            }
            intent.consumeNotificationLaunchExtras()
        } catch (_: Exception) {
        }
    }
}
