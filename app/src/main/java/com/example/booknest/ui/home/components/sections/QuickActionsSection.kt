package com.example.booknest.ui.home.components.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.navigation.BottomBarScreen
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.home.components.cards.QuickActionCard

@Composable
fun QuickActionsSection(
    activeReadingApplications: List<ApplicationResponse>,
    pendingApplications: List<ApplicationResponse>,
    unreadCount: Int,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (activeReadingApplications.isNotEmpty()) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Reading",
                subtitle = "${activeReadingApplications.size} book(s)",
                icon = Icons.Filled.Book,
                onClick = {
                    navController.navigate(BottomBarScreen.MyApplications.route)
                }
            )
        }

        if (pendingApplications.isNotEmpty()) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Pending",
                subtitle = "${pendingApplications.size} waiting",
                icon = Icons.Filled.Book,
                onClick = {
                    navController.navigate(BottomBarScreen.MyApplications.route)
                }
            )
        }

        if (unreadCount > 0) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Alerts",
                subtitle = "$unreadCount new",
                icon = Icons.Filled.Notifications,
                onClick = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }
    }
}

