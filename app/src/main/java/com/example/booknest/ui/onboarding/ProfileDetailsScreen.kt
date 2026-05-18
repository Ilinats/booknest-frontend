package com.example.booknest.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.presentation.navigation.applyAuthUiEffect
import com.example.booknest.ui.components.Toast
import com.example.booknest.ui.components.ToastMessage
import com.example.booknest.ui.components.ToastType
import com.example.booknest.viewmodel.auth.SignupViewModel
import com.example.booknest.presentation.common.UiState
import com.example.booknest.ui.components.BackgroundDecoration
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(navController: NavController, viewModel: SignupViewModel) {
    val context = LocalContext.current
    val signupState by viewModel.signupState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.authUiEffect.collectLatest { effect ->
            navController.applyAuthUiEffect(effect)
        }
    }

    var bio by remember { mutableStateOf("") }

    var streetAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Toast(
            toastMessage = errorMessage?.let { ToastMessage(it, ToastType.ERROR) },
            onDismiss = { errorMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        BackgroundDecoration(modifier = Modifier.fillMaxSize())

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
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
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
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

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
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                        viewModel.updateBio(bio.ifBlank { null }, null)
                        viewModel.updateProfileDetails(
                            birthDate = viewModel.signupData.value.birthDate,
                            streetAddress = streetAddress,
                            city = city,
                            postalCode = postalCode,
                            country = country.ifBlank { null },
                            isPrimary = isPrimary
                        )
                        viewModel.submitSignup()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    enabled = signupState !is UiState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (signupState is UiState.Loading) {
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
