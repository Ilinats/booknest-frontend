package com.example.booknest.ui.author.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.SocialMediaResponse
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.components.social.SocialMediaLinkChip
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthorProfileHeader(
    authorName: String,
    authorBio: String,
    joinYear: String,
    myProfile: Any?,
    currentUser: Any?,
    navController: NavController,
    sessionManager: SessionManager = koinInject()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val avatarUrl = when {
                myProfile != null -> {
                    when {
                        myProfile.hasProperty("avatarUrl") -> myProfile.getProperty<String>("avatarUrl")
                        myProfile.hasProperty("profilePictureUrl") -> myProfile.getProperty<String>("profilePictureUrl")
                        else -> null
                    }
                }
                currentUser != null -> {
                    when {
                        currentUser.hasProperty("profilePictureUrl") -> currentUser.getProperty<String>("profilePictureUrl")
                        currentUser.hasProperty("avatarUrl") -> currentUser.getProperty<String>("avatarUrl")
                        else -> null
                    }
                }
                else -> null
            }

            Box {
                if (avatarUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5EDE8)),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = when {
                            myProfile != null -> {
                                when {
                                    myProfile.hasProperty("firstName") -> {
                                        val firstName = myProfile.getProperty<String>("firstName")
                                        firstName?.firstOrNull()?.uppercaseChar() ?: '?'
                                    }
                                    myProfile.hasProperty("username") -> {
                                        val username = myProfile.getProperty<String>("username")
                                        username?.firstOrNull()?.uppercaseChar() ?: '?'
                                    }
                                    else -> '?'
                                }
                            }
                            currentUser != null -> {
                                when {
                                    currentUser.hasProperty("username") -> {
                                        val username = currentUser.getProperty<String>("username")
                                        username?.firstOrNull()?.uppercaseChar() ?: '?'
                                    }
                                    else -> '?'
                                }
                            }
                            else -> '?'
                        }
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
                    onClick = { navController.navigate(Screen.ProfileEdit.route) },
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

                if (myProfile != null && myProfile.hasProperty("username")) {
                    val username = myProfile.getProperty<String>("username")
                    if (!username.isNullOrBlank()) {
                        Text(
                            text = "@$username",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

                    if (myProfile != null && myProfile.hasProperty("isVerified") && 
                        myProfile.getProperty<Boolean>("isVerified") == true) {
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

            if (myProfile != null && myProfile.hasProperty("socialMedia")) {
                val socialMedia = myProfile.getProperty<SocialMediaResponse>("socialMedia")
                if (socialMedia != null && hasSocialMediaLinks(socialMedia)) {
                    Divider()
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Social Media",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                onClick = { navController.navigate(Screen.ProfileEdit.route) },
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

            if (myProfile != null && myProfile.hasProperty("createdAt")) {
                val createdAt = myProfile.getProperty<String>("createdAt")
                if (!createdAt.isNullOrBlank()) {
                    Text(
                        text = "Member since ${formatDateMyBooks(createdAt)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Joined $joinYear",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

private fun <T> Any?.getProperty(propertyName: String): T? {
    return try {
        val clazz = this?.javaClass
        val field = clazz?.getDeclaredField(propertyName)
        field?.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        field?.get(this) as? T
    } catch (e: Exception) {
        null
    }
}

private fun Any?.hasProperty(propertyName: String): Boolean {
    return try {
        val clazz = this?.javaClass
        clazz?.declaredFields?.any { it.name == propertyName } == true
    } catch (e: Exception) {
        false
    }
}
