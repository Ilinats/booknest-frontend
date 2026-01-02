package com.example.booknest.ui.account

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.components.ErrorToast
import com.example.booknest.ui.theme.BackgroundWhite
import com.example.booknest.ui.theme.DarkNavyBlue
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import com.example.booknest.viewmodel.SignupUiState
import com.example.booknest.viewmodel.SignupViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(navController: NavController, viewModel: SignupViewModel) {
    val context = LocalContext.current
    val signupState by viewModel.signupState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(signupState) {
        if (signupState is SignupUiState.Error) {
            val error = (signupState as SignupUiState.Error).error
            errorMessage = error
        }
    }

    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var streetAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(true) }

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
            .background(Color(0xFFF1E9EE))
    ) {
        ErrorToast(
            message = errorMessage,
            onDismiss = { errorMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-175).dp, y = (-175).dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-135).dp, y = (-135).dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 175.dp, y = 175.dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 135.dp, y = 135.dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(SkyBluePeriwinkle)
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(DarkNavyBlue, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(DarkNavyBlue, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(DarkNavyBlue, RoundedCornerShape(2.dp))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    "Tell Us About\nYourself",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DarkNavyBlue,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8DFE4)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    imagePickerLauncher.launch("image/*")
                                },
                                modifier = Modifier.weight(1f),
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
                }

                OutlinedTextField(
                    value = bio,
                    onValueChange = {
                        if (it.length <= 500) {
                            bio = it
                        }
                    },
                    placeholder = { Text("Bio", color = Color(0xFF757575)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    maxLines = 10,
                    isError = bio.length > 500,
                    supportingText = {
                        if (bio.length > 500) {
                            Text(
                                "Bio must be 500 characters or less",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("${bio.length}/500 characters (optional)")
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFE8DFE4),
                        unfocusedContainerColor = Color(0xFFE8DFE4),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8DFE4)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Shipping Address (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Add your address if you want to apply for physical copies of books. Authors may send physical books to reviewers, and we'll use this address for shipping.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = streetAddress,
                            onValueChange = { if (it.length <= 255) streetAddress = it },
                            label = { Text("Street Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = streetAddress.isNotBlank() && (streetAddress.isEmpty() || streetAddress.length > 255),
                            placeholder = { Text("123 Main Street") }
                        )

                        OutlinedTextField(
                            value = city,
                            onValueChange = { if (it.length <= 100) city = it },
                            label = { Text("City") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = city.isNotBlank() && (city.isEmpty() || city.length > 100),
                            placeholder = { Text("New York") }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = postalCode,
                                onValueChange = { if (it.length <= 20) postalCode = it },
                                label = { Text("Postal Code") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = postalCode.isNotBlank() && (postalCode.length < 1 || postalCode.length > 20),
                                supportingText = {
                                    if (postalCode.isNotBlank() && postalCode.length > 20) {
                                        Text(
                                            "Max 20 chars",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else if (postalCode.isNotBlank()) {
                                        Text("✓", color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                placeholder = { Text("10001") }
                            )

                            OutlinedTextField(
                                value = country,
                                onValueChange = { if (it.length <= 100) country = it },
                                label = { Text("Country") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                isError = country.isNotBlank() && country.length > 100,
                                supportingText = {
                                    if (country.isNotBlank() && country.length > 100) {
                                        Text(
                                            "Max 100 chars",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else if (country.isNotBlank()) {
                                        Text("✓", color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                placeholder = { Text("USA") }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isPrimary,
                                onCheckedChange = { isPrimary = it }
                            )
                            Text(
                                "Set as primary address",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }


                Button(
                    onClick = {
                        selectedImageUri?.let { uri ->
                            viewModel.pendingImageUri = uri
                        }
                        viewModel.updateBio(bio.ifBlank { null }, null)
                        viewModel.updateProfileDetails(
                            birthDate = viewModel.signupData.birthDate,
                            streetAddress = streetAddress,
                            city = city,
                            postalCode = postalCode,
                            country = country.ifBlank { null },
                            isPrimary = isPrimary
                        )
                        viewModel.submitSignup { success, error ->
                            if (success) {
                                selectedImageUri?.let { uri ->
                                    viewModel.uploadProfileImage(context, uri) { uploadedUrl ->
                                        println("Profile image uploaded: $uploadedUrl")
                                    }
                                }
                                val userEmail = viewModel.signupData.email
                                navController.navigate(
                                    Screen.EmailVerification.createRoute(
                                        userEmail
                                    )
                                ) {
                                    popUpTo(Screen.ProfileDetails.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    enabled = signupState !is SignupUiState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkNavyBlue
                    )
                ) {
                    if (signupState is SignupUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            "Finish Sign Up",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
