package com.example.booknest.ui.profile.components.bio

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.SocialMediaResponse

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

fun hasSocialMediaLinks(social: SocialMediaResponse): Boolean {
    return !social.instagram.isNullOrBlank() ||
            !social.tiktok.isNullOrBlank() ||
            !social.youtube.isNullOrBlank() ||
            !social.goodreads.isNullOrBlank() ||
            !social.custom.isNullOrEmpty()
}

