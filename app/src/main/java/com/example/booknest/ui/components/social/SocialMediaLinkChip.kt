package com.example.booknest.ui.components.social

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SocialMediaLinkChip(
    platform: String,
    url: String,
    icon: ImageVector
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
