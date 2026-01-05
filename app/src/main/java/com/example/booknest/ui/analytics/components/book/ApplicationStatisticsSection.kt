package com.example.booknest.ui.analytics.components.book

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.ApplicationAnalyticsResponse

@Composable
fun ApplicationStatisticsSection(applicationStatistics: ApplicationAnalyticsResponse) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Application Statistics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ApplicationStatCard(
                        title = "Total Applications",
                        value = applicationStatistics.totalApplications.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ApplicationStatCard(
                        title = "Approved",
                        value = applicationStatistics.approvedApplications.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ApplicationStatCard(
                        title = "Pending",
                        value = applicationStatistics.pendingApplications.toString(),
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    ApplicationStatCard(
                        title = "Rejected",
                        value = applicationStatistics.rejectedApplications.toString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (applicationStatistics.applicationsThisMonth != null ||
                    applicationStatistics.approvedApplicationsThisMonth != null
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "This Month",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        applicationStatistics.applicationsThisMonth?.let {
                            StatCard(
                                title = "Applications",
                                value = it.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        applicationStatistics.approvedApplicationsThisMonth?.let {
                            StatCard(
                                title = "Approved",
                                value = it.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        applicationStatistics.rejectedApplicationsThisMonth?.let {
                            StatCard(
                                title = "Rejected",
                                value = it.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Approval Rate",
                        value = "${applicationStatistics.approvalRate}%",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Rejection Rate",
                        value = "${applicationStatistics.rejectionRate}%",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    applicationStatistics.averageResponseTime?.let { time ->
                        StatCard(
                            title = "Avg Response Time",
                            value = if (time < 24) "$time hours" else "${time / 24} days",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    applicationStatistics.applicationConversionRate?.let { rate ->
                        StatCard(
                            title = "Conversion Rate",
                            value = "$rate%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
