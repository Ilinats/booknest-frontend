package com.example.booknest.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.data.AuthManager
import com.example.booknest.network.UserProfile
import com.example.booknest.viewmodel.ProfileViewModel
import com.example.booknest.viewmodel.ProfileViewModelFactory
import com.example.booknest.viewmodel.EmailVerificationViewModel
import com.example.booknest.viewmodel.EmailVerificationViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authManager: AuthManager,
    userId: String? = null,
    profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(authManager)
    ),
    emailVerificationViewModel: EmailVerificationViewModel = viewModel(
        factory = EmailVerificationViewModelFactory(authManager)
    )
) {
    val currentUser = authManager.getCurrentUser()
    val isOwnProfile = userId == null || userId == currentUser?.id
    
    val profileState by profileViewModel.profileState.collectAsState()
    val currentProfile by profileViewModel.currentProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        profileViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    // Handle email verification snackbar messages
    LaunchedEffect(Unit) {
        emailVerificationViewModel.snackbarMessage.collectLatest { message ->
            message?.let {
                snackbarHostState.showSnackbar(it)
                emailVerificationViewModel.clearMessage()
            }
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            profileViewModel.loadUserProfile(userId)
            // Check email verification status for own profile
            if (isOwnProfile) {
                emailVerificationViewModel.checkVerificationStatus(userId)
            }
        } else {
            currentUser?.let { user ->
                profileViewModel.loadUserProfile(user.id)
                // Check email verification status for own profile
                if (isOwnProfile) {
                    emailVerificationViewModel.checkVerificationStatus(user.id)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwnProfile) {
                        IconButton(onClick = { 
                            navController.navigate("profile_edit")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val currentState = profileState
        when (currentState) {
            is com.example.booknest.viewmodel.ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is com.example.booknest.viewmodel.ProfileUiState.Error -> {
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
                            text = currentState.message,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { 
                            if (userId != null) {
                                profileViewModel.loadUserProfile(userId)
                            } else {
                                currentUser?.let { user ->
                                    profileViewModel.loadUserProfile(user.id)
                                }
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is com.example.booknest.viewmodel.ProfileUiState.Success -> {
                ProfileContent(
                    profile = currentState.profile,
                    isOwnProfile = isOwnProfile,
                    currentUser = currentUser,
                    emailVerificationViewModel = emailVerificationViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {}
        }
    }
}

@Composable
fun ProfileContent(
    profile: UserProfile,
    isOwnProfile: Boolean,
    currentUser: com.example.booknest.network.UserData?,
    emailVerificationViewModel: EmailVerificationViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Profile Header
        ProfileHeader(profile = profile, isOwnProfile = isOwnProfile)
        
        // Email Verification Section (only for own profile)
        if (isOwnProfile && currentUser?.email != null) {
            EmailVerificationSection(
                emailVerificationViewModel = emailVerificationViewModel,
                userEmail = currentUser.email
            )
        }
        
        // Profile Stats
        profile.stats?.let { stats ->
            ProfileStatsSection(stats = stats, isOwnProfile = isOwnProfile)
        }
        
        // Profile Details
        ProfileDetailsSection(profile = profile)
    }
}

@Composable
fun ProfileHeader(
    profile: UserProfile,
    isOwnProfile: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            AsyncImage(
                model = profile.avatarUrl ?: "https://via.placeholder.com/120",
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            // Name and Username
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${profile.firstName ?: ""} ${profile.lastName ?: ""}".trim(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${profile.username}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // User Type Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = profile.userType.replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (profile.isVerified) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Bio
            profile.bio?.let { bio ->
                if (bio.isNotBlank()) {
                    Text(
                        text = bio,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Member Since
            Text(
                text = "Member since ${formatDate(profile.createdAt)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileStatsSection(
    stats: com.example.booknest.network.UserStats,
    isOwnProfile: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Statistics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (stats.userType == "author") {
                if (isOwnProfile) {
                    ProfileAuthorStatsGrid(stats = stats)
                } else {
                    // For other authors, show only public stats
                    ProfilePublicAuthorStatsGrid(stats = stats)
                }
            } else {
                ProfileReaderStatsGrid(stats = stats)
            }
        }
    }
}

@Composable
fun ProfileAuthorStatsGrid(
    stats: com.example.booknest.network.UserStats
) {
    val statItems = listOf(
        "Total Books" to (stats.totalBooks ?: 0),
        "Published Books" to (stats.publishedBooks ?: 0),
        "Draft Books" to (stats.draftBooks ?: 0),
        "Total Applications" to stats.totalApplications,
        "Approved Applications" to stats.approvedApplications,
        "Pending Applications" to stats.pendingApplications,
        "Total Reviews" to (stats.totalReviews ?: 0),
        "Average Rating" to (stats.averageRating ?: 0.0)
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statItems.size) { index ->
            val (title, value) = statItems[index]
            StatCard(
                title = title,
                value = if (title == "Average Rating") String.format("%.1f", value) else value.toString()
            )
        }
    }
}

@Composable
fun ProfilePublicAuthorStatsGrid(
    stats: com.example.booknest.network.UserStats
) {
    val statItems = listOf(
        "Total Books" to (stats.totalBooks ?: 0),
        "Published Books" to (stats.publishedBooks ?: 0),
        "Total Reviews" to (stats.totalReviews ?: 0),
        "Average Rating" to (stats.averageRating ?: 0.0)
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statItems.size) { index ->
            val (title, value) = statItems[index]
            StatCard(
                title = title,
                value = if (title == "Average Rating") String.format("%.1f", value) else value.toString()
            )
        }
    }
}

@Composable
fun ProfileReaderStatsGrid(
    stats: com.example.booknest.network.UserStats
) {
    val statItems = listOf(
        "Total Applications" to stats.totalApplications,
        "Approved Applications" to stats.approvedApplications,
        "Pending Applications" to stats.pendingApplications,
        "Completed Reads" to (stats.completedReads ?: 0),
        "Reviews Written" to (stats.totalReviews ?: 0)
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(150.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statItems.size) { index ->
            val (title, value) = statItems[index]
            StatCard(
                title = title,
                value = value.toString()
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileDetailsSection(
    profile: UserProfile
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profile Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            ProfileDetailItem(
                label = "Username",
                value = profile.username
            )
            
            ProfileDetailItem(
                label = "User Type",
                value = profile.userType.replaceFirstChar { it.uppercase() }
            )
            
            ProfileDetailItem(
                label = "Member Since",
                value = formatDate(profile.createdAt)
            )
            
            if (profile.isVerified) {
                ProfileDetailItem(
                    label = "Verification Status",
                    value = "Verified",
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    }
}

@Composable
fun ProfileDetailItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: java.util.Date())
    } catch (e: Exception) {
        dateString
    }
}
