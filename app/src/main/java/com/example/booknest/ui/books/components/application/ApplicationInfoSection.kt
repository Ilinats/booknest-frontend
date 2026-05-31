package com.example.booknest.ui.books.components.application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.port.SessionReader
import com.example.booknest.domain.model.response.ApplicationCheckApplicationResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.books.utils.formatDate
import com.example.booknest.ui.author.components.books.formatBookStatus
import com.example.booknest.ui.author.components.books.formatDistributionType
import com.example.booknest.ui.author.components.books.formatSelectionMethod
import com.example.booknest.ui.books.utils.isFullyBooked
import com.example.booknest.viewmodel.profile.AddressViewModel
import com.example.booknest.viewmodel.applications.ApplicationViewModel
import com.example.booknest.viewmodel.profile.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

private fun String?.isApplicationStatus(status: String): Boolean =
    this?.equals(status, ignoreCase = true) == true

@Composable
fun ApplicationInfoSection(
    book: BookResponse,
    userApplication: ApplicationCheckApplicationResponse?,
    onApplyClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    isApplicationDeadlinePassed: Boolean = false,
    showApplyButton: Boolean,
    showWithdrawButton: Boolean,
    navController: NavController? = null,
    sessionReader: SessionReader? = null,
    applicationViewModel: ApplicationViewModel = koinViewModel(),
) {
    val profileViewModel: ProfileViewModel = koinViewModel()
    val addressViewModel: AddressViewModel = koinViewModel()
    val myProfile by profileViewModel.myProfile.collectAsState()
    val addresses by addressViewModel.addresses.collectAsState()
    val currentUser = if (sessionReader != null) {
        val user by sessionReader.currentUser.collectAsState()
        user
    } else {
        null
    }

    val requiresPhysicalCopy = book.distributionType?.lowercase() in listOf("physical", "both")
    val isEmailVerified = currentUser?.emailVerified == true

    LaunchedEffect(requiresPhysicalCopy, profileViewModel) {
        if (requiresPhysicalCopy) {
            if (myProfile == null) {
                profileViewModel.loadMyProfile()
            }
            addressViewModel.loadAddresses()
        }
    }

    val hasAddresses = (myProfile?.addresses?.isNotEmpty() == true) || addresses.isNotEmpty()
    val applicationStatus = userApplication?.status

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Slots Filled: ${(book.totalCopies ?: 0) - (book.availableCopies ?: 0)}/${book.totalCopies ?: 0}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Application Deadline: ${book.applicationDeadline?.let { formatDate(it) } ?: "Not specified"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (book.reviewDeadline != null) {
                        "Review Deadline: ${book.reviewDeadline?.let { formatDate(it) }}"
                    } else {
                        "Review Deadline: Not specified"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (book.distributionType != null || book.selectionMethod != null || book.status != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )

                    book.distributionType?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Distribution",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatDistributionType(it),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    book.selectionMethod?.takeIf { it.isNotBlank() }?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Selection Method",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatSelectionMethod(it),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    book.status?.takeIf { it.isNotBlank() }?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatBookStatus(it),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        when {
            applicationStatus.isApplicationStatus("approved") -> {
                ApplicationApprovedMessageCard()
            }

            applicationStatus.isApplicationStatus("rejected") -> {
                ApplicationRejectedMessageCard()
            }

            applicationStatus.isApplicationStatus("withdrawn") -> {
                ApplicationWithdrawnMessageCard()
            }

            book.isFullyBooked() && userApplication == null -> {
                ApplicationFullyBookedMessageCard()
            }

            isApplicationDeadlinePassed &&
                userApplication == null &&
                !book.isFullyBooked() -> {
                ApplicationDeadlinePassedMessageCard()
            }

            showWithdrawButton -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ApplicationPendingMessageCard()
                    OutlinedButton(
                        onClick = onWithdrawClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Withdraw application")
                    }
                }
            }

            showApplyButton -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!isEmailVerified) {
                        if (navController != null) {
                            EmailVerificationRequiredMessageCard(
                                onVerifyClick = {
                                    navController.navigate(
                                        Screen.EmailVerification.createRoute(currentUser?.email)
                                    )
                                },
                            )
                        } else {
                            ApplicationStatusMessageCard(
                                title = "Verify your email",
                                message = "Email verification is required before you can apply for books.",
                                variant = ApplicationStatusMessageVariant.Warning,
                            )
                        }
                    }

                    if (requiresPhysicalCopy && !hasAddresses) {
                        if (navController != null) {
                            ShippingAddressRequiredMessageCard(
                                onAddAddressClick = {
                                    navController.navigate(Screen.PrivacySettings.route)
                                },
                            )
                        } else {
                            ApplicationStatusMessageCard(
                                title = "Shipping address required",
                                message = "Add a delivery address in your profile to apply for physical review copies.",
                                variant = ApplicationStatusMessageVariant.Warning,
                            )
                        }
                    }

                    val isApplicationLoading by applicationViewModel.isLoading.collectAsState()

                    Button(
                        onClick = onApplyClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isEmailVerified && (!requiresPhysicalCopy || hasAddresses) && !isApplicationLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (isApplicationLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Apply for review copy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
