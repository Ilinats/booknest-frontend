package com.example.booknest.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import com.example.booknest.ui.testing.UiTestTags
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.R
import com.example.booknest.data.session.SessionManager
import com.example.booknest.presentation.navigation.Screen

@Composable
fun LandingScreen(navController: NavController, sessionManager: SessionManager) {
    val navigationBarBottom = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true) {
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Landing.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 175.dp, y = (-175).dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 135.dp, y = (-135).dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
        )

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "BookNest Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 180.dp)
                .size(200.dp),
            contentScale = ContentScale.Fit
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.47f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                Text(
                    text = "Welcome",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(top = 20.dp),
                )

                Text(
                    text = "Connect readers with authors for book reviews. Get free review copies. Support indie authors. Build your reading portfolio. Connect with fellow readers.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondary
                )

                Spacer(modifier = Modifier.weight(0.5f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp + navigationBarBottom),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { navController.navigate(Screen.Login.route) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag(UiTestTags.LANDING_LOGIN_BUTTON)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(24.dp)
                            ),
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            "Log In",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }

                    Button(
                        onClick = { navController.navigate(Screen.AccountType.route) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag(UiTestTags.LANDING_SIGNUP_BUTTON)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(24.dp)
                            ),
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            "Sign Up",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                }
            }
        }
    }
}