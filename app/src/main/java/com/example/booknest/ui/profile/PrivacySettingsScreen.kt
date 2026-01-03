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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import com.example.booknest.ui.theme.SkyBluePeriwinkle
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.viewmodel.FavoriteGenresViewModel
import org.koin.androidx.compose.getViewModel
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.viewmodel.ProfileViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    profileViewModel: ProfileViewModel = getViewModel()
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

    var friendRequests by remember { mutableStateOf(true) }
    var friendRequestAccepted by remember { mutableStateOf(true) }
    var applicationApproved by remember { mutableStateOf(true) }
    var applicationRejected by remember { mutableStateOf(true) }
    var reviewDeadlineReminders by remember { mutableStateOf(true) }
    var authorBookPublished by remember { mutableStateOf(true) }

    var initialActivityPrivacy by remember { mutableStateOf<String?>(null) }
    var initialProfilePrivacy by remember { mutableStateOf<String?>(null) }
    var initialReadingListPrivacy by remember { mutableStateOf<String?>(null) }
    var initialReviewsPrivacy by remember { mutableStateOf<String?>(null) }
    var initialNotificationsEnabled by remember { mutableStateOf<Boolean?>(null) }
    var initialEmailNotifications by remember { mutableStateOf<Boolean?>(null) }
    var initialFriendRequests by remember { mutableStateOf<Boolean?>(null) }
    var initialFriendRequestAccepted by remember { mutableStateOf<Boolean?>(null) }
    var initialApplicationApproved by remember { mutableStateOf<Boolean?>(null) }
    var initialApplicationRejected by remember { mutableStateOf<Boolean?>(null) }
    var initialReviewDeadlineReminders by remember { mutableStateOf<Boolean?>(null) }
    var initialAuthorBookPublished by remember { mutableStateOf<Boolean?>(null) }

    val favoriteGenresViewModel: FavoriteGenresViewModel = getViewModel()
    val favoriteGenres by favoriteGenresViewModel.genres.collectAsState()
    val selectedGenres by favoriteGenresViewModel.selectedGenreIds.collectAsState()
    val genresLoading by favoriteGenresViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadMyProfile()
        profileViewModel.loadAddresses()
        favoriteGenresViewModel.loadGenres()
    }

    LaunchedEffect(myProfile) {
        myProfile?.let { profile ->
            val newActivityPrivacy = profile.activityPrivacy ?: "friends"
            val newProfilePrivacy = profile.profilePrivacy ?: "friends"
            val newReadingListPrivacy = profile.readingListPrivacy ?: "friends"
            val newReviewsPrivacy = profile.reviewsPrivacy ?: "public"
            val newNotificationsEnabled = profile.notificationsEnabled
            val newEmailNotifications = profile.emailNotifications

            activityPrivacy = newActivityPrivacy
            profilePrivacy = newProfilePrivacy
            readingListPrivacy = newReadingListPrivacy
            reviewsPrivacy = newReviewsPrivacy
            notificationsEnabled = newNotificationsEnabled
            emailNotifications = newEmailNotifications

            if (initialActivityPrivacy == null) {
                initialActivityPrivacy = newActivityPrivacy
                initialProfilePrivacy = newProfilePrivacy
                initialReadingListPrivacy = newReadingListPrivacy
                initialReviewsPrivacy = newReviewsPrivacy
                initialNotificationsEnabled = newNotificationsEnabled
                initialEmailNotifications = newEmailNotifications
            }

            profile.notificationPreferences?.let { prefs ->
                val newFriendRequests = prefs.friendRequests ?: true
                val newFriendRequestAccepted = prefs.friendRequestAccepted ?: true
                val newApplicationApproved = prefs.applicationApproved ?: true
                val newApplicationRejected = prefs.applicationRejected ?: true
                val newReviewDeadlineReminders = prefs.reviewDeadlineReminders ?: true
                val newAuthorBookPublished = prefs.authorBookPublished ?: true

                friendRequests = newFriendRequests
                friendRequestAccepted = newFriendRequestAccepted
                applicationApproved = newApplicationApproved
                applicationRejected = newApplicationRejected
                reviewDeadlineReminders = newReviewDeadlineReminders
                authorBookPublished = newAuthorBookPublished

                if (initialFriendRequests == null) {
                    initialFriendRequests = newFriendRequests
                    initialFriendRequestAccepted = newFriendRequestAccepted
                    initialApplicationApproved = newApplicationApproved
                    initialApplicationRejected = newApplicationRejected
                    initialReviewDeadlineReminders = newReviewDeadlineReminders
                    initialAuthorBookPublished = newAuthorBookPublished
                }
            }
        }
    }

    val hasChanges = remember(
        activityPrivacy,
        profilePrivacy,
        readingListPrivacy,
        reviewsPrivacy,
        notificationsEnabled,
        emailNotifications,
        friendRequests,
        friendRequestAccepted,
        applicationApproved,
        applicationRejected,
        reviewDeadlineReminders,
        authorBookPublished,
        initialActivityPrivacy,
        initialProfilePrivacy,
        initialReadingListPrivacy,
        initialReviewsPrivacy,
        initialNotificationsEnabled,
        initialEmailNotifications,
        initialFriendRequests,
        initialFriendRequestAccepted,
        initialApplicationApproved,
        initialApplicationRejected,
        initialReviewDeadlineReminders,
        initialAuthorBookPublished
    ) {
        activityPrivacy != (initialActivityPrivacy ?: "friends") ||
                profilePrivacy != (initialProfilePrivacy ?: "friends") ||
                readingListPrivacy != (initialReadingListPrivacy ?: "friends") ||
                reviewsPrivacy != (initialReviewsPrivacy ?: "public") ||
                notificationsEnabled != (initialNotificationsEnabled ?: true) ||
                emailNotifications != (initialEmailNotifications ?: true) ||
                friendRequests != (initialFriendRequests ?: true) ||
                friendRequestAccepted != (initialFriendRequestAccepted ?: true) ||
                applicationApproved != (initialApplicationApproved ?: true) ||
                applicationRejected != (initialApplicationRejected ?: true) ||
                reviewDeadlineReminders != (initialReviewDeadlineReminders ?: true) ||
                authorBookPublished != (initialAuthorBookPublished ?: true)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1E9EE))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-175).dp, y = (-175).dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-135).dp, y = (-135).dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 175.dp, y = 175.dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 135.dp, y = 135.dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle)
            )

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
                    item {
                        Text(
                            text = "Privacy Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

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
                            onValueChange = { value: String -> activityPrivacy = value }
                        )
                    }

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
                            onValueChange = { value: String -> profilePrivacy = value }
                        )
                    }

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
                            onValueChange = { value: String -> reviewsPrivacy = value }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Notification Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

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
                                    Spacer(modifier = Modifier.height(2.dp))
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

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Notification Preferences",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        NotificationPreferenceCard(
                            title = "Friend Requests",
                            description = "Notify when someone sends a friend request",
                            checked = friendRequests,
                            onCheckedChange = { checked: Boolean -> friendRequests = checked },
                            enabled = notificationsEnabled
                        )
                    }

                    item {
                        NotificationPreferenceCard(
                            title = "Friend Request Accepted",
                            description = "Notify when a friend request is accepted",
                            checked = friendRequestAccepted,
                            onCheckedChange = { checked: Boolean ->
                                friendRequestAccepted = checked
                            },
                            enabled = notificationsEnabled
                        )
                    }

                    item {
                        NotificationPreferenceCard(
                            title = "Application Approved",
                            description = "Notify when book application is approved",
                            checked = applicationApproved,
                            onCheckedChange = { checked: Boolean -> applicationApproved = checked },
                            enabled = notificationsEnabled
                        )
                    }

                    item {
                        NotificationPreferenceCard(
                            title = "Application Rejected",
                            description = "Notify when book application is rejected",
                            checked = applicationRejected,
                            onCheckedChange = { checked: Boolean -> applicationRejected = checked },
                            enabled = notificationsEnabled
                        )
                    }

                    item {
                        NotificationPreferenceCard(
                            title = "Review Deadline Reminders",
                            description = "Reminders for review deadlines",
                            checked = reviewDeadlineReminders,
                            onCheckedChange = { checked: Boolean ->
                                reviewDeadlineReminders = checked
                            },
                            enabled = notificationsEnabled
                        )
                    }

                    item {
                        NotificationPreferenceCard(
                            title = "Author Book Published",
                            description = "Notify when followed author publishes a book",
                            checked = authorBookPublished,
                            onCheckedChange = { checked: Boolean -> authorBookPublished = checked },
                            enabled = notificationsEnabled
                        )
                    }

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
                                    val selectedGenresList =
                                        favoriteGenres.filter { it.id in selectedGenres }
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

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Shipping Addresses",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        val addresses by profileViewModel.addresses.collectAsState()
                        AddressManagementSection(
                            addresses = addresses,
                            onAddAddress = { streetAddress: String, city: String, postalCode: String, country: String, isPrimary: Boolean ->
                                profileViewModel.addAddress(
                                    streetAddress,
                                    city,
                                    postalCode,
                                    country,
                                    isPrimary
                                )
                            },
                            onUpdateAddress = { addressId: String, streetAddress: String?, city: String?, postalCode: String?, country: String?, isPrimary: Boolean? ->
                                profileViewModel.updateAddress(
                                    addressId,
                                    streetAddress,
                                    city,
                                    postalCode,
                                    country,
                                    isPrimary
                                )
                            },
                            onDeleteAddress = { addressId: String ->
                                profileViewModel.deleteAddress(addressId)
                            }
                        )
                    }

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
                                    notificationPreferences = com.example.booknest.domain.model.response.NotificationPreferencesResponse(
                                        friendRequests = friendRequests,
                                        friendRequestAccepted = friendRequestAccepted,
                                        applicationApproved = applicationApproved,
                                        applicationRejected = applicationRejected,
                                        reviewDeadlineReminders = reviewDeadlineReminders,
                                        authorBookPublished = authorBookPublished
                                    )
                                )
                                initialActivityPrivacy = activityPrivacy
                                initialProfilePrivacy = profilePrivacy
                                initialReadingListPrivacy = readingListPrivacy
                                initialReviewsPrivacy = reviewsPrivacy
                                initialNotificationsEnabled = notificationsEnabled
                                initialEmailNotifications = emailNotifications
                                initialFriendRequests = friendRequests
                                initialFriendRequestAccepted = friendRequestAccepted
                                initialApplicationApproved = applicationApproved
                                initialApplicationRejected = applicationRejected
                                initialReviewDeadlineReminders = reviewDeadlineReminders
                                initialAuthorBookPublished = authorBookPublished
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = hasChanges && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save Settings")
                            }
                        }
                    }

                    if (error != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = error ?: "Unknown error",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
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
                Spacer(modifier = Modifier.height(2.dp))
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            options.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentValue == value,
                        onClick = { onValueChange(value) }
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddressManagementSection(
    addresses: List<com.example.booknest.domain.model.response.ReaderAddressResponse>,
    onAddAddress: (String, String, String, String, Boolean) -> Unit,
    onUpdateAddress: (String, String?, String?, String?, String?, Boolean?) -> Unit,
    onDeleteAddress: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAddress by remember {
        mutableStateOf<com.example.booknest.domain.model.response.ReaderAddressResponse?>(
            null
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (addresses.isEmpty()) {
                Text(
                    text = "No addresses added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                addresses.forEach { address ->
                    AddressCard(
                        address = address,
                        onEdit = { editingAddress = address },
                        onDelete = { onDeleteAddress(address.id) }
                    )
                }
            }

            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Address")
            }
        }
    }

    if (showAddDialog) {
        AddEditAddressDialog(
            address = null,
            onDismiss = { showAddDialog = false },
            onSave = { streetAddress: String, city: String, postalCode: String, country: String, isPrimary: Boolean ->
                onAddAddress(streetAddress, city, postalCode, country, isPrimary)
                showAddDialog = false
            }
        )
    }

    if (editingAddress != null) {
        val address = editingAddress!!
        AddEditAddressDialog(
            address = address,
            onDismiss = { editingAddress = null },
            onSave = { streetAddress: String, city: String, postalCode: String, country: String, isPrimary: Boolean ->
                onUpdateAddress(address.id, streetAddress, city, postalCode, country, isPrimary)
                editingAddress = null
            }
        )
    }
}

@Composable
fun AddressCard(
    address: com.example.booknest.domain.model.response.ReaderAddressResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (address.isPrimary)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Address",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (address.isPrimary) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Primary",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Primary Address",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = address.streetAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${address.city}, ${address.postalCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!address.country.isNullOrBlank()) {
                    Text(
                        text = address.country,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddEditAddressDialog(
    address: com.example.booknest.domain.model.response.ReaderAddressResponse?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Boolean) -> Unit
) {
    var streetAddress by remember { mutableStateOf(address?.streetAddress ?: "") }
    var city by remember { mutableStateOf(address?.city ?: "") }
    var postalCode by remember { mutableStateOf(address?.postalCode ?: "") }
    var country by remember { mutableStateOf(address?.country ?: "") }
    var isPrimary by remember { mutableStateOf(address?.isPrimary ?: false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (address == null) "Add Address" else "Edit Address") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = { Text("Street Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Postal Code *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it }
                    )
                    Text("Set as primary address")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (streetAddress.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()) {
                        onSave(streetAddress, city, postalCode, country, isPrimary)
                    }
                },
                enabled = streetAddress.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
