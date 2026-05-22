package com.example.booknest.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.MaterialTheme

import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.presentation.navigation.navigateToMainAsRoot
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
            true -> navController.navigateToMainAsRoot()

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
            .background(MaterialTheme.colorScheme.background)
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
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer { clip = false }
                .offset(x = 135.dp, y = (-135).dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(350.dp)
                .offset(x = (-175).dp, y = 175.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(270.dp)
                .offset(x = (-135).dp, y = 135.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
        )

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "BookNest Logo",
            modifier = Modifier.size(200.dp),
            contentScale = ContentScale.Fit
        )
    }
}
