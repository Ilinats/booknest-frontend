package com.example.booknest.ui.account

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.viewmodel.FavoriteGenresViewModel
import org.koin.androidx.compose.getViewModel
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.enums.NotificationType
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.account.components.privacy.NotificationPreferenceCard
import com.example.booknest.ui.account.components.privacy.PrivacySettingCard
import com.example.booknest.ui.account.components.privacy.address.AddressManagementSection
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
    var hasJustSaved by remember { mutableStateOf(false) }

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

            profile.notificationPreferences?.let { enabledTypes ->
                val allEnabled = enabledTypes.isEmpty()
                
                val newFriendRequests = allEnabled || enabledTypes.contains(NotificationType.FRIEND_REQUEST_RECEIVED)
                val newFriendRequestAccepted = allEnabled || enabledTypes.contains(NotificationType.FRIEND_REQUEST_ACCEPTED)
                val newApplicationApproved = allEnabled || enabledTypes.contains(NotificationType.APPLICATION_APPROVED)
                val newApplicationRejected = allEnabled || enabledTypes.contains(NotificationType.APPLICATION_REJECTED)
                val newReviewDeadlineReminders = allEnabled || enabledTypes.contains(NotificationType.REVIEW_DEADLINE_REMINDER)
                val newAuthorBookPublished = allEnabled || enabledTypes.contains(NotificationType.AUTHOR_BOOK_PUBLISHED)

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

    LaunchedEffect(isLoading, error) {
        if (hasJustSaved && !isLoading && error == null) {
            hasJustSaved = false
            navController.popBackStack()
        } else if (hasJustSaved && !isLoading && error != null) {
            hasJustSaved = false
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-175).dp, y = (-175).dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-135).dp, y = (-135).dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 175.dp, y = 175.dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 135.dp, y = 135.dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
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
                            modifier = Modifier
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
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
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
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
                                        navController.navigate(Screen.FavoriteGenres.route)
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
                                val enabledTypes = mutableListOf<String>()
                                if (friendRequests) enabledTypes.add(NotificationType.FRIEND_REQUEST_RECEIVED)
                                if (friendRequestAccepted) enabledTypes.add(NotificationType.FRIEND_REQUEST_ACCEPTED)
                                if (applicationApproved) enabledTypes.add(NotificationType.APPLICATION_APPROVED)
                                if (applicationRejected) enabledTypes.add(NotificationType.APPLICATION_REJECTED)
                                if (reviewDeadlineReminders) enabledTypes.add(NotificationType.REVIEW_DEADLINE_REMINDER)
                                if (authorBookPublished) enabledTypes.add(NotificationType.AUTHOR_BOOK_PUBLISHED)
                                
                                profileViewModel.updateNotificationSettings(
                                    notificationsEnabled = notificationsEnabled,
                                    emailNotifications = emailNotifications,
                                    notificationPreferences = enabledTypes
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
                                hasJustSaved = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = hasChanges && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Save Settings",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
