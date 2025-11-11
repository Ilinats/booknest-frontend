package com.example.booknest.ui.profile

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.CustomSocialLink
import com.example.booknest.network.SocialMedia
import com.example.booknest.viewmodel.ProfileViewModel
import com.example.booknest.viewmodel.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaManagementScreen(
    navController: NavController,
    authManager: AuthManager,
    profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(authManager)
    )
) {
    val myProfile by profileViewModel.myProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    
    // Social media state
    var instagram by remember { mutableStateOf("") }
    var tiktok by remember { mutableStateOf("") }
    var youtube by remember { mutableStateOf("") }
    var goodreads by remember { mutableStateOf("") }
    var customLinks by remember { mutableStateOf<List<CustomSocialLink>>(emptyList()) }
    
    // UI state
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Social Media") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
            
            // Predefined Social Media Platforms
            item {
                Text(
                    text = "Popular Platforms",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Instagram
            item {
                SocialMediaInputCard(
                    platform = "Instagram",
                    icon = "instagram",
                    value = instagram,
                    onValueChange = { instagram = it },
                    placeholder = "https://instagram.com/username"
                )
            }
            
            // TikTok
            item {
                SocialMediaInputCard(
                    platform = "TikTok",
                    icon = "tiktok",
                    value = tiktok,
                    onValueChange = { tiktok = it },
                    placeholder = "https://tiktok.com/@username"
                )
            }
            
            // YouTube
            item {
                SocialMediaInputCard(
                    platform = "YouTube",
                    icon = "youtube",
                    value = youtube,
                    onValueChange = { youtube = it },
                    placeholder = "https://youtube.com/@username"
                )
            }
            
            // Goodreads
            item {
                SocialMediaInputCard(
                    platform = "Goodreads",
                    icon = "goodreads",
                    value = goodreads,
                    onValueChange = { goodreads = it },
                    placeholder = "https://goodreads.com/user/show/username"
                )
            }
            
            // Custom Links Section
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
            
            // Custom Links List
            items(customLinks.size) { index ->
                val customLink = customLinks[index]
                CustomLinkCard(
                    customLink = customLink,
                    isEditing = editingCustomIndex == index,
                    onEdit = { editingCustomIndex = index },
                    onSave = { platform, url ->
                        customLinks = customLinks.toMutableList().apply {
                            set(index, CustomSocialLink(platform, url))
                        }
                        editingCustomIndex = null
                    },
                    onCancel = { editingCustomIndex = null },
                    onDelete = { linkToDelete ->
                        customLinks = customLinks.filter { it != linkToDelete }
                    }
                )
            }
            
            // Add Custom Link Dialog
            if (showAddCustom) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                            customLinks = customLinks + CustomSocialLink(
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
            
            // Save Button
            item {
                Button(
                    onClick = {
                        profileViewModel.updateSocialMedia(
                            SocialMedia(
                                instagram = instagram.takeIf { it.isNotBlank() },
                                tiktok = tiktok.takeIf { it.isNotBlank() },
                                youtube = youtube.takeIf { it.isNotBlank() },
                                goodreads = goodreads.takeIf { it.isNotBlank() },
                                custom = customLinks.takeIf { it.isNotEmpty() }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }
            }
            
            // Error Display
            if (error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = platform,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = platform,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun CustomLinkCard(
    customLink: CustomSocialLink,
    isEditing: Boolean,
    onEdit: () -> Unit,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit,
    onDelete: (CustomSocialLink) -> Unit
) {
    var platform by remember { mutableStateOf(customLink.platform) }
    var url by remember { mutableStateOf(customLink.url) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
