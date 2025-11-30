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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.OutlinedTextField
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
import com.example.booknest.viewmodel.FavoriteGenresViewModel
import com.example.booknest.viewmodel.FavoriteGenresViewModelFactory
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
    
    // Notification preferences
    var friendRequests by remember { mutableStateOf(true) }
    var friendRequestAccepted by remember { mutableStateOf(true) }
    var applicationApproved by remember { mutableStateOf(true) }
    var applicationRejected by remember { mutableStateOf(true) }
    var reviewDeadlineReminders by remember { mutableStateOf(true) }
    var authorBookPublished by remember { mutableStateOf(true) }
    
    // Favorite Genres ViewModel
    val favoriteGenresViewModel: FavoriteGenresViewModel = viewModel(
        factory = FavoriteGenresViewModelFactory(authManager)
    )
    val favoriteGenres by favoriteGenresViewModel.genres.collectAsState()
    val selectedGenres by favoriteGenresViewModel.selectedGenreIds.collectAsState()
    val genresLoading by favoriteGenresViewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        profileViewModel.loadMyProfile()
        favoriteGenresViewModel.loadGenres()
    }
    
    LaunchedEffect(myProfile) {
        myProfile?.let { profile ->
            activityPrivacy = profile.activityPrivacy ?: activityPrivacy
            profilePrivacy = profile.profilePrivacy ?: profilePrivacy
            readingListPrivacy = profile.readingListPrivacy ?: readingListPrivacy
            reviewsPrivacy = profile.reviewsPrivacy ?: reviewsPrivacy
            notificationsEnabled = profile.notificationsEnabled
            emailNotifications = profile.emailNotifications
            
            // Load notification preferences
            profile.notificationPreferences?.let { prefs ->
                friendRequests = prefs.friendRequests ?: true
                friendRequestAccepted = prefs.friendRequestAccepted ?: true
                applicationApproved = prefs.applicationApproved ?: true
                applicationRejected = prefs.applicationRejected ?: true
                reviewDeadlineReminders = prefs.reviewDeadlineReminders ?: true
                authorBookPublished = prefs.authorBookPublished ?: true
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    BackButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 8.dp)
                    )
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
                
                // Notification Preferences Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Notification Preferences",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Friend Requests Notification
                item {
                    NotificationPreferenceCard(
                        title = "Friend Requests",
                        description = "Notify when someone sends a friend request",
                        checked = friendRequests,
                        onCheckedChange = { friendRequests = it },
                        enabled = notificationsEnabled
                    )
                }
                
                // Friend Request Accepted Notification
                item {
                    NotificationPreferenceCard(
                        title = "Friend Request Accepted",
                        description = "Notify when a friend request is accepted",
                        checked = friendRequestAccepted,
                        onCheckedChange = { friendRequestAccepted = it },
                        enabled = notificationsEnabled
                    )
                }
                
                // Application Approved Notification
                item {
                    NotificationPreferenceCard(
                        title = "Application Approved",
                        description = "Notify when book application is approved",
                        checked = applicationApproved,
                        onCheckedChange = { applicationApproved = it },
                        enabled = notificationsEnabled
                    )
                }
                
                // Application Rejected Notification
                item {
                    NotificationPreferenceCard(
                        title = "Application Rejected",
                        description = "Notify when book application is rejected",
                        checked = applicationRejected,
                        onCheckedChange = { applicationRejected = it },
                        enabled = notificationsEnabled
                    )
                }
                
                // Review Deadline Reminders
                item {
                    NotificationPreferenceCard(
                        title = "Review Deadline Reminders",
                        description = "Reminders for review deadlines",
                        checked = reviewDeadlineReminders,
                        onCheckedChange = { reviewDeadlineReminders = it },
                        enabled = notificationsEnabled
                    )
                }
                
                // Author Book Published Notification
                item {
                    NotificationPreferenceCard(
                        title = "Author Book Published",
                        description = "Notify when followed author publishes a book",
                        checked = authorBookPublished,
                        onCheckedChange = { authorBookPublished = it },
                        enabled = notificationsEnabled
                    )
                }
                
                // Favorite Genres Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Favorite Genres",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (genresLoading && favoriteGenres.isEmpty()) {
                                Box(
                        modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                }
                            } else if (selectedGenres.isEmpty()) {
                                Text(
                                    text = "No favorite genres selected yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                // Show selected genres
                                val selectedGenresList = favoriteGenres.filter { it.id in selectedGenres }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    selectedGenresList.forEach { genre ->
                                        Row(
                        modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = genre.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                    )
                }
                                    }
                                }
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    navController.navigate(com.example.booknest.navigation.Screen.FavoriteGenres.route)
                                },
                        modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Favorite Genres")
                            }
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
                                emailNotifications = emailNotifications,
                                notificationPreferences = com.example.booknest.network.NotificationPreferences(
                                    friendRequests = friendRequests,
                                    friendRequestAccepted = friendRequestAccepted,
                                    applicationApproved = applicationApproved,
                                    applicationRejected = applicationRejected,
                                    reviewDeadlineReminders = reviewDeadlineReminders,
                                    authorBookPublished = authorBookPublished
                                )
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
fun NotificationPreferenceCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
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
            Column(modifier = Modifier.weight(1f)) {
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
            Switch(
                checked = checked && enabled,
                onCheckedChange = { if (enabled) onCheckedChange(it) },
                enabled = enabled
            )
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
