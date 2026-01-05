package com.example.booknest.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.*
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.ProfileViewModel
import com.example.booknest.viewmodel.AuthorFollowViewModel
import com.example.booknest.viewmodel.FriendViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import com.example.booknest.ui.profile.components.header.EnhancedProfileHeader
import com.example.booknest.ui.profile.components.header.FriendAction
import com.example.booknest.ui.profile.components.bio.BioSection
import com.example.booknest.ui.profile.components.bio.hasSocialMediaLinks
import com.example.booknest.ui.account.components.stats.EnhancedProfileStatsSection
import com.example.booknest.ui.profile.components.sections.AuthorBooksSection
import com.example.booknest.ui.profile.components.sections.RecentActivitySection
import com.example.booknest.ui.profile.components.sections.ReviewsWrittenSection
import com.example.booknest.ui.profile.components.sections.ProfileDetailsSection

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
            if (currentState is com.example.booknest.ui.state.UiState.Success) {
                println("DEBUG: ProfileScreen Success state - profile: username=${currentState.data.username}, firstName=${currentState.data.firstName}")
            }
        }
        when (currentState) {
            is com.example.booknest.ui.state.UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is com.example.booknest.ui.state.UiState.Error -> {
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

            is com.example.booknest.ui.state.UiState.Success -> {
                ProfileContent(
                    profile = currentState.data,
                    isOwnProfile = isOwnProfile,
                    currentUser = currentUser,
                    profileViewModel = profileViewModel,
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is com.example.booknest.ui.state.UiState.Idle -> {
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

    val friendshipStatuses by friendViewModel.friendshipStatuses.collectAsState()
    val followingStatus by authorFollowViewModel.followingStatus.collectAsState()

    LaunchedEffect(profile.userId, profile.id, isOwnProfile) {
        if (!isOwnProfile) {
            val targetUserId = profile.userId ?: profile.id
            friendViewModel.getFriendshipStatus(targetUserId)

            if (profile.userType == "author") {
                authorFollowViewModel.checkIfFollowingAuthor(targetUserId)
                authorFollowViewModel.loadAuthorFollowers(targetUserId)
            }
        }
    }

    // Update local state from ViewModel state flows
    LaunchedEffect(friendshipStatuses, profile.userId, profile.id) {
        if (!isOwnProfile) {
            val targetUserId = profile.userId ?: profile.id
            friendshipStatus = friendshipStatuses[targetUserId]
        }
    }

    LaunchedEffect(followingStatus, profile.userId, profile.id) {
        if (!isOwnProfile && profile.userType == "author") {
            val targetUserId = profile.userId ?: profile.id
            isFollowingAuthor = followingStatus[targetUserId] ?: false
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
                currentUserIsAuthor = currentUser?.userType == "author",
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
                                friendViewModel.getFriendshipStatus(targetUserId)
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
