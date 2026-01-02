package com.example.booknest.ui.account

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.components.ErrorToast
import com.example.booknest.viewmodel.SignupUiState
import com.example.booknest.viewmodel.SignupViewModel

@Composable
fun BioScreen(navController: NavController, viewModel: SignupViewModel) {
    val context = LocalContext.current
    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val signupState by viewModel.signupState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(signupState) {
        if (signupState is SignupUiState.Error) {
            val error = (signupState as SignupUiState.Error).error
            errorMessage = error
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ErrorToast(
            message = errorMessage,
            onDismiss = { errorMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Tell us about yourself",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(0.85f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Profile Picture",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable(enabled = signupState !is SignupUiState.Loading) {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
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

                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = signupState !is SignupUiState.Loading
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Select Image",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedImageUri != null) "Change Photo" else "Add Photo")
                    }
                }
            }

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio (optional)") },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(120.dp),
                maxLines = 4
            )

            Button(
                onClick = {
                    selectedImageUri?.let { uri ->
                        viewModel.pendingImageUri = uri
                    }
                    viewModel.updateBio(bio.ifBlank { null }, null)
                    viewModel.submitSignup { success, error ->
                        if (success) {
                            selectedImageUri?.let { uri ->
                                viewModel.uploadProfileImage(context, uri) { uploadedUrl ->
                                    println("Profile image uploaded: $uploadedUrl")
                                }
                            }
                            val userEmail = viewModel.signupData.email
                            navController.navigate("email_verification?email=$userEmail") {
                                popUpTo(Screen.AccountType.route) { inclusive = false }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                enabled = signupState !is SignupUiState.Loading
            ) {
                if (signupState is SignupUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Finish Signup")
            }

        }
    }
}