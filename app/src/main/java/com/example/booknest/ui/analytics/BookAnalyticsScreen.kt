package com.example.booknest.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.*
import com.example.booknest.ui.analytics.components.book.*
import com.example.booknest.viewmodel.analytics.AnalyticsViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.presentation.common.UiState
import com.example.booknest.ui.components.AppTopBar
import com.example.booknest.ui.components.BackgroundDecoration
import com.example.booknest.ui.components.paddingTopFromScaffold
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAnalyticsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    bookId: String,
    analyticsViewModel: AnalyticsViewModel = getViewModel()
) {
    val analyticsState by analyticsViewModel.bookAnalyticsState.collectAsState()
    val currentAnalytics by analyticsViewModel.currentBookAnalytics.collectAsState()

    LaunchedEffect(bookId) {
        analyticsViewModel.loadDetailedBookAnalytics(bookId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundDecoration(modifier = Modifier.fillMaxSize())

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                AppTopBar(
                    title = "Book Analytics",
                    navigationIcon = {
                        BackButton(onClick = { navController.popBackStack() })
                    },
                )
            },
        ) { paddingValues ->
            val currentState = analyticsState
            when (currentState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .paddingTopFromScaffold(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .paddingTopFromScaffold(paddingValues),
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
                                text = (currentState as UiState.Error).message,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = {
                                analyticsViewModel.loadDetailedBookAnalytics(bookId)
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    BookAnalyticsContent(
                        analytics = currentState.data,
                        analyticsViewModel = analyticsViewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .paddingTopFromScaffold(paddingValues),
                        isScrollable = true
                    )
                }

                else -> {}
            }
        }
    }
}
