package com.example.booknest.ui.books.components.application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.ApplicationCheckApplicationResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.books.utils.formatDate
import com.example.booknest.viewmodel.profile.AddressViewModel
import com.example.booknest.viewmodel.applications.ApplicationViewModel
import com.example.booknest.viewmodel.profile.ProfileViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun ApplicationInfoSection(
    book: BookResponse,
    userApplication: ApplicationCheckApplicationResponse?,
    onApplyClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    showApplyButton: Boolean,
    showWithdrawButton: Boolean,
    navController: NavController? = null,
    sessionManager: SessionManager? = null,
    applicationViewModel: ApplicationViewModel = getViewModel()
) {
    val profileViewModel: ProfileViewModel = getViewModel()
    val addressViewModel: AddressViewModel = getViewModel()
    val myProfile by profileViewModel.myProfile.collectAsState()
    val addresses by addressViewModel.addresses.collectAsState()
    val currentUser = if (sessionManager != null) {
        val user by sessionManager.currentUser.collectAsState()
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
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
                    Divider(
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
                                text = it.replaceFirstChar { char -> char.uppercase() },
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
                                text = it.replaceFirstChar { char -> char.uppercase() },
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
                                text = it.replaceFirstChar { char -> char.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        LaunchedEffect(userApplication, showWithdrawButton, showApplyButton) {
            println("DEBUG ApplicationInfoSection: userApplication?.status = ${userApplication?.status}")
            println("DEBUG ApplicationInfoSection: showWithdrawButton = $showWithdrawButton")
            println("DEBUG ApplicationInfoSection: showApplyButton = $showApplyButton")
        }

        when {
            userApplication?.status == "approved" -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✅ Application Approved! Check your books for the copy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            userApplication?.status == "rejected" -> {
                Text(
                    text = "❌ Application Rejected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            userApplication?.status == "withdrawn" -> {
                Text(
                    text = "Application Withdrawn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            showWithdrawButton -> {
                OutlinedButton(
                    onClick = onWithdrawClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Withdraw Application")
                }
            }

            showApplyButton -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isEmailVerified) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Warning",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Email verification required to apply for books. Please verify your email address first.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (navController != null) {
                                    Button(
                                        onClick = {
                                            navController.navigate(
                                                Screen.EmailVerification.createRoute(
                                                    currentUser?.email
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Verify Email")
                                    }
                                }
                            }
                        }
                    }

                    if (requiresPhysicalCopy && !hasAddresses) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Warning",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "A shipping address is required to apply for physical copies.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (navController != null) {
                                    Button(
                                        onClick = {
                                            navController.navigate(Screen.PrivacySettings.route)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Add Address")
                                    }
                                }
                            }
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
                                text = if (userApplication?.status == "approved") "Read Now" else "Apply",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

