package com.example.booknest.ui.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.booknest.R
import com.example.booknest.ui.theme.BackgroundWhite
import com.example.booknest.ui.theme.SkyBluePeriwinkle

import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, sessionManager: SessionManager) {
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        delay(1500)

        if (isLoggedIn == null) {
            delay(500)
        }

        when (isLoggedIn) {
            true -> {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }

            false, null -> {
                navController.navigate(Screen.Landing.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .graphicsLayer { clip = false },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer { clip = false }
                .offset(x = 175.dp, y = (-175).dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer { clip = false }
                .offset(x = 135.dp, y = (-135).dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .graphicsLayer { clip = false }
                .offset(x = (-275).dp, y = 275.dp)
                .size(550.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .graphicsLayer { clip = false }
                .offset(x = (-250).dp, y = 250.dp)
                .size(500.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle)
        )

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "BookNest Logo",
            modifier = Modifier.size(200.dp),
            contentScale = ContentScale.Fit
        )
    }
}
