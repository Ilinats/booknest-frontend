package com.example.booknest.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.account.components.stats.StatsContent
import com.example.booknest.viewmodel.profile.ProfileStatsViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.ui.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    authorId: String? = null,
    profileStatsViewModel: ProfileStatsViewModel = getViewModel()
) {
    val currentUser by sessionManager.currentUser.collectAsState()
    val statsState by profileStatsViewModel.statsState.collectAsState()
    val currentStats by profileStatsViewModel.currentStats.collectAsState()

    LaunchedEffect(authorId) {
        if (authorId != null) {
            profileStatsViewModel.loadAuthorStats(authorId)
        } else {
            profileStatsViewModel.loadMyStats()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        },
    ) { paddingValues ->
        val currentStatsState = statsState
        when (currentStatsState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = currentStatsState.message,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            if (authorId != null) {
                                profileStatsViewModel.loadAuthorStats(authorId)
                            } else {
                                profileStatsViewModel.loadMyStats()
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is UiState.Success -> {
                StatsContent(
                    stats = currentStatsState.data,
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {}
        }
    }
}
