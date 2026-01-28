package com.example.booknest.ui.author.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.booknest.domain.model.response.SocialMediaResponse
import com.example.booknest.ui.components.social.SocialMediaLinkChip

@Composable
fun ProfileHeaderSection(
    authorName: String,
    authorBio: String,
    avatarUrl: String?,
    initials: String,
    username: String?,
    isVerified: Boolean?,
    onEditClick: () -> Unit
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
            Box {
                if (avatarUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5EDE8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
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
                    onClick = onEditClick,
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

                username?.let { username ->
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

                    if (isVerified == true) {
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialMediaSection(socialMedia: SocialMediaResponse?) {
    socialMedia?.let { social ->
        if (hasSocialMediaLinks(social)) {
            Column {
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
                    social.instagram?.takeIf { it.isNotBlank() }?.let { url ->
                        SocialMediaLinkChip(
                            platform = "Instagram",
                            url = url,
                            icon = Icons.Default.Share
                        )
                    }
                    social.tiktok?.takeIf { it.isNotBlank() }?.let { url ->
                        SocialMediaLinkChip(
                            platform = "TikTok",
                            url = url,
                            icon = Icons.Default.VideoLibrary
                        )
                    }
                    social.youtube?.takeIf { it.isNotBlank() }?.let { url ->
                        SocialMediaLinkChip(
                            platform = "YouTube",
                            url = url,
                            icon = Icons.Default.PlayArrow
                        )
                    }
                    social.goodreads?.takeIf { it.isNotBlank() }?.let { url ->
                        SocialMediaLinkChip(
                            platform = "Goodreads",
                            url = url,
                            icon = Icons.Default.Book
                        )
                    }
                    social.custom?.forEach { customLink ->
                        SocialMediaLinkChip(
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



private fun hasSocialMediaLinks(social: SocialMediaResponse): Boolean {
    return !social.instagram.isNullOrBlank() ||
            !social.tiktok.isNullOrBlank() ||
            !social.youtube.isNullOrBlank() ||
            !social.goodreads.isNullOrBlank() ||
            !social.custom.isNullOrEmpty()
}

