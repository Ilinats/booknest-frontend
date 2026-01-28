package com.example.booknest.ui.account.components.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.model.response.UserStatsDataResponse

@Composable
fun EnhancedProfileStatsSection(
    stats: UserStatsDataResponse,
    isOwnProfile: Boolean,
    profile: UserProfileResponse,
    favoriteGenres: List<com.example.booknest.domain.model.response.GenreResponse> = emptyList(),
    followerCount: Int? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
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

            if (stats.userType == "author") {
                if (isOwnProfile) {
                    EnhancedAuthorStatsGrid(stats = stats)
                } else {
                    EnhancedPublicAuthorStatsGrid(stats = stats, followerCount = followerCount)
                }
            } else {
                EnhancedReaderStatsGrid(
                    stats = stats,
                    profile = profile,
                    favoriteGenres = favoriteGenres
                )
            }
        }
    }
}

