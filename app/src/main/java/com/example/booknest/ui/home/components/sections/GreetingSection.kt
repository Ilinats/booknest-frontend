package com.example.booknest.ui.home.components.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.UserResponse

@Composable
fun GreetingSection(
    currentUser: UserResponse?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val greeting = when {
            currentUser?.firstName != null -> "Welcome back, ${currentUser.firstName}!"
            currentUser?.username != null -> "Welcome back, ${currentUser.username}!"
            else -> "Welcome back!"
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "What would you like to read today?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

