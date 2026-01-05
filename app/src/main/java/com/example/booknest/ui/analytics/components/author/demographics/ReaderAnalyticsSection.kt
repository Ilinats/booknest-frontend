package com.example.booknest.ui.author.components.analytics.demographics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.ReaderAnalyticsResponse

@Composable
fun ReaderAnalyticsSection(readerAnalytics: ReaderAnalyticsResponse) {
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reader Analytics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReaderMetric(
                    label = "Total Readers",
                    value = readerAnalytics.totalUniqueReaders.toString()
                )
                ReaderMetric(
                    label = "New This Month",
                    value = readerAnalytics.newReadersThisMonth.toString()
                )
                ReaderMetric(
                    label = "Repeat Readers",
                    value = readerAnalytics.repeatReaders.toString()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReaderMetric(
                    label = "Engagement Rate",
                    value = "${readerAnalytics.engagementRate}%"
                )
                ReaderMetric(
                    label = "With Reviews",
                    value = readerAnalytics.readersWithReviews.toString()
                )
            }

            readerAnalytics.demographics?.let { demographics ->
                Divider()

                Text(
                    text = "Reader Demographics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                demographics.age?.let { ageDemographics ->
                    AgeDemographicsSection(ageDemographics = ageDemographics)
                }

                demographics.countries?.let { countryDemographics ->
                    CountryDemographicsSection(countryDemographics = countryDemographics)
                }

                demographics.genrePreferences?.let { genrePreferences ->
                    GenrePreferencesSection(
                        title = "Reader Genre Preferences",
                        genreDemographics = genrePreferences
                    )
                }

                demographics.appliedBookGenres?.let { appliedGenres ->
                    GenrePreferencesSection(
                        title = "Genres of Applied Books",
                        genreDemographics = appliedGenres
                    )
                }
            }
        }
    }
}

@Composable
fun ReaderMetric(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
