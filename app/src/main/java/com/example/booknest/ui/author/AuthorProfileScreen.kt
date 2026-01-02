package com.example.booknest.ui.author

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.*
import coil.compose.AsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.ui.theme.DarkNavyBlue
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import android.content.Intent
import android.net.Uri
import com.example.booknest.domain.model.response.SocialMediaResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.navigation.AuthorBottomBarScreen
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.ui.author.BookStatus
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
    val campaigns = myBooks.size
    val reviews = myProfile?.stats?.totalReviews ?: 0
    val followers = 0
    val successRate = if (campaigns > 0) {
        ((myBooks.count { it.status == BookStatus.ACTIVE.value || it.status == BookStatus.COMPLETED.value } * 100) / campaigns).coerceIn(
            0,
            100
        )
    } else 0

    val portfolioBooks = myBooks.take(5)

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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8DFE4)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val avatarUrl =
                                myProfile?.avatarUrl ?: currentUser?.profilePictureUrl
                                ?: currentUser?.avatarUrl
                            Box {
                                if (avatarUrl.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF5EDE8)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initial =
                                            (myProfile?.firstName?.firstOrNull()
                                                ?: myProfile?.username?.firstOrNull()
                                                ?: currentUser?.username?.firstOrNull() ?: '?')
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
                                        model = avatarUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
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

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = authorName,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                myProfile?.username?.let { username ->
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
                                            text = "Author",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }

                                    if (myProfile?.isVerified == true) {
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
                            }

                            if (authorBio.isNotBlank() && authorBio != "No bio available") {
                                Text(
                                    text = authorBio,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }

                            myProfile?.socialMedia?.let { socialMedia ->
                                if (hasSocialMediaLinks(socialMedia)) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))
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
                                        socialMedia.instagram?.takeIf { it.isNotBlank() }
                                            ?.let { url ->
                                                SocialMediaLinkChip(
                                                    platform = "Instagram",
                                                    url = url,
                                                    icon = Icons.Default.Share
                                                )
                                            }
                                        socialMedia.tiktok?.takeIf { it.isNotBlank() }?.let { url ->
                                            SocialMediaLinkChip(
                                                platform = "TikTok",
                                                url = url,
                                                icon = Icons.Default.VideoLibrary
                                            )
                                        }
                                        socialMedia.youtube?.takeIf { it.isNotBlank() }
                                            ?.let { url ->
                                                SocialMediaLinkChip(
                                                    platform = "YouTube",
                                                    url = url,
                                                    icon = Icons.Default.PlayArrow
                                                )
                                            }
                                        socialMedia.goodreads?.takeIf { it.isNotBlank() }
                                            ?.let { url ->
                                                SocialMediaLinkChip(
                                                    platform = "Goodreads",
                                                    url = url,
                                                    icon = Icons.Default.Book
                                                )
                                            }
                                        socialMedia.custom?.forEach { customLink ->
                                            SocialMediaLinkChip(
                                                platform = customLink.platform,
                                                url = customLink.url,
                                                icon = Icons.Default.Link
                                            )
                                        }
                                    }
                                }
                            }

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

                            myProfile?.createdAt?.let { createdAt ->
                                Text(
                                    text = "Member since ${formatDateMyBooks(createdAt)}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } ?: run {
                                Text(
                                    text = "Joined $joinYear",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    myProfile?.stats?.let { stats ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8DFE4)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val statItems = listOf(
                                        "Total Books" to (stats.totalBooks ?: 0),
                                        "Published Books" to (stats.publishedBooks ?: 0),
                                        "Draft Books" to (stats.draftBooks ?: 0),
                                        "Total Applications" to stats.totalApplications,
                                        "Approval Rate" to (if (stats.totalApplications > 0) {
                                            ((stats.approvedApplications.toDouble() / stats.totalApplications) * 100).toInt()
                                        } else 0),
                                        "Average Rating" to (stats.averageRating ?: 0.0),
                                        "Total Reviews" to (stats.totalReviews ?: 0)
                                    )

                                    statItems.chunked(2).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            rowItems.forEach { (title, value) ->
                                                AuthorStatCard(
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
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (myBooks.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8DFE4)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                        text = "My Books",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    TextButton(onClick = {
                                        navController.navigate(AuthorBottomBarScreen.MyBooks.route)
                                    }) {
                                        Text("View All")
                                    }
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(portfolioBooks) { book ->
                                        BookCoverCard(
                                            book = book,
                                            onClick = {
                                                navController.navigate(
                                                    Screen.BookDetails.createRoute(
                                                        book.id
                                                    )
                                                )
                                            }
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
}

@Composable
fun AuthorStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5EDE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = title,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun hasSocialMediaLinks(social: SocialMediaResponse): Boolean {
    return !social.instagram.isNullOrBlank() ||
            !social.tiktok.isNullOrBlank() ||
            !social.youtube.isNullOrBlank() ||
            !social.goodreads.isNullOrBlank() ||
            !social.custom.isNullOrEmpty()
}

@Composable
fun SocialMediaLinkChip(
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

private fun formatDateMyBooks(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val inputFormat = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            java.util.Locale.getDefault()
        )
        val outputFormat = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun BookCoverCard(
    book: BookResponse,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp, 120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8DFE4)),
            contentAlignment = Alignment.Center
        ) {
            if (!book.coverImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = "Book Cover",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Book,
                        contentDescription = "Book Cover",
                        modifier = Modifier.size(32.dp),
                        tint = DarkNavyBlue.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Text(
            text = book.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2
        )
    }
}