package com.example.booknest.ui.author

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.ui.theme.DarkNavyBlue
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.author.components.profile.AuthorProfileHeader
import com.example.booknest.ui.author.components.profile.AuthorStatisticsSection
import com.example.booknest.ui.author.components.AuthorBooksSection
import com.example.booknest.viewmodel.AuthorViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorProfileScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    profileViewModel: ProfileViewModel = getViewModel(),
    authorViewModel: AuthorViewModel = getViewModel()
) {
    val currentUser by sessionManager.currentUser.collectAsState()
    val myProfile by profileViewModel.myProfile.collectAsState()
    val myBooks by authorViewModel.myBooks.collectAsState()
    val isLoadingProfile by profileViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadMyProfile()
        authorViewModel.loadMyBooks()
    }

    val authorName = myProfile?.firstName?.let { firstName ->
        myProfile?.lastName?.let { lastName ->
            "$firstName $lastName"
        } ?: firstName
    } ?: myProfile?.username ?: currentUser?.username ?: "Author"
    val authorBio = myProfile?.bio ?: "No bio available"
    val joinYear = myProfile?.createdAt?.take(4) ?: "N/A"

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = DarkNavyBlue
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("profile_edit")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isLoadingProfile && myProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AuthorProfileHeader(
                        authorName = authorName,
                        authorBio = authorBio,
                        joinYear = joinYear,
                        myProfile = myProfile,
                        currentUser = currentUser,
                        navController = navController,
                        sessionManager = sessionManager
                    )

                    AuthorStatisticsSection(stats = myProfile?.stats)

                    AuthorBooksSection(
                        myBooks = myBooks,
                        navController = navController
                    )
                }
            }
        }
    }
}