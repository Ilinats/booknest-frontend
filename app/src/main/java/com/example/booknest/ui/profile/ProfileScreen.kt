package com.example.booknest.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.content.Intent
import android.net.Uri
import coil.compose.AsyncImage
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.*
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.ProfileViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.ui.books.BookItem
import com.example.booknest.ui.books.SimpleBookItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    userId: String? = null,
    username: String? = null,
    profileViewModel: ProfileViewModel = getViewModel(),
) {
    val currentUser by sessionManager.currentUser.collectAsState()
    val isOwnProfile =
        (userId == null && username == null) || userId == currentUser?.id || username == currentUser?.username

    val isFromBottomNav = userId == null && username == null && isOwnProfile

    val profileState by profileViewModel.profileState.collectAsState()
    val myProfile by profileViewModel.myProfile.collectAsState()

    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    LaunchedEffect(userId, username, isLoggedIn) {
        if (isLoggedIn != true) {
            return@LaunchedEffect
        }

        when {
            username != null -> {
                if (username == currentUser?.username) {
                    profileViewModel.loadMyProfile()
                } else {
                    profileViewModel.loadUserProfile(username)
                }
            }

            userId != null -> {
                if (userId == currentUser?.id || userId == currentUser?.username) {
                    profileViewModel.loadMyProfile()
                } else {
                    profileViewModel.loadUserProfile(userId)
                }
            }

            else -> {
                currentUser?.let { user ->
                    profileViewModel.loadMyProfile()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.shadow(elevation = 4.dp)
            ) {
                TopAppBar(
                    title = { Text("Profile") },
                    navigationIcon = {
                        BackButton(onClick = { navController.popBackStack() })
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
            }
        }
    ) { paddingValues ->
        val currentState = profileState
        LaunchedEffect(currentState) {
            println("DEBUG: ProfileScreen state changed: ${currentState::class.simpleName}")
            if (currentState is com.example.booknest.viewmodel.ProfileUiState.Success) {
                println("DEBUG: ProfileScreen Success state - profile: username=${currentState.profile.username}, firstName=${currentState.profile.firstName}")
            }
        }
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
                                profileViewModel.loadMyProfile()
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
                    profileViewModel = profileViewModel,
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is com.example.booknest.viewmodel.ProfileUiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Unknown state: ${currentState::class.simpleName}")
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    profile: UserProfileResponse,
    isOwnProfile: Boolean,
    currentUser: com.example.booknest.domain.model.response.UserResponse?,
    profileViewModel: ProfileViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val authorBooks by profileViewModel.authorBooks.collectAsState()
    val authorBooksLoading by profileViewModel.authorBooksLoading.collectAsState()
    val userReviews by remember {
        mutableStateOf<List<com.example.booknest.domain.model.response.ReviewResponse>>(
            emptyList()
        )
    }
    val myRecentActivity by profileViewModel.myRecentActivity.collectAsState()
    val userActivity = myRecentActivity

    var isBooksExpanded by remember { mutableStateOf(false) }
    var showReviews by remember { mutableStateOf(false) }
    var showActivity by remember { mutableStateOf(false) }

    var friendshipStatus by remember {
        mutableStateOf<com.example.booknest.domain.model.response.FriendshipStatusResponse?>(
            null
        )
    }
    var isFollowingAuthor by remember { mutableStateOf(false) }
    var followerCount by remember { mutableStateOf(0) }
    var favoriteGenres by remember {
        mutableStateOf<List<com.example.booknest.domain.model.response.GenreResponse>>(
            emptyList()
        )
    }

    val friendViewModel: com.example.booknest.viewmodel.FriendViewModel =
        org.koin.androidx.compose.getViewModel()
    val authorFollowViewModel: com.example.booknest.viewmodel.AuthorFollowViewModel =
        org.koin.androidx.compose.getViewModel()
    val reviewViewModel: com.example.booknest.viewmodel.ReviewViewModel =
        org.koin.androidx.compose.getViewModel()
    val favoriteGenresViewModel: com.example.booknest.viewmodel.FavoriteGenresViewModel =
        org.koin.androidx.compose.getViewModel()
    val unfriendLoading by friendViewModel.isLoading.collectAsState()
    val authorFollowLoading by authorFollowViewModel.isLoading.collectAsState()
    val loadingAuthors by authorFollowViewModel.loadingAuthors.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(profile.id, profile.userId, profile.userType, isOwnProfile) {
        if (profile.userType == "author") {
            val authorId = profile.userId ?: profile.id
            val authorName = listOfNotNull(profile.firstName, profile.lastName)
                .joinToString(" ")
                .ifBlank { profile.username ?: "" }
            if (authorName.isNotBlank()) {
                profileViewModel.loadAuthorBooks(authorId, authorName)
            }
        }
    }

    LaunchedEffect(profile.userId, profile.id, isOwnProfile) {
        if (!isOwnProfile) {
            val targetUserId = profile.userId ?: profile.id
            friendViewModel.getFriendshipStatus(targetUserId) { status ->
                friendshipStatus = status
            }

            if (profile.userType == "author") {
                authorFollowViewModel.checkIfFollowingAuthor(targetUserId) { following ->
                    isFollowingAuthor = following
                }
                authorFollowViewModel.loadAuthorFollowers(targetUserId)
            }
        }
    }

    LaunchedEffect(Unit) {
        authorFollowViewModel.error.collectLatest { error ->
            error?.let {
                if (profile.userType == "author") {
                    val authorId = profile.userId ?: profile.id
                    authorFollowViewModel.checkIfFollowingAuthor(authorId) { following ->
                        isFollowingAuthor = following
                    }
                }
            }
        }
    }

    LaunchedEffect(isOwnProfile, profile.userType) {
        if (isOwnProfile && profile.userType == "reader") {
            favoriteGenresViewModel.loadGenres()
        }
    }

    val authorFollowers by authorFollowViewModel.authorFollowers.collectAsState()
    LaunchedEffect(authorFollowers) {
        if (!isOwnProfile && profile.userType == "author") {
            followerCount = authorFollowers.size
        }
    }

    val selectedGenreIds by favoriteGenresViewModel.selectedGenreIds.collectAsState()
    val allGenres by favoriteGenresViewModel.genres.collectAsState()
    LaunchedEffect(selectedGenreIds, allGenres) {
        if (isOwnProfile && profile.userType == "reader") {
            favoriteGenres = allGenres.filter { it.id in selectedGenreIds }
        }
    }

    LaunchedEffect(profile.userId, profile.id, isOwnProfile) {
        if (!isOwnProfile) {
            val targetUserId = profile.userId ?: profile.id
            reviewViewModel.loadUserReviews(targetUserId)
        }
    }

    LaunchedEffect(profile.userId, profile.id, profile.username, isOwnProfile) {
        if (isOwnProfile) {
            profileViewModel.loadMyRecentActivity(days = 7)
        } else {
            profile.username?.let { username ->
                profileViewModel.loadUserRecentActivity(username, days = 7)
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

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnhancedProfileHeader(
                profile = profile,
                isOwnProfile = isOwnProfile,
                navController = navController,
                friendshipStatus = friendshipStatus,
                isFollowingAuthor = isFollowingAuthor,
                followerCount = followerCount,
                unfriendLoading = unfriendLoading,
                authorFollowLoading = if (profile.userType == "author") {
                    val authorId = profile.userId ?: profile.id
                    loadingAuthors.contains(authorId)
                } else false,
                onFriendAction = { action ->
                    val targetUserId = profile.userId ?: profile.id
                    when (action) {
                        FriendAction.ADD -> friendViewModel.sendFriendRequest(
                            profile.username ?: ""
                        )

                        FriendAction.UNFRIEND -> {
                            friendViewModel.unfriendUser(targetUserId)
                            scope.launch {
                                delay(800)
                                friendViewModel.getFriendshipStatus(targetUserId) { status ->
                                    friendshipStatus = status
                                }
                                com.example.booknest.ui.toast.GlobalToastHandler.showSuccess("User unfriended successfully")
                            }
                        }
                    }
                },
                onFollowAction = { follow ->
                    val authorId = profile.userId ?: profile.id
                    isFollowingAuthor = follow
                    if (follow) {
                        authorFollowViewModel.followAuthor(authorId)
                    } else {
                        authorFollowViewModel.unfollowAuthor(authorId)
                    }
                }
            )

            if (!profile.bio.isNullOrBlank() || (profile.socialMedia != null && hasSocialMediaLinks(
                    profile.socialMedia
                ))
            ) {
                BioSection(
                    bio = profile.bio,
                    socialMedia = profile.socialMedia
                )
            }

            profile.stats?.let { stats ->
                EnhancedProfileStatsSection(
                    stats = stats,
                    isOwnProfile = isOwnProfile,
                    profile = profile,
                    favoriteGenres = if (isOwnProfile && profile.userType == "reader") favoriteGenres else emptyList(),
                    followerCount = if (!isOwnProfile && profile.userType == "author") followerCount else null
                )
            }

            if (profile.userType == "author") {
                AuthorBooksSection(
                    books = authorBooks,
                    isLoading = authorBooksLoading,
                    isExpanded = isBooksExpanded,
                    onExpandToggle = { isBooksExpanded = !isBooksExpanded },
                    navController = navController
                )
            }

            if (userActivity.isNotEmpty()) {
                RecentActivitySection(
                    activities = userActivity.take(5),
                    onViewAll = { showActivity = true },
                    navController = navController
                )
            }

            if (!isOwnProfile) {
                val reviews = reviewViewModel.userReviews.collectAsState().value
                if (reviews.isNotEmpty()) {
                    ReviewsWrittenSection(
                        reviews = reviews,
                        onViewAll = {
                            val targetUserId = profile.userId ?: profile.id
                            val userName = listOfNotNull(profile.firstName, profile.lastName)
                                .joinToString(" ")
                                .ifBlank { profile.username ?: "User" }
                            navController.navigate(
                                Screen.UserReviews.createRoute(
                                    targetUserId,
                                    userName
                                )
                            )
                        }
                    )
                }
            }

            if (profile.birthDate != null) {
                ProfileDetailsSection(profile = profile)
            }
        }

    }
}

enum class FriendAction {
    ADD, UNFRIEND
}

@Composable
fun EnhancedProfileHeader(
    profile: UserProfileResponse,
    isOwnProfile: Boolean,
    navController: NavController,
    friendshipStatus: com.example.booknest.domain.model.response.FriendshipStatusResponse?,
    isFollowingAuthor: Boolean,
    followerCount: Int,
    unfriendLoading: Boolean = false,
    authorFollowLoading: Boolean = false,
    onFriendAction: (FriendAction) -> Unit,
    onFollowAction: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box {
                if (profile.avatarUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial =
                            (profile.firstName?.firstOrNull() ?: profile.username?.firstOrNull()
                            ?: '?')
                                .uppercaseChar()
                        Text(
                            text = initial.toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                if (isOwnProfile) {
                    IconButton(
                        onClick = { navController.navigate("profile_edit") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val displayName = listOfNotNull(
                    profile.firstName?.takeIf { it.isNotBlank() },
                    profile.lastName?.takeIf { it.isNotBlank() }
                )
                    .joinToString(" ")
                    .takeIf { it.isNotBlank() }
                    ?: profile.username?.takeIf { it.isNotBlank() }
                    ?: "User"

                Text(
                    text = displayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                profile.username?.takeIf { it.isNotBlank() }?.let { username ->
                    Text(
                        text = "@$username",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = profile.userType?.replaceFirstChar { it.uppercase() }
                                ?: "Unknown",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (profile.isVerified) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                if (profile.userType == "author") {
                    Text(
                        text = "$followerCount ${if (followerCount == 1) "follower" else "followers"}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            profile.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                Text(
                    text = bio,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            if (!isOwnProfile) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (profile.userType == "author") {
                        Button(
                            onClick = { onFollowAction(!isFollowingAuthor) },
                            modifier = Modifier.weight(1f),
                            enabled = !authorFollowLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowingAuthor)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (authorFollowLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            } else {
                                Icon(
                                    if (isFollowingAuthor) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(if (isFollowingAuthor) "Following" else "Follow")
                        }
                    } else {
                        when (friendshipStatus?.status) {
                            "accepted" -> {
                                OutlinedButton(
                                    onClick = { onFriendAction(FriendAction.UNFRIEND) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !unfriendLoading
                                ) {
                                    if (unfriendLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    } else {
                                        Icon(
                                            Icons.Default.PersonRemove,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text("Unfriend")
                                }
                            }

                            "pending" -> {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.weight(1f),
                                    enabled = false
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (friendshipStatus.isRequester) "Request Sent" else "Pending")
                                }
                            }

                            else -> {
                                Button(
                                    onClick = { onFriendAction(FriendAction.ADD) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Friend")
                                }
                            }
                        }
                    }
                }

            } else {
                Button(
                    onClick = { navController.navigate("profile_edit") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }

            Text(
                text = "Member since ${formatDate(profile.createdAt)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BioSection(
    bio: String?,
    socialMedia: SocialMediaResponse?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "About",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            bio?.takeIf { it.isNotBlank() }?.let { bioText ->
                Text(
                    text = bioText,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            }

            socialMedia?.let { social ->
                if (hasSocialMediaLinks(social)) {
                    Divider()
                    Text(
                        text = "Social Media",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        social.instagram?.takeIf { it.isNotBlank() }?.let { url ->
                            SocialMediaLink(
                                platform = "Instagram",
                                url = url,
                                icon = Icons.Default.Share
                            )
                        }
                        social.tiktok?.takeIf { it.isNotBlank() }?.let { url ->
                            SocialMediaLink(
                                platform = "TikTok",
                                url = url,
                                icon = Icons.Default.VideoLibrary
                            )
                        }
                        social.youtube?.takeIf { it.isNotBlank() }?.let { url ->
                            SocialMediaLink(
                                platform = "YouTube",
                                url = url,
                                icon = Icons.Default.PlayArrow
                            )
                        }
                        social.goodreads?.takeIf { it.isNotBlank() }?.let { url ->
                            SocialMediaLink(
                                platform = "Goodreads",
                                url = url,
                                icon = Icons.Default.Book
                            )
                        }
                        social.custom?.forEach { customLink ->
                            SocialMediaLink(
                                platform = customLink.platform,
                                url = customLink.url,
                                icon = Icons.Default.Link
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialMediaLink(
    platform: String,
    url: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val context = LocalContext.current
    AssistChip(
        onClick = {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e: Exception) {
            }
        },
        label = { Text(platform, fontSize = 12.sp) },
        leadingIcon = {
            Icon(icon, contentDescription = platform, modifier = Modifier.size(16.dp))
        }
    )
}

private fun hasSocialMediaLinks(social: SocialMediaResponse): Boolean {
    return !social.instagram.isNullOrBlank() ||
            !social.tiktok.isNullOrBlank() ||
            !social.youtube.isNullOrBlank() ||
            !social.goodreads.isNullOrBlank() ||
            !social.custom.isNullOrEmpty()
}

@Composable
fun EnhancedProfileStatsSection(
    stats: UserStatsDataResponse,
    isOwnProfile: Boolean,
    profile: UserProfileResponse,
    favoriteGenres: List<com.example.booknest.domain.model.response.GenreResponse> = emptyList(),
    followerCount: Int? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Statistics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (stats.userType == "author") {
                if (isOwnProfile) {
                    EnhancedAuthorStatsGrid(stats = stats)
                } else {
                    EnhancedPublicAuthorStatsGrid(stats = stats, followerCount = followerCount)
                }
            } else {
                EnhancedReaderStatsGrid(
                    stats = stats,
                    profile = profile,
                    favoriteGenres = favoriteGenres
                )
            }
        }
    }
}

@Composable
fun EnhancedAuthorStatsGrid(stats: UserStatsDataResponse) {
    val approvalRate = if (stats.totalApplications > 0) {
        (stats.approvedApplications.toDouble() / stats.totalApplications * 100).toInt()
    } else 0

    val statItems = listOf(
        "Total Books" to (stats.totalBooks ?: 0),
        "Published Books" to (stats.publishedBooks ?: 0),
        "Draft Books" to (stats.draftBooks ?: 0),
        "Total Applications" to stats.totalApplications,
        "Approval Rate" to approvalRate,
        "Average Rating" to (stats.averageRating ?: 0.0),
        "Total Reviews" to (stats.totalReviews ?: 0)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (title, value) ->
                    StatCard(
                        title = title,
                        value = when {
                            title == "Average Rating" -> String.format(
                                "%.1f",
                                (value as? Number)?.toDouble() ?: 0.0
                            )

                            title == "Approval Rate" -> "${(value as? Number)?.toInt() ?: 0}%"
                            else -> value.toString()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedPublicAuthorStatsGrid(stats: UserStatsDataResponse, followerCount: Int? = null) {
    val statItems = buildList {
        add("Published Books" to (stats.publishedBooks ?: 0))
        add("Total Reviews" to (stats.totalReviews ?: 0))
        add("Average Rating" to (stats.averageRating ?: 0.0))
        followerCount?.let { count ->
            add("Followers" to count)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (title, value) ->
                    StatCard(
                        title = title,
                        value = if (title == "Average Rating") String.format(
                            "%.1f",
                            value
                        ) else value.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedReaderStatsGrid(
    stats: UserStatsDataResponse,
    profile: UserProfileResponse,
    favoriteGenres: List<com.example.booknest.domain.model.response.GenreResponse> = emptyList()
) {
    val statItems = listOf(
        "Books Read" to (stats.completedReads ?: 0),
        "Reviews Written" to (stats.totalReviews ?: 0),
        "Total Applications" to stats.totalApplications,
        "Approved Applications" to stats.approvedApplications
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (title, value) ->
                    StatCard(
                        title = title,
                        value = value.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }

    if (favoriteGenres.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Favorite Genres",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            favoriteGenres.chunked(3).forEach { rowGenres ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowGenres.forEach { genre ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = genre.name,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAuthorStatsGrid(
    stats: UserStatsDataResponse
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (title, value) ->
                    StatCard(
                        title = title,
                        value = if (title == "Average Rating") String.format(
                            "%.1f",
                            value
                        ) else value.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfilePublicAuthorStatsGrid(
    stats: UserStatsDataResponse
) {
    val statItems = listOf(
        "Total Books" to (stats.totalBooks ?: 0),
        "Published Books" to (stats.publishedBooks ?: 0),
        "Total Reviews" to (stats.totalReviews ?: 0),
        "Average Rating" to (stats.averageRating ?: 0.0)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (title, value) ->
                    StatCard(
                        title = title,
                        value = if (title == "Average Rating") String.format(
                            "%.1f",
                            value
                        ) else value.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileReaderStatsGrid(
    stats: UserStatsDataResponse
) {
    val statItems = listOf(
        "Total Applications" to stats.totalApplications,
        "Approved Applications" to stats.approvedApplications,
        "Pending Applications" to stats.pendingApplications,
        "Completed Reads" to (stats.completedReads ?: 0),
        "Reviews Written" to (stats.totalReviews ?: 0)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (title, value) ->
                    StatCard(
                        title = title,
                        value = value.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileDetailsSection(
    profile: UserProfileResponse
) {
    val hasDetails = profile.birthDate != null
    if (!hasDetails) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Additional Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            profile.birthDate?.let { birthDate ->
                ProfileDetailItem(
                    label = "Birth Date",
                    value = formatDate(birthDate),
                    icon = Icons.Default.CalendarToday
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

@Composable
fun AuthorBooksSection(
    books: List<RecommendedBookResponse>,
    isLoading: Boolean,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Books",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (books.size > 5) {
                TextButton(onClick = onExpandToggle) {
                    Text(if (isExpanded) "Show Less" else "Show All (${books.size})")
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (books.isEmpty()) {
            Text(
                text = "No books available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            val booksToShow = if (isExpanded || books.size <= 5) books else books.take(5)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(booksToShow) { book ->
                    SimpleBookItem(book = book, navController = navController)
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    activities: List<com.example.booknest.domain.model.response.UserActivityResponse>,
    onViewAll: () -> Unit,
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            activities.forEach { activity ->
                com.example.booknest.ui.components.ActivityItem(
                    activity = activity,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun ReviewsWrittenSection(
    reviews: List<com.example.booknest.domain.model.response.ReviewResponse>,
    onViewAll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reviews Written",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onViewAll) {
                    Text("View All")
                }
            }

            reviews.take(5).forEach { review ->
                ReviewItem(review = review)
            }
        }
    }
}

@Composable
fun ReviewItem(
    review: com.example.booknest.domain.model.response.ReviewResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                review.application?.bookTitle?.let { bookTitle ->
                    Text(
                        text = bookTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(review.rating) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Star",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            review.reviewContent?.take(100)?.let { excerpt ->
                Text(
                    text = excerpt + if (review.reviewContent.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = formatDate(review.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            java.util.Locale.getDefault()
        )
        val outputFormat = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: java.util.Date())
    } catch (e: Exception) {
        dateString
    }
}
