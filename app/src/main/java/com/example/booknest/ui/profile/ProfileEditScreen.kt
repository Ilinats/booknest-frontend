package com.example.booknest.ui.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.presentation.common.UiState
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.presentation.navigation.applyProfileUiEffect
import com.example.booknest.ui.components.BackButton
import com.example.booknest.ui.profile.components.edit.ProfileEditBottomSaveBar
import com.example.booknest.ui.profile.components.edit.ProfileEditDeleteAccountDialog
import com.example.booknest.ui.profile.components.edit.ProfileEditErrorBanner
import com.example.booknest.ui.profile.components.edit.sections.AccountSettingsSection
import com.example.booknest.ui.profile.components.edit.sections.BioSection
import com.example.booknest.ui.profile.components.edit.sections.DangerZoneSection
import com.example.booknest.ui.profile.components.edit.sections.PersonalInfoSection
import com.example.booknest.ui.profile.components.edit.sections.ProfilePictureSection
import com.example.booknest.ui.profile.components.edit.sections.SocialMediaSection
import com.example.booknest.data.session.SessionManager
import com.example.booknest.viewmodel.profile.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

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

    LaunchedEffect(Unit) {
        profileViewModel.profileUiEffect.collectLatest { effect ->
            navController.applyProfileUiEffect(effect)
        }
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

    fun performSave() {
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
    }

    val saveEnabled = hasChanges && isFormValid && editState !is UiState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                },
                actions = {
                    TextButton(
                        onClick = { performSave() },
                        enabled = saveEnabled
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
            ProfilePictureSection(
                avatarUrl = avatarUrl,
                selectedImageUri = selectedImageUri,
                isUploadingImage = isUploadingImage,
                isLoading = isLoading,
                onSelectImage = { imagePickerLauncher.launch("image/*") },
                onRemoveImage = {
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
                }
            )

            PersonalInfoSection(
                firstName = firstName,
                onFirstNameChange = {
                    firstName = it
                    firstNameError = null
                },
                firstNameError = firstNameError,
                lastName = lastName,
                onLastNameChange = {
                    lastName = it
                    lastNameError = null
                },
                lastNameError = lastNameError,
                username = username,
                onUsernameChange = {
                    username = it
                    usernameError = null
                },
                usernameError = usernameError,
                initialUsername = initialUsername,
                currentUserEmail = currentUser?.email,
                sessionUsername = currentUser?.username,
                isCheckingUsername = isCheckingUsername,
                onIsCheckingUsernameChange = { isCheckingUsername = it },
                usernameAvailable = usernameAvailable,
                onUsernameAvailableChange = { usernameAvailable = it },
            )

            BioSection(
                bio = bio,
                bioError = bioError,
                onBioChange = {
                    bio = it
                    bioError = null
                }
            )

            SocialMediaSection(
                onNavigateToSocialMediaManagement = {
                    navController.navigate(Screen.SocialMediaManagement.route)
                }
            )

            AccountSettingsSection(
                isAuthor = currentUser?.userType == "author",
                onPasswordResetClick = {
                    currentUser?.email?.let { email ->
                        navController.navigate(Screen.PasswordReset.createRoute(email))
                    }
                },
                onPrivacySettingsClick = { navController.navigate(Screen.PrivacySettings.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )

            DangerZoneSection(
                onDeleteAccountClick = { showDeleteAccountDialog = true }
            )

            if (showDeleteAccountDialog) {
                ProfileEditDeleteAccountDialog(
                    profileViewModel = profileViewModel,
                    onDismiss = { showDeleteAccountDialog = false }
                )
            }

            val editError = editState as? UiState.Error
            if (editError != null) {
                ProfileEditErrorBanner(message = editError.message)
            }

            ProfileEditBottomSaveBar(
                enabled = saveEnabled,
                editState = editState,
                onSaveClick = { performSave() }
            )
        }
    }
}
