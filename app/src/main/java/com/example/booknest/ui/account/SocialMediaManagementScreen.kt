package com.example.booknest.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.booknest.ui.components.BackButton
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.CustomSocialLinkResponse
import com.example.booknest.domain.model.response.SocialMediaResponse
import com.example.booknest.ui.components.social.CustomLinkCard
import com.example.booknest.ui.account.components.social.SocialMediaInputCard
import com.example.booknest.ui.account.components.social.utils.isValidUrl
import com.example.booknest.viewmodel.profile.ProfileViewModel
import com.example.booknest.viewmodel.profile.ProfileSettingsViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaManagementScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    profileViewModel: ProfileViewModel = getViewModel(),
    profileSettingsViewModel: ProfileSettingsViewModel = getViewModel()
) {
    val myProfile by profileViewModel.myProfile.collectAsState()
    val isLoading by profileSettingsViewModel.isLoading.collectAsState()
    val error by profileSettingsViewModel.error.collectAsState()
    var instagram by remember { mutableStateOf("") }
    var tiktok by remember { mutableStateOf("") }
    var youtube by remember { mutableStateOf("") }
    var goodreads by remember { mutableStateOf("") }
    var customLinks by remember { mutableStateOf<List<CustomSocialLinkResponse>>(emptyList()) }

    var initialInstagram by remember { mutableStateOf<String?>(null) }
    var initialTiktok by remember { mutableStateOf<String?>(null) }
    var initialYoutube by remember { mutableStateOf<String?>(null) }
    var initialGoodreads by remember { mutableStateOf<String?>(null) }
    var initialCustomLinks by remember { mutableStateOf<List<CustomSocialLinkResponse>?>(null) }

    var showAddCustom by remember { mutableStateOf(false) }
    var newCustomPlatform by remember { mutableStateOf("") }
    var newCustomUrl by remember { mutableStateOf("") }
    var editingCustomIndex by remember { mutableStateOf<Int?>(null) }
    var hasJustSaved by remember { mutableStateOf(false) }

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

    LaunchedEffect(isLoading, error) {
        if (hasJustSaved && !isLoading && error == null) {
            hasJustSaved = false
            navController.popBackStack()
        } else if (hasJustSaved && !isLoading && error != null) {
            hasJustSaved = false
        }
    }

    LaunchedEffect(myProfile) {
        myProfile?.socialMedia?.let { socialMedia ->
            val newInstagram = socialMedia.instagram ?: ""
            val newTiktok = socialMedia.tiktok ?: ""
            val newYoutube = socialMedia.youtube ?: ""
            val newGoodreads = socialMedia.goodreads ?: ""
            val newCustomLinks = socialMedia.custom ?: emptyList()

            if (initialInstagram == null) {
                initialInstagram = newInstagram
                initialTiktok = newTiktok
                initialYoutube = newYoutube
                initialGoodreads = newGoodreads
                initialCustomLinks = newCustomLinks
            }
        }
    }

    val hasChanges = remember(
        instagram, tiktok, youtube, goodreads, customLinks,
        initialInstagram, initialTiktok, initialYoutube, initialGoodreads, initialCustomLinks
    ) {
        instagram.trim() != (initialInstagram ?: "") ||
                tiktok.trim() != (initialTiktok ?: "") ||
                youtube.trim() != (initialYoutube ?: "") ||
                goodreads.trim() != (initialGoodreads ?: "") ||
                customLinks != (initialCustomLinks ?: emptyList<CustomSocialLinkResponse>())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Social Media") },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Manage Your Social Media",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Connect your social media profiles to share with other BookNest users",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Text(
                    text = "Popular Platforms",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                SocialMediaInputCard(
                    platform = "Instagram",
                    value = instagram,
                    onValueChange = { instagram = it },
                    placeholder = "https://instagram.com/username"
                )
            }

            item {
                SocialMediaInputCard(
                    platform = "TikTok",
                    value = tiktok,
                    onValueChange = { tiktok = it },
                    placeholder = "https://tiktok.com/@username"
                )
            }

            item {
                SocialMediaInputCard(
                    platform = "YouTube",
                    value = youtube,
                    onValueChange = { youtube = it },
                    placeholder = "https://youtube.com/@username"
                )
            }

            item {
                SocialMediaInputCard(
                    platform = "Goodreads",
                    value = goodreads,
                    onValueChange = { goodreads = it },
                    placeholder = "https://goodreads.com/user/show/username"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom Links",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddCustom = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Custom")
                    }
                }
            }

            items(customLinks.size) { index ->
                val customLink = customLinks[index]
                CustomLinkCard(
                    customLink = customLink,
                    isEditing = editingCustomIndex == index,
                    onEdit = { editingCustomIndex = index },
                    onSave = { platform, url ->
                        customLinks = customLinks.toMutableList().apply {
                            set(index, CustomSocialLinkResponse(platform, url))
                        }
                        editingCustomIndex = null
                    },
                    onCancel = { editingCustomIndex = null },
                    onDelete = { linkToDelete ->
                        customLinks = customLinks.filter { it != linkToDelete }
                    }
                )
            }

            if (showAddCustom) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Add Custom Link",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newCustomPlatform,
                                onValueChange = { newCustomPlatform = it },
                                label = { Text("Platform Name") },
                                placeholder = { Text("e.g., Twitter, Personal Blog") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newCustomUrl,
                                onValueChange = { newCustomUrl = it },
                                label = { Text("URL") },
                                placeholder = { Text("https://example.com") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = !isCustomUrlValid,
                                supportingText = if (!isCustomUrlValid && newCustomUrl.isNotBlank()) {
                                    { Text("Please enter a valid URL starting with http:// or https://") }
                                } else null,
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        showAddCustom = false
                                        newCustomPlatform = ""
                                        newCustomUrl = ""
                                    }
                                ) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
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
                                    enabled = isCustomFormValid
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        hasJustSaved = true
                        profileSettingsViewModel.updateSocialMedia(
                            SocialMediaResponse(
                                instagram = instagram.takeIf { it.isNotBlank() },
                                tiktok = tiktok.takeIf { it.isNotBlank() },
                                youtube = youtube.takeIf { it.isNotBlank() },
                                goodreads = goodreads.takeIf { it.isNotBlank() },
                                custom = customLinks.takeIf { it.isNotEmpty() }
                            )
                        )
                        initialInstagram = instagram
                        initialTiktok = tiktok
                        initialYoutube = youtube
                        initialGoodreads = goodreads
                        initialCustomLinks = customLinks
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasChanges && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Save Changes")
                }
            }

            if (error != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
