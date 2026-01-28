package com.example.booknest.ui.account.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.ui.account.components.stats.sections.UserStatsHeader
import com.example.booknest.ui.account.components.stats.sections.StatsGrid
import com.example.booknest.ui.account.components.stats.sections.AuthorAnalyticsLink
import com.example.booknest.ui.account.components.stats.sections.ReadingStatisticsSection
import com.example.booknest.ui.account.components.stats.sections.GenresBreakdownSection
import com.example.booknest.ui.account.components.stats.sections.AdditionalStatsInfo

@Composable
fun StatsContent(
    stats: UserStatsResponse,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        UserStatsHeader(user = stats.user)

        StatsGrid(stats = stats.stats)

        if (stats.stats.userType == "author") {
            AuthorAnalyticsLink(navController = navController)
        } else {
            ReadingStatisticsSection(stats = stats.stats)
            GenresBreakdownSection(stats = stats.stats)
        }

        AdditionalStatsInfo(stats = stats.stats)
    }
}

