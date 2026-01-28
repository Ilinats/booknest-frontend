package com.example.booknest.ui.components.reviews

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
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
import com.example.booknest.utils.YouTubeMetadata
import kotlinx.coroutines.launch

@Composable
fun ReviewLinkPreview(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val linkType = remember(url) { LinkPreviewUtils.detectLinkType(url) }

    when (linkType) {
        LinkType.YOUTUBE -> {
            YouTubePreviewCard(
                url = url,
                modifier = modifier
            )
        }

        LinkType.TIKTOK -> {
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
                    }
                }
            )
        }
    }
}

@Composable
fun YouTubePreviewCard(
    url: String,
    modifier: Modifier = Modifier
) {
    var metadata by remember { mutableStateOf<YouTubeMetadata?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(url) {
        isLoading = true
        scope.launch {
            metadata = LinkPreviewUtils.fetchYouTubeMetadata(url)
            isLoading = false
        }
    }

    val videoId = remember(url) { LinkPreviewUtils.extractYouTubeVideoId(url) }
    val embedUrl = remember(videoId) {
        videoId?.let {
            "https://www.youtube.com/embed/$it?enablejsapi=1&autoplay=0&modestbranding=1&rel=0&playsinline=1"
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isExpanded && embedUrl != null) {
            Column {
                YouTubeEmbedWebView(
                    embedUrl = embedUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { isExpanded = false }
                    ) {
                        Text("Show Preview")
                    }

                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in YouTube",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open in YouTube")
                    }
                }
            }
        } else {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clickable { isExpanded = true }
                ) {
                    metadata?.thumbnailUrl?.let { thumbnailUrl ->
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = metadata?.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(64.dp),
                            tint = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        metadata?.title?.let { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        metadata?.authorName?.let { author ->
                            Text(
                                text = author,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeEmbedWebView(
    embedUrl: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.allowFileAccessFromFileURLs = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                setBackgroundColor(0x00000000)

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (url != null && !url.contains("youtube.com/embed")) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            ctx.startActivity(intent)
                            return true
                        }
                        return false
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            """
                            (function() {
                                var style = document.createElement('style');
                                style.innerHTML = 'body { margin: 0; padding: 0; overflow: hidden; } iframe { width: 100% !important; height: 100% !important; border: none; }';
                                document.head.appendChild(style);
                            })();
                        """.trimIndent(), null
                        )
                    }
                }

                loadUrl(embedUrl)
            }
        },
        modifier = modifier
    )
}

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
                            settings.mixedContentMode =
                                WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true

                            setBackgroundColor(0x00000000)

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    url: String?
                                ): Boolean {
                                    if (url != null && !url.contains("embed") && !url.contains("youtube.com") && !url.contains(
                                            "tiktok.com"
                                        )
                                    ) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        ctx.startActivity(intent)
                                        return true
                                    }
                                    return false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    if (linkType == LinkType.YOUTUBE) {
                                        view?.evaluateJavascript(
                                            """
                                            (function() {
                                                var style = document.createElement('style');
                                                style.innerHTML = 'body { margin: 0; padding: 0; } iframe { width: 100%; height: 100%; }';
                                                document.head.appendChild(style);
                                            })();
                                        """.trimIndent(), null
                                        )
                                    }
                                }
                            }

                            loadUrl(embedUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (linkType == LinkType.YOUTUBE) {
                                Modifier.aspectRatio(16f / 9f)
                            } else if (linkType == LinkType.TIKTOK) {
                                Modifier.height(600.dp)
                            } else {
                                Modifier.height(400.dp)
                            }
                        )
                )

                val contextForButton = LocalContext.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                contextForButton.startActivity(intent)
                            } catch (e: Exception) {
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
        val contextForFallback = LocalContext.current
        LinkPreviewCard(
            url = url,
            modifier = modifier,
            onLinkClick = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    contextForFallback.startActivity(intent)
                } catch (e: Exception) {
                }
            }
        )
    }
}

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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = metaTags?.siteName
                                ?: url.split("/").getOrNull(2)?.split(".")?.getOrNull(0)
                                    ?.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase() else it.toString()
                                    }
                                ?: "Link",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = metaTags?.title ?: url,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

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

