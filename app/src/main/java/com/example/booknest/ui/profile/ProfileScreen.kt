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
import com.example.booknest.port.ToastNotifier
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.presentation.common.UiState
import com.example.booknest.viewmodel.profile.ProfileViewModel
import com.example.booknest.viewmodel.analytics.ReviewViewModel
import com.example.booknest.viewmodel.author.AuthorFollowViewModel
import com.example.booknest.viewmodel.books.ProfileAuthorBooksViewModel
import com.example.booknest.viewmodel.profile.ProfileActivityViewModel
import com.example.booknest.viewmodel.friends.FriendViewModel
import com.example.booknest.viewmodel.genres.FavoriteGenresViewModel
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
import com.example.booknest.ui.profile.components.sections.ProfileRecommendedBooksSection
import com.example.booknest.ui.profile.components.sections.RecentActivitySection
import com.example.booknest.ui.profile.components.sections.ReviewsWrittenSection
import com.example.booknest.ui.profile.components.sections.ProfileDetailsSection
import com.example.booknest.ui.components.BackgroundDecoration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    userId: String? = null,
    username: String? = null,
    profileViewModel: ProfileViewModel = getViewModel(),
    toastNotifier: ToastNotifier = koinInject(),
) {
    val currentUser by sessionManager.currentUser.collectAsState()
    val isOwnProfile =
        (userId == null && username == null) || userId == currentUser?.id || username == currentUser?.username

    val isFromBottomNav = userId == null && username == null && isOwnProfile

    val profileState by profileViewModel.profileState.collectAsState()
    val myProfile by profileViewModel.myProfile.collectAsState()

    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    val profileTargetKey = when {
        username != null -> "user:$username"
        userId != null -> "id:$userId"
        else -> "me"
    }

    LaunchedEffect(profileTargetKey, isLoggedIn) {
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

            else -> profileViewModel.loadMyProfile()
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
                                navController.navigate(Screen.ProfileEdit.route)
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
        when (currentState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
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

            is UiState.Success -> {
                ProfileContent(
                    profile = currentState.data,
                    isOwnProfile = isOwnProfile,
                    currentUser = currentUser,
                    profileViewModel = profileViewModel,
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is UiState.Idle -> {
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
    modifier: Modifier = Modifier,
    toastNotifier: ToastNotifier = koinInject(),
) {
    val profileAuthorBooksViewModel: ProfileAuthorBooksViewModel = getViewModel()
    val profileActivityViewModel: ProfileActivityViewModel = getViewModel()
    val authorBooks by profileAuthorBooksViewModel.authorBooks.collectAsState()
    val authorBooksLoading by profileAuthorBooksViewModel.authorBooksLoading.collectAsState()
    val userReviews by remember {
        mutableStateOf<List<com.example.booknest.domain.model.response.ReviewResponse>>(
            emptyList()
        )
    }
    val myRecentActivity by profileActivityViewModel.myRecentActivity.collectAsState()
    val userActivity = myRecentActivity

    val booksSectionExpanded = remember { mutableStateOf(false) }
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

    val friendViewModel: FriendViewModel = getViewModel()
    val authorFollowViewModel: AuthorFollowViewModel = getViewModel()
    val reviewViewModel: ReviewViewModel = getViewModel()
    val favoriteGenresViewModel: FavoriteGenresViewModel = getViewModel()
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
                profileAuthorBooksViewModel.loadAuthorBooks(authorId, authorName)
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

    val activityTargetKey = if (isOwnProfile) {
        "me"
    } else {
        profile.username?.let { "user:$it" } ?: "id:${profile.userId ?: profile.id}"
    }
    LaunchedEffect(activityTargetKey) {
        if (isOwnProfile) {
            profileActivityViewModel.loadMyRecentActivity(days = 7)
        } else {
            profile.username?.let { username ->
                profileActivityViewModel.loadUserRecentActivity(username, days = 7)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundDecoration(modifier = Modifier.fillMaxSize())

        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
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
                                    toastNotifier.showSuccess("User unfriended successfully")
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
            }

            if (!profile.bio.isNullOrBlank() || (profile.socialMedia != null && hasSocialMediaLinks(
                    profile.socialMedia
                ))
            ) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    BioSection(
                        bio = profile.bio,
                        socialMedia = profile.socialMedia
                    )
                }
            }

            profile.stats?.let { stats ->
                Column(Modifier.padding(horizontal = 16.dp)) {
                    EnhancedProfileStatsSection(
                        stats = stats,
                        isOwnProfile = isOwnProfile,
                        profile = profile,
                        favoriteGenres = if (isOwnProfile && profile.userType == "reader") favoriteGenres else emptyList(),
                        followerCount = if (!isOwnProfile && profile.userType == "author") followerCount else null
                    )
                }
            }

            if (profile.userType == "author") {
                ProfileRecommendedBooksSection(
                    books = authorBooks,
                    isLoading = authorBooksLoading,
                    isExpanded = booksSectionExpanded.value,
                    onExpandToggle = { booksSectionExpanded.value = !booksSectionExpanded.value },
                    navController = navController
                )
            }

            if (userActivity.isNotEmpty()) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    RecentActivitySection(
                        activities = userActivity.take(5),
                        onViewAll = { showActivity = true },
                        navController = navController
                    )
                }
            }

            if (!isOwnProfile) {
                val reviews = reviewViewModel.userReviews.collectAsState().value
                if (reviews.isNotEmpty()) {
                    Column(Modifier.padding(horizontal = 16.dp)) {
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
            }

            if (profile.birthDate != null) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ProfileDetailsSection(profile = profile)
                }
            }
        }

    }
}
