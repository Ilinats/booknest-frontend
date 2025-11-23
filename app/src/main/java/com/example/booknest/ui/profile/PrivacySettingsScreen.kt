package com.example.booknest.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.UserProfile
import com.example.booknest.viewmodel.ProfileViewModel
import com.example.booknest.viewmodel.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    navController: NavController,
    authManager: AuthManager,
    profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(authManager)
    )
) {
    val myProfile by profileViewModel.myProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    
    var activityPrivacy by remember { mutableStateOf("friends") }
    var profilePrivacy by remember { mutableStateOf("friends") }
    var readingListPrivacy by remember { mutableStateOf("friends") }
    var reviewsPrivacy by remember { mutableStateOf("public") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        profileViewModel.loadMyProfile()
    }
    
    LaunchedEffect(myProfile) {
        myProfile?.let { profile ->
            activityPrivacy = profile.activityPrivacy ?: activityPrivacy
            profilePrivacy = profile.profilePrivacy ?: profilePrivacy
            readingListPrivacy = profile.readingListPrivacy ?: readingListPrivacy
            reviewsPrivacy = profile.reviewsPrivacy ?: reviewsPrivacy
            notificationsEnabled = profile.notificationsEnabled
            emailNotifications = profile.emailNotifications
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading && myProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Privacy Settings Section
                item {
                    Text(
                        text = "Privacy Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Activity Privacy
                item {
                    PrivacySettingCard(
                        title = "Activity Privacy",
                        description = "Who can see your activity",
                        icon = Icons.Default.Visibility,
                        currentValue = activityPrivacy,
                        options = listOf(
                            "public" to "Everyone",
                            "friends" to "Friends Only",
                            "private" to "Only Me"
                        ),
                        onValueChange = { activityPrivacy = it }
                    )
                }
                
                // Profile Privacy
                item {
                    PrivacySettingCard(
                        title = "Profile Privacy",
                        description = "Who can see your profile",
                        icon = Icons.Default.Person,
                        currentValue = profilePrivacy,
                        options = listOf(
                            "public" to "Everyone",
                            "friends" to "Friends Only",
                            "private" to "Only Me"
                        ),
                        onValueChange = { profilePrivacy = it }
                    )
                }
                
                // Reading List Privacy
                item {
                    PrivacySettingCard(
                        title = "Reading List Privacy",
                        description = "Who can see your reading list",
                        icon = Icons.Default.Book,
                        currentValue = readingListPrivacy,
                        options = listOf(
                            "public" to "Everyone",
                            "friends" to "Friends Only",
                            "private" to "Only Me"
                        ),
                        onValueChange = { readingListPrivacy = it }
                    )
                }
                
                // Reviews Privacy
                item {
                    PrivacySettingCard(
                        title = "Reviews Privacy",
                        description = "Who can see your reviews",
                        icon = Icons.Default.Star,
                        currentValue = reviewsPrivacy,
                        options = listOf(
                            "public" to "Everyone",
                            "friends" to "Friends Only",
                            "private" to "Only Me"
                        ),
                        onValueChange = { reviewsPrivacy = it }
                    )
                }
                
                // Notification Settings Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Notification Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Notifications Toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Push Notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Receive notifications about friend activity and updates",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )
                        }
                    }
                }
                
                // Email Notifications Toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Email Notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Receive email notifications about important updates",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = emailNotifications,
                                onCheckedChange = { emailNotifications = it }
                            )
                        }
                    }
                }
                
                // Save Button
                item {
                    Button(
                        onClick = {
                            profileViewModel.updatePrivacySettings(
                                activityPrivacy = activityPrivacy,
                                profilePrivacy = profilePrivacy,
                                readingListPrivacy = readingListPrivacy,
                                reviewsPrivacy = reviewsPrivacy
                            )
                            profileViewModel.updateNotificationSettings(
                                notificationsEnabled = notificationsEnabled,
                                emailNotifications = emailNotifications
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Settings")
                    }
                }
                
                // Error Display
                if (error != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacySettingCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentValue: String,
    options: List<Pair<String, String>>,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            options.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentValue == value,
                        onClick = { onValueChange(value) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
