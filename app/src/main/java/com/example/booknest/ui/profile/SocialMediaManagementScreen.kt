package com.example.booknest.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.collectLatest
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
import com.example.booknest.viewmodel.ProfileViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaManagementScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    profileViewModel: ProfileViewModel = getViewModel()
) {
    val myProfile by profileViewModel.myProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    LaunchedEffect(Unit) {
        profileViewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
            if (message.contains("updated successfully", ignoreCase = true)) {
                initialInstagram = instagram
                initialTiktok = tiktok
                initialYoutube = youtube
                initialGoodreads = goodreads
                initialCustomLinks = customLinks
                profileViewModel.loadMyProfile()
                navController.popBackStack()
            }
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    icon = "instagram",
                    value = instagram,
                    onValueChange = { instagram = it },
                    placeholder = "https://instagram.com/username"
                )
            }

            item {
                SocialMediaInputCard(
                    platform = "TikTok",
                    icon = "tiktok",
                    value = tiktok,
                    onValueChange = { tiktok = it },
                    placeholder = "https://tiktok.com/@username"
                )
            }

            item {
                SocialMediaInputCard(
                    platform = "YouTube",
                    icon = "youtube",
                    value = youtube,
                    onValueChange = { youtube = it },
                    placeholder = "https://youtube.com/@username"
                )
            }

            item {
                SocialMediaInputCard(
                    platform = "Goodreads",
                    icon = "goodreads",
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
                                modifier = Modifier.fillMaxWidth()
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
                                        if (newCustomPlatform.isNotBlank() && newCustomUrl.isNotBlank()) {
                                            customLinks = customLinks + CustomSocialLinkResponse(
                                                platform = newCustomPlatform,
                                                url = newCustomUrl
                                            )
                                            newCustomPlatform = ""
                                            newCustomUrl = ""
                                            showAddCustom = false
                                        }
                                    }
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
                        profileViewModel.updateSocialMedia(
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

@Composable
fun SocialMediaInputCard(
    platform: String,
    icon: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val hasValue = value.isNotBlank()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = platform,
                        modifier = Modifier.size(20.dp),
                        tint = if (hasValue)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = platform,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasValue)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                if (hasValue) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )
                }
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun CustomLinkCard(
    customLink: CustomSocialLinkResponse,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit,
    onDelete: (CustomSocialLinkResponse) -> Unit
) {
    var platform by remember { mutableStateOf(customLink.platform) }
    var url by remember { mutableStateOf(customLink.url) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        if (isEditing) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Edit Custom Link",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = platform,
                    onValueChange = { platform = it },
                    label = { Text("Platform Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(platform, url) }
                    ) {
                        Text("Save")
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = customLink.platform,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customLink.platform,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = customLink.url,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { onDelete(customLink) }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
