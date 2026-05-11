package com.example.booknest.ui.applications.components.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.ui.applications.components.list.GenreTag
import com.example.booknest.viewmodel.profile.ProfileViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun ReaderStatsRow(
    readerId: String?,
    navController: NavController,
    profileViewModel: ProfileViewModel = getViewModel()
) {
    var userProfile by remember { mutableStateOf<UserProfileResponse?>(null) }
    var isLoadingStats by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(readerId) {
        if (readerId != null) {
            isLoadingStats = true
            hasError = false
            try {
                profileViewModel.loadPublicUserProfile(readerId)
            } catch (e: Exception) {
                hasError = true
            } finally {
                isLoadingStats = false
            }
        }
    }

    val publicProfile by profileViewModel.publicProfile.collectAsState()
    val profileError by profileViewModel.error.collectAsState()

    LaunchedEffect(publicProfile, profileError) {
        if (profileError != null) {
            hasError = true
        } else {
            publicProfile?.let { profile ->
                userProfile = profile.toFullProfile()
            }
        }
    }

    val stats = userProfile?.stats
    val genresBreakdown = stats?.genresBreakdown

    LaunchedEffect(Unit) {
    }

    if (isLoadingStats || hasError || stats == null) {
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            stats.totalReviews?.let { reviews ->
                Text(
                    text = "Reviews: $reviews",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } ?: Text(
                text = "Reviews: N/A",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            stats.averageRating?.let { rating ->
                Text(
                    text = "Avg rating: ${String.format("%.1f", rating)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } ?: Text(
                text = "Avg rating: N/A",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        genresBreakdown?.let { breakdown ->
            if (breakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Favorite genres:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val topGenres = breakdown.entries.sortedByDescending { it.value }.take(5)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        topGenres.chunked(3).forEach { rowGenres ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rowGenres.forEach { entry ->
                                    GenreTag(text = entry.key)
                                }
                            }
                        }
                    }
                }
            }
        } ?: run {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Favorite genres:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "N/A",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
