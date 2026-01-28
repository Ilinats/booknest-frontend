package com.example.booknest.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.data.session.SessionManager
import com.example.booknest.data.service.AuthService
import com.example.booknest.viewmodel.ProfileViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    profileViewModel: ProfileViewModel = getViewModel()
) {
    val context = LocalContext.current
    val currentUser by sessionManager.currentUser.collectAsState()
    val editState by profileViewModel.profileEditState.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    var firstName by remember { mutableStateOf(currentUser?.firstName ?: "") }
    var lastName by remember { mutableStateOf(currentUser?.lastName ?: "") }
    var username by remember { mutableStateOf(currentUser?.username ?: "") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var avatarUrl by remember { mutableStateOf(currentUser?.avatarUrl ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var shouldRemoveAvatar by remember { mutableStateOf(false) }

    var initialFirstName by remember { mutableStateOf<String?>(null) }
    var initialLastName by remember { mutableStateOf<String?>(null) }
    var initialUsername by remember { mutableStateOf<String?>(null) }
    var initialBio by remember { mutableStateOf<String?>(null) }
    var initialAvatarUrl by remember { mutableStateOf<String?>(null) }

    var isCheckingUsername by remember { mutableStateOf(false) }
    var usernameStatus by remember { mutableStateOf<UsernameStatus>(UsernameStatus.Idle) }
    var usernameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var bioError by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isUploadingImage = true
            profileViewModel.uploadProfileImage(context, it)
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadMyProfile()
    }

    val myProfile by profileViewModel.myProfile.collectAsState()

    LaunchedEffect(myProfile) {
        if (isUploadingImage && myProfile?.avatarUrl?.isNotBlank() == true) {
            avatarUrl = myProfile?.avatarUrl ?: ""
            shouldRemoveAvatar = false
            isUploadingImage = false
        }
    }

    var profileLoadedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(myProfile) {
        myProfile?.let { profile ->
            val newFirstName = profile.firstName ?: ""
            val newLastName = profile.lastName ?: ""
            val newUsername = profile.username ?: ""
            val newBio = profile.bio ?: ""
            val newAvatarUrl = profile.avatarUrl ?: ""

            if (!profileLoadedOnce) {
                firstName = newFirstName
                lastName = newLastName
                username = newUsername
                bio = newBio
                avatarUrl = newAvatarUrl

                initialFirstName = newFirstName
                initialLastName = newLastName
                initialUsername = newUsername
                initialBio = newBio
                initialAvatarUrl = newAvatarUrl

                profileLoadedOnce = true
                Log.d(
                    "ProfileEditScreen",
                    "Initial values set: firstName='$initialFirstName', lastName='$initialLastName', bio='$initialBio', avatarUrl='$initialAvatarUrl'"
                )
            } else {
                if (firstName == newFirstName && lastName == newLastName &&
                    username == newUsername && bio == newBio && avatarUrl == newAvatarUrl
                ) {
                    firstName = newFirstName
                    lastName = newLastName
                    username = newUsername
                    bio = newBio
                    avatarUrl = newAvatarUrl
                    selectedImageUri = null
                    shouldRemoveAvatar = false

                    initialFirstName = newFirstName
                    initialLastName = newLastName
                    initialUsername = newUsername
                    initialBio = newBio
                    initialAvatarUrl = newAvatarUrl
                }
            }
        }
    }

    LaunchedEffect(firstName, lastName, username, bio) {
        firstNameError = if (firstName.isNotBlank() && firstName.length > 50) {
            "First name must be 50 characters or less"
        } else null

        lastNameError = if (lastName.isNotBlank() && lastName.length > 50) {
            "Last name must be 50 characters or less"
        } else null

        usernameError = when {
            username.isBlank() -> "Username is required"
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 50 -> "Username must be 50 characters or less"
            !username.matches(Regex("^[a-zA-Z0-9_.-]+$")) -> "Username can only contain letters, numbers, dots, hyphens, and underscores"
            username != (initialUsername
                ?: "") && usernameAvailable == false -> "Username is not available"

            else -> null
        }

        bioError = if (bio.length > 1000) {
            "Bio must be 1000 characters or less"
        } else null
    }

    val hasChanges = remember(
        firstName,
        lastName,
        username,
        bio,
        avatarUrl,
        selectedImageUri,
        shouldRemoveAvatar,
        initialFirstName,
        initialLastName,
        initialUsername,
        initialBio,
        initialAvatarUrl
    ) {
        val firstNameChanged = firstName.trim() != (initialFirstName?.trim() ?: "")
        val lastNameChanged = lastName.trim() != (initialLastName?.trim() ?: "")
        val usernameChanged = username.trim() != (initialUsername?.trim() ?: "")
        val bioChanged = bio.trim() != (initialBio?.trim() ?: "")

        val hadInitialAvatar = !initialAvatarUrl.isNullOrBlank()
        val avatarChanged = selectedImageUri != null ||
                (shouldRemoveAvatar && hadInitialAvatar) ||
                (!shouldRemoveAvatar && avatarUrl.trim() != (initialAvatarUrl?.trim() ?: ""))

        val hasChangesResult =
            firstNameChanged || lastNameChanged || usernameChanged || bioChanged || avatarChanged

        if (hasChangesResult) {
            Log.d(
                "ProfileEditScreen",
                "hasChanges=true: firstName=$firstNameChanged, lastName=$lastNameChanged, username=$usernameChanged, bio=$bioChanged, avatar=$avatarChanged"
            )
            Log.d(
                "ProfileEditScreen",
                "  firstName: '${firstName.trim()}' vs '${initialFirstName?.trim() ?: ""}'"
            )
            Log.d(
                "ProfileEditScreen",
                "  lastName: '${lastName.trim()}' vs '${initialLastName?.trim() ?: ""}'"
            )
            Log.d("ProfileEditScreen", "  bio: '${bio.trim()}' vs '${initialBio?.trim() ?: ""}'")
            Log.d(
                "ProfileEditScreen",
                "  shouldRemoveAvatar=$shouldRemoveAvatar, hadInitialAvatar=$hadInitialAvatar"
            )
        }

        hasChangesResult
    }

    val isFormValid =
        remember(
            firstNameError,
            lastNameError,
            usernameError,
            bioError,
            usernameAvailable,
            username,
            initialUsername
        ) {
            firstNameError == null &&
                    lastNameError == null &&
                    usernameError == null &&
                    bioError == null &&
                    (username == (initialUsername ?: "") || usernameAvailable == true)
        }


    LaunchedEffect(editState) {
        when (editState) {
            is UiState.Success -> {
                initialFirstName = firstName.trim()
                initialLastName = lastName.trim()
                initialUsername = username.trim()
                initialBio = bio.trim()

                if (shouldRemoveAvatar) {
                    avatarUrl = ""
                    initialAvatarUrl = ""
                    shouldRemoveAvatar = false
                } else {
                    initialAvatarUrl = avatarUrl
                }

                profileViewModel.loadMyProfile()

                try {
                    val profilesService = org.koin.core.context.GlobalContext.get()
                        .get<com.example.booknest.data.service.ProfilesService>()
                    val response = profilesService.getMe()
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            sessionManager.updateUser(user)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileEditScreen", "Failed to fetch user after update: ${e.message}")
                }

                navController.popBackStack()
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                },
                actions = {
                    TextButton(
                        onClick = {
                            firstNameError = null
                            lastNameError = null
                            usernameError = null
                            bioError = null

                            if (isFormValid) {
                                if (shouldRemoveAvatar) {
                                    profileViewModel.removeAvatar()
                                } else {
                                    val usernameToUpdate =
                                        if (username.trim() != (initialUsername?.trim()
                                                ?: "") && username.trim().isNotBlank()
                                        ) {
                                            username.trim()
                                        } else {
                                            null
                                        }
                                    profileViewModel.updateProfile(
                                        username = usernameToUpdate,
                                        firstName = firstName.takeIf { it.isNotBlank() },
                                        lastName = lastName.takeIf { it.isNotBlank() },
                                        bio = bio.takeIf { it.isNotBlank() },
                                        avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else null
                                    )
                                    initialFirstName = firstName.trim()
                                    initialLastName = lastName.trim()
                                    initialUsername = username.trim()
                                    initialBio = bio.trim()
                                    initialAvatarUrl = avatarUrl
                                }
                            }
                        },
                        enabled = hasChanges && isFormValid && editState !is UiState.Loading
                    ) {
                        if (editState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Profile Picture",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !isUploadingImage && !isLoading) {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isUploadingImage -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp
                                )
                            }

                            selectedImageUri != null -> {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            !avatarUrl.isNullOrBlank() -> {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            else -> {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Add Profile Picture",
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isUploadingImage && !isLoading
                        ) {
                            if (isUploadingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Select Image",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (avatarUrl.isNotBlank()) "Change" else "Add Photo")
                            }
                        }

                        if (avatarUrl.isNotBlank() || selectedImageUri != null) {
                            OutlinedButton(
                                onClick = {
                                    Log.d(
                                        "ProfileEditScreen",
                                        "Remove button clicked. Current avatarUrl: $avatarUrl, initialAvatarUrl: $initialAvatarUrl"
                                    )
                                    avatarUrl = ""
                                    selectedImageUri = null
                                    shouldRemoveAvatar = true
                                    Log.d(
                                        "ProfileEditScreen",
                                        "After remove click: shouldRemoveAvatar=$shouldRemoveAvatar, hasChanges should be: ${shouldRemoveAvatar && !initialAvatarUrl.isNullOrBlank()}"
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isUploadingImage && !isLoading
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove")
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = {
                                firstName = it
                                firstNameError = null
                            },
                            label = { Text("First Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = firstNameError != null,
                            supportingText = firstNameError?.let { { Text(it) } }
                        )

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = {
                                lastName = it
                                lastNameError = null
                            },
                            label = { Text("Last Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = lastNameError != null,
                            supportingText = lastNameError?.let { { Text(it) } }
                        )
                    }

                    val authService: AuthService = org.koin.compose.koinInject()
                    val usernamePattern = remember { Regex("^[a-zA-Z0-9_.-]+$") }
                    val isUsernameValid = remember(username) {
                        username.length in 3..50 && usernamePattern.matches(username)
                    }

                    val scope = rememberCoroutineScope()
                    LaunchedEffect(username) {
                        if (username.isNotBlank() && username != currentUser?.username) {
                            isCheckingUsername = true
                            delay(500)

                            when {
                                username.length < 3 -> {
                                    usernameStatus = UsernameStatus.TooShort
                                    usernameAvailable = null
                                    isCheckingUsername = false
                                }

                                username.length > 50 -> {
                                    usernameStatus = UsernameStatus.TooLong
                                    usernameAvailable = null
                                    isCheckingUsername = false
                                }

                                !usernamePattern.matches(username) -> {
                                    usernameStatus = UsernameStatus.InvalidFormat
                                    usernameAvailable = null
                                    isCheckingUsername = false
                                }

                                else -> {
                                    usernameStatus = UsernameStatus.ValidFormat
                                    scope.launch {
                                        try {
                                            val response =
                                                authService.checkUsernameAvailability(username)
                                            if (response.isSuccessful) {
                                                val body = response.body()
                                                usernameAvailable = body?.available
                                            } else {
                                                usernameAvailable = false
                                            }
                                        } catch (e: Exception) {
                                            usernameAvailable = false
                                        } finally {
                                            isCheckingUsername = false
                                        }
                                    }
                                }
                            }
                        } else {
                            usernameStatus = UsernameStatus.Idle
                            usernameAvailable = null
                            isCheckingUsername = false
                        }
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            usernameError = null
                        },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("@username") },
                        trailingIcon = {
                            when {
                                isCheckingUsername -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                usernameAvailable == true && username != (initialUsername
                                    ?: "") -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Available",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                usernameAvailable == false && username != (initialUsername
                                    ?: "") -> {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Unavailable",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        isError = usernameError != null || (usernameAvailable == false && username != (initialUsername
                            ?: "")),
                        supportingText = {
                            when {
                                isCheckingUsername -> Text("Checking availability...")
                                usernameError != null -> Text(
                                    usernameError ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )

                                usernameAvailable == true && username != (initialUsername
                                    ?: "") -> Text(
                                    "Username is available",
                                    color = MaterialTheme.colorScheme.primary
                                )

                                usernameAvailable == false && username != (initialUsername
                                    ?: "") -> Text(
                                    "Username is already taken",
                                    color = MaterialTheme.colorScheme.error
                                )

                                !isUsernameValid && username.isNotBlank() -> Text(
                                    "Username must be 3-30 characters and contain only letters, numbers, and underscores",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = currentUser?.email ?: "",
                        onValueChange = { },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        supportingText = {
                            Text(
                                "Email cannot be changed",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Bio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = {
                            bio = it
                            bioError = null
                        },
                        label = { Text("Tell us about yourself") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        placeholder = { Text("Write a short bio about yourself...") },
                        isError = bioError != null,
                        supportingText = bioError?.let { { Text(it) } }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Character limit: 1000",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${bio.length}/1000",
                            fontSize = 12.sp,
                            color = if (bio.length > 1000)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Social Media",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Manage your social media links",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { navController.navigate(Screen.SocialMediaManagement.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Social Media Links")
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Account Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentUser?.email?.let { email ->
                                    navController.navigate(Screen.PasswordReset.createRoute(email))
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Change Password",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Update your account password",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentUser?.userType != "author") {
                        Divider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("privacy_settings") },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Privacy Settings",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Control your privacy preferences",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Divider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Screen.Notifications.route) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Notification Preferences",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Manage your notification settings",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

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
                        text = "Danger Zone",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account")
                    }
                }
            }

            if (showDeleteAccountDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAccountDialog = false },
                    title = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    text = {
                        Text(
                            "Are you sure you want to delete your account? This action cannot be undone. All your data, applications, and reviews will be permanently deleted."
                        )
                    },
                    confirmButton = {
                        val scope = rememberCoroutineScope()
                        var isDeleting by remember { mutableStateOf(false) }
                        TextButton(
                            onClick = {
                                if (!isDeleting) {
                                    isDeleting = true
                                    scope.launch {
                                        profileViewModel.deleteAccount()
                                        showDeleteAccountDialog = false
                                    }
                                }
                            },
                            enabled = !isDeleting,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    strokeWidth = 2.dp
                                )
                            } else {
                            Text("Delete")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteAccountDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            val currentEditState = editState
            if (currentEditState is UiState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = (currentEditState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Button(
                onClick = {
                    firstNameError = null
                    lastNameError = null
                    usernameError = null
                    bioError = null

                    if (isFormValid) {
                        if (shouldRemoveAvatar) {
                            profileViewModel.removeAvatar()
                        } else {
                            val usernameToUpdate = if (username.trim() != (initialUsername?.trim()
                                    ?: "") && username.trim().isNotBlank()
                            ) {
                                username.trim()
                            } else {
                                null
                            }
                            profileViewModel.updateProfile(
                                username = usernameToUpdate,
                                firstName = firstName.takeIf { it.isNotBlank() },
                                lastName = lastName.takeIf { it.isNotBlank() },
                                bio = bio.takeIf { it.isNotBlank() },
                                avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else null
                            )
                            initialFirstName = firstName.trim()
                            initialLastName = lastName.trim()
                            initialUsername = username.trim()
                            initialBio = bio.trim()
                            initialAvatarUrl = avatarUrl
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = hasChanges && isFormValid && editState !is UiState.Loading
            ) {
                if (editState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Save Changes")
            }
        }
    }
}

private sealed class UsernameStatus {
    object Idle : UsernameStatus()
    object TooShort : UsernameStatus()
    object TooLong : UsernameStatus()
    object InvalidFormat : UsernameStatus()
    object ValidFormat : UsernameStatus()
}
