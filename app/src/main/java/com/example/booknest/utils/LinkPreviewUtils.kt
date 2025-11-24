package com.example.booknest.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL

/**
 * Utility functions for link preview and detection
 */
object LinkPreviewUtils {
    
    private const val TAG = "LinkPreviewUtils"
    
    /**
     * Detects the type of link (TikTok, YouTube, or Other)
     */
    fun detectLinkType(url: String): LinkType {
        return when {
            url.contains("tiktok.com", ignoreCase = true) -> LinkType.TIKTOK
            url.contains("youtube.com", ignoreCase = true) || 
            url.contains("youtu.be", ignoreCase = true) -> LinkType.YOUTUBE
            else -> LinkType.OTHER
        }
    }
    
    /**
     * Converts YouTube URL to embeddable format
     */
    fun getYouTubeEmbedUrl(url: String): String? {
        return try {
            val videoId = extractYouTubeVideoId(url)
            videoId?.let { 
                // Add parameters for better embedding: autoplay=0, modestbranding=1, rel=0
                "https://www.youtube.com/embed/$it?autoplay=0&modestbranding=1&rel=0&playsinline=1"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting YouTube URL", e)
            null
        }
    }
    
    /**
     * Converts TikTok URL to embeddable format
     * TikTok embeds use the format: https://www.tiktok.com/embed/v2/{videoId}
     * Video ID is extracted from URLs like: https://www.tiktok.com/@username/video/1234567890
     */
    fun getTikTokEmbedUrl(url: String): String? {
        return try {
            val urlObj = URL(url)
            val pathParts = urlObj.path.split("/").filter { it.isNotBlank() }
            
            // Find "video" in the path
            val videoIndex = pathParts.indexOf("video")
            if (videoIndex != -1 && videoIndex < pathParts.size - 1) {
                val videoId = pathParts[videoIndex + 1].split("?")[0]
                if (videoId.isNotBlank()) {
                    return "https://www.tiktok.com/embed/v2/$videoId"
                }
            }
            
            // Alternative: try to extract from full URL string
            val videoRegex = Regex("""/video/(\d+)""")
            val match = videoRegex.find(url)
            match?.groupValues?.get(1)?.let { videoId ->
                return "https://www.tiktok.com/embed/v2/$videoId"
            }
            
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error converting TikTok URL", e)
            null
        }
    }
    
    /**
     * Extracts video ID from YouTube URL
     */
    fun extractYouTubeVideoId(url: String): String? {
        return try {
            val urlObj = URL(url)
            when {
                urlObj.host.contains("youtu.be") -> {
                    urlObj.path.substringAfter("/").split("?")[0]
                }
                urlObj.host.contains("youtube.com") -> {
                    urlObj.query?.split("&")?.find { it.startsWith("v=") }?.substringAfter("v=")?.split("&")?.get(0)
                        ?: urlObj.path.substringAfter("/watch/").split("?")[0]
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting YouTube video ID", e)
            null
        }
    }
    
    /**
     * Gets YouTube video thumbnail URL
     */
    fun getYouTubeThumbnailUrl(videoId: String): String {
        // Use maxresdefault for best quality, fallback to hqdefault
        return "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
    }
    
    /**
     * Fetches YouTube video metadata using oEmbed API
     */
    suspend fun fetchYouTubeMetadata(url: String): YouTubeMetadata? = withContext(Dispatchers.IO) {
        try {
            val videoId = extractYouTubeVideoId(url) ?: return@withContext null
            
            // Try oEmbed API first
            try {
                val oEmbedUrl = "https://www.youtube.com/oembed?url=${java.net.URLEncoder.encode(url, "UTF-8")}&format=json"
                val connection = java.net.URL(oEmbedUrl).openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(response)
                
                return@withContext YouTubeMetadata(
                    videoId = videoId,
                    title = json.optString("title", ""),
                    authorName = json.optString("author_name", ""),
                    thumbnailUrl = getYouTubeThumbnailUrl(videoId)
                )
            } catch (e: Exception) {
                Log.d(TAG, "oEmbed failed, trying HTML parsing", e)
            }
            
            // Fallback: parse HTML
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            val title = doc.select("meta[property=og:title]").attr("content")
                .takeIf { it.isNotBlank() }
                ?: doc.title()
                .takeIf { it.isNotBlank() }
                ?: "YouTube Video"
            
            val authorName = doc.select("link[itemprop=name]").attr("content")
                .takeIf { it.isNotBlank() }
                ?: doc.select("a.yt-simple-endpoint.style-scope.yt-formatted-string").firstOrNull()?.text()
                ?: "YouTube"
            
            YouTubeMetadata(
                videoId = videoId,
                title = title,
                authorName = authorName,
                thumbnailUrl = getYouTubeThumbnailUrl(videoId)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching YouTube metadata", e)
            null
        }
    }
    
    /**
     * Fetches meta tags from a URL
     */
    suspend fun fetchMetaTags(url: String): LinkMetaTags? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            val title = doc.select("meta[property=og:title]").attr("content")
                .takeIf { it.isNotBlank() }
                ?: doc.select("meta[name=title]").attr("content")
                .takeIf { it.isNotBlank() }
                ?: doc.title()
                .takeIf { it.isNotBlank() }
            
            val description = doc.select("meta[property=og:description]").attr("content")
                .takeIf { it.isNotBlank() }
                ?: doc.select("meta[name=description]").attr("content")
                .takeIf { it.isNotBlank() }
            
            val imageUrl = doc.select("meta[property=og:image]").attr("content")
                .takeIf { it.isNotBlank() }
                ?: doc.select("meta[name=image]").attr("content")
                .takeIf { it.isNotBlank() }
                ?: doc.select("link[rel=image_src]").attr("href")
                .takeIf { it.isNotBlank() }
            
            val siteName = doc.select("meta[property=og:site_name]").attr("content")
                .takeIf { it.isNotBlank() }
            
            if (title != null || description != null || imageUrl != null) {
                LinkMetaTags(
                    title = title ?: "Link Preview",
                    description = description,
                    imageUrl = imageUrl,
                    siteName = siteName,
                    url = url
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching meta tags for $url", e)
            null
        }
    }
}

enum class LinkType {
    TIKTOK,
    YOUTUBE,
    OTHER
}

data class LinkMetaTags(
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val siteName: String?,
    val url: String
)

data class YouTubeMetadata(
    val videoId: String,
    val title: String,
    val authorName: String,
    val thumbnailUrl: String
)


