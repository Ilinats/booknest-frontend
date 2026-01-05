package com.example.booknest.ui.profile.components.header

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.ui.profile.utils.formatDate

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
    currentUserIsAuthor: Boolean = false,
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                    } else if (!currentUserIsAuthor) {
                        // Only show friend buttons if current user is not an author
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

