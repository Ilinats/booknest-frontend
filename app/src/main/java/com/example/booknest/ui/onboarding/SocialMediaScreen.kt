package com.example.booknest.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.CustomSocialLinkResponse
import com.example.booknest.domain.model.response.SocialMediaResponse
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.presentation.navigation.navigateToMainAsRoot
import com.example.booknest.ui.components.social.CustomLinkCard
import com.example.booknest.ui.onboarding.components.fields.SocialMediaInputField
import com.example.booknest.ui.auth.components.utils.isValidUrl
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.profile.ProfileSettingsViewModel
import com.example.booknest.viewmodel.profile.ProfileViewModel
import com.example.booknest.ui.components.BackgroundDecoration
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    profileViewModel: ProfileViewModel = getViewModel(),
    profileSettingsViewModel: ProfileSettingsViewModel = getViewModel()
) {
    val myProfile by profileViewModel.myProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()

    var instagram by remember { mutableStateOf("") }
    var tiktok by remember { mutableStateOf("") }
    var youtube by remember { mutableStateOf("") }
    var goodreads by remember { mutableStateOf("") }
    var customLinks by remember { mutableStateOf<List<CustomSocialLinkResponse>>(emptyList()) }

    var showAddCustom by remember { mutableStateOf(false) }
    var newCustomPlatform by remember { mutableStateOf("") }
    var newCustomUrl by remember { mutableStateOf("") }

    val isCustomUrlValid = remember(newCustomUrl) {
        newCustomUrl.isBlank() || isValidUrl(newCustomUrl)
    }
    val isCustomFormValid = remember(newCustomPlatform, newCustomUrl) {
        newCustomPlatform.isNotBlank() && newCustomUrl.isNotBlank() && isValidUrl(newCustomUrl)
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadMyProfile()
    }

    LaunchedEffect(myProfile) {
        myProfile?.socialMedia?.let { socialMedia ->
            instagram = socialMedia.instagram ?: ""
            tiktok = socialMedia.tiktok ?: ""
            youtube = socialMedia.youtube ?: ""
            goodreads = socialMedia.goodreads ?: ""
            customLinks = socialMedia.custom ?: emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundDecoration(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(
                    onClick = { navController.popBackStack() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Connect Your\nSocial Media",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Share your social media profiles with other BookNest users (optional)",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SocialMediaInputField(
                    label = "Instagram",
                    value = instagram,
                    onValueChange = { instagram = it },
                    placeholder = "https://instagram.com/username"
                )

                SocialMediaInputField(
                    label = "TikTok",
                    value = tiktok,
                    onValueChange = { tiktok = it },
                    placeholder = "https://tiktok.com/@username"
                )

                SocialMediaInputField(
                    label = "YouTube",
                    value = youtube,
                    onValueChange = { youtube = it },
                    placeholder = "https://youtube.com/@username"
                )

                SocialMediaInputField(
                    label = "Goodreads",
                    value = goodreads,
                    onValueChange = { goodreads = it },
                    placeholder = "https://goodreads.com/user/show/username"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (customLinks.isNotEmpty() || showAddCustom) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Links",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (!showAddCustom) {
                            Button(
                                onClick = { showAddCustom = true },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontSize = 14.sp)
                            }
                        }
                    }

                    customLinks.forEach { customLink ->
                        CustomLinkCard(
                            customLink = customLink,
                            onDelete = { linkToDelete ->
                                customLinks = customLinks.filter { it != linkToDelete }
                            }
                        )
                    }

                    if (showAddCustom) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Add Custom Link",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    IconButton(
                                        onClick = {
                                            showAddCustom = false
                                            newCustomPlatform = ""
                                            newCustomUrl = ""
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color(0xFF757575)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = newCustomPlatform,
                                    onValueChange = { newCustomPlatform = it },
                                    placeholder = {
                                        Text(
                                            "Platform Name",
                                            color = Color(0xFF757575)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(28.dp)
                                        ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(28.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    )
                                )

                                OutlinedTextField(
                                    value = newCustomUrl,
                                    onValueChange = { newCustomUrl = it },
                                    placeholder = { Text("URL", color = Color(0xFF757575)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(28.dp)
                                        ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(28.dp),
                                    isError = !isCustomUrlValid,
                                    supportingText = if (!isCustomUrlValid && newCustomUrl.isNotBlank()) {
                                        {
                                            Text(
                                                "Please enter a valid URL starting with http:// or https://",
                                                fontSize = 12.sp
                                            )
                                        }
                                    } else null,
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = MaterialTheme.colorScheme.error,
                                        errorContainerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (isCustomFormValid) {
                                            customLinks = customLinks + CustomSocialLinkResponse(
                                                platform = newCustomPlatform,
                                                url = newCustomUrl
                                            )
                                            newCustomPlatform = ""
                                            newCustomUrl = ""
                                            showAddCustom = false
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = isCustomFormValid,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        disabledContainerColor = Color(0xFFE0E0E0)
                                    )
                                ) {
                                    Text(
                                        "Add Link",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isCustomFormValid) Color.White else Color(
                                            0xFF757575
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Button(
                    onClick = { showAddCustom = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add Custom Link",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    profileSettingsViewModel.updateSocialMedia(
                        SocialMediaResponse(
                            instagram = instagram.takeIf { it.isNotBlank() },
                            tiktok = tiktok.takeIf { it.isNotBlank() },
                            youtube = youtube.takeIf { it.isNotBlank() },
                            goodreads = goodreads.takeIf { it.isNotBlank() },
                            custom = customLinks.takeIf { it.isNotEmpty() }
                        )
                    )
                    navController.navigateToMainAsRoot()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Continue",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.navigateToMainAsRoot() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Skip for Now",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
