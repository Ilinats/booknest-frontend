package com.example.booknest.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.booknest.utils.LinkPreviewUtils
import com.example.booknest.utils.LinkMetaTags
import com.example.booknest.utils.LinkType
import kotlinx.coroutines.launch

/**
 * Displays a review link with appropriate preview based on link type
 */
@Composable
fun ReviewLinkPreview(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val linkType = remember(url) { LinkPreviewUtils.detectLinkType(url) }
    
    when (linkType) {
        LinkType.TIKTOK, LinkType.YOUTUBE -> {
            VideoEmbedView(
                url = url,
                linkType = linkType,
                modifier = modifier
            )
        }
        LinkType.OTHER -> {
            LinkPreviewCard(
                url = url,
                modifier = modifier,
                onLinkClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            )
        }
    }
}

/**
 * WebView component for embedding TikTok and YouTube videos
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoEmbedView(
    url: String,
    linkType: LinkType,
    modifier: Modifier = Modifier
) {
    val embedUrl = remember(url, linkType) {
        when (linkType) {
            LinkType.TIKTOK -> LinkPreviewUtils.getTikTokEmbedUrl(url)
            LinkType.YOUTUBE -> LinkPreviewUtils.getYouTubeEmbedUrl(url)
            else -> null
        }
    }
    
    if (embedUrl != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            settings.allowUniversalAccessFromFileURLs = true
                            settings.allowFileAccessFromFileURLs = true
                            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            settings.mediaPlaybackRequiresUserGesture = false
                            
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                    // Allow navigation within embed, but open external links in browser
                                    if (url != null && !url.contains("embed")) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        ctx.startActivity(intent)
                                        return true
                                    }
                                    return false
                                }
                            }
                            
                            loadUrl(embedUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (linkType == LinkType.TIKTOK) 600.dp else 400.dp)
                )
                
                // Clickable overlay to open in browser
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            val context = LocalContext.current
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in browser",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open in app")
                    }
                }
            }
        }
    } else {
        // Fallback: show clickable card
        LinkPreviewCard(
            url = url,
            modifier = modifier,
            onLinkClick = {
                val context = LocalContext.current
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Handle error
                }
            }
        )
    }
}

/**
 * Preview card for regular links using meta tags
 */
@Composable
fun LinkPreviewCard(
    url: String,
    modifier: Modifier = Modifier,
    onLinkClick: () -> Unit
) {
    val context = LocalContext.current
    var metaTags by remember { mutableStateOf<LinkMetaTags?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(url) {
        isLoading = true
        scope.launch {
            metaTags = LinkPreviewUtils.fetchMetaTags(url)
            isLoading = false
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onLinkClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image
                metaTags?.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = metaTags?.title,
                        modifier = Modifier
                            .size(100.dp, 100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Site name or URL domain
                        Text(
                            text = metaTags?.siteName 
                                ?: url.split("/").getOrNull(2)?.split(".")?.getOrNull(0)?.replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                                }
                                ?: "Link",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Title
                        Text(
                            text = metaTags?.title ?: url,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Description
                        metaTags?.description?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Open link indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open link",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tap to open",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

