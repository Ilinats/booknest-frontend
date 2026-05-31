package com.example.booknest.ui.profile.components.edit.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.data.service.AuthService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PersonalInfoSection(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    firstNameError: String?,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    lastNameError: String?,
    username: String,
    onUsernameChange: (String) -> Unit,
    usernameError: String?,
    initialUsername: String?,
    currentUserEmail: String?,
    sessionUsername: String?,
    isCheckingUsername: Boolean,
    onIsCheckingUsernameChange: (Boolean) -> Unit,
    usernameAvailable: Boolean?,
    onUsernameAvailableChange: (Boolean?) -> Unit,
) {
    val authService: AuthService = koinInject()
    val usernamePattern = remember { Regex("^[a-zA-Z0-9_.-]+$") }
    val isUsernameValid = remember(username) {
        username.length in 3..50 && usernamePattern.matches(username)
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(username) {
        if (username.isNotBlank() && username != sessionUsername) {
            onIsCheckingUsernameChange(true)
            delay(500)

            when {
                username.length < 3 -> {
                    onUsernameAvailableChange(null)
                    onIsCheckingUsernameChange(false)
                }

                username.length > 50 -> {
                    onUsernameAvailableChange(null)
                    onIsCheckingUsernameChange(false)
                }

                !usernamePattern.matches(username) -> {
                    onUsernameAvailableChange(null)
                    onIsCheckingUsernameChange(false)
                }

                else -> {
                    scope.launch {
                        try {
                            val response =
                                authService.checkUsernameAvailability(username)
                            if (response.isSuccessful) {
                                val body = response.body()
                                onUsernameAvailableChange(body?.available)
                            } else {
                                onUsernameAvailableChange(false)
                            }
                        } catch (_: Exception) {
                            onUsernameAvailableChange(false)
                        } finally {
                            onIsCheckingUsernameChange(false)
                        }
                    }
                }
            }
        } else {
            onUsernameAvailableChange(null)
            onIsCheckingUsernameChange(false)
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
                    onValueChange = onFirstNameChange,
                    label = { Text("First Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = firstNameError != null,
                    supportingText = firstNameError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = { Text("Last Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = lastNameError != null,
                    supportingText = lastNameError?.let { { Text(it) } }
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
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
                                Icons.Filled.CheckCircle,
                                contentDescription = "Available",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        usernameAvailable == false && username != (initialUsername
                            ?: "") -> {
                            Icon(
                                Icons.Filled.Close,
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
                value = currentUserEmail ?: "",
                onValueChange = { },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                enabled = false,
                trailingIcon = {
                    Icon(
                        Icons.Filled.Email,
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
}
