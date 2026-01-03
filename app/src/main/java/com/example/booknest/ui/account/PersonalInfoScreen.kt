package com.example.booknest.ui.account

import android.R
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.SignupViewModel
import com.example.booknest.data.service.AuthService
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(navController: NavController, viewModel: SignupViewModel) {
    val authService: AuthService = koinInject()
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var birthDate by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = null
    )

    val formattedDate = remember(birthDate) {
        birthDate?.let {
            try {
                val inputFormat =
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat =
                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date ?: java.util.Date())
            } catch (e: Exception) {
                it
            }
        } ?: ""
    }

    val isEmailValid = remember(email) {
        email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                email.length <= 255
    }

    val isFirstNameValid = remember(firstName) {
        firstName.length in 1..100
    }

    val isLastNameValid = remember(lastName) {
        lastName.length in 1..100
    }

    val usernamePattern = remember { Regex("^[a-zA-Z0-9_.-]+$") }
    val isUsernameValid = remember(username) {
        username.length in 3..50 && usernamePattern.matches(username)
    }

    var isCheckingUsername by remember { mutableStateOf(false) }
    var usernameStatus by remember { mutableStateOf<UsernameStatus>(UsernameStatus.Idle) }
    var usernameAvailable by remember { mutableStateOf<Boolean?>(null) }

    var isCheckingEmail by remember { mutableStateOf(false) }
    var emailAvailable by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(username) {
        if (username.isNotBlank()) {
            isCheckingUsername = true
            kotlinx.coroutines.delay(500)

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
                            val response = authService.checkUsernameAvailability(username)
                            if (response.isSuccessful) {
                                val body = response.body()
                                if (body != null) {
                                    usernameAvailable = body.available
                                    Log.d(
                                        "PersonalInfoScreen",
                                        "Username '$username' availability: ${body.available}"
                                    )
                                } else {
                                    Log.w(
                                        "PersonalInfoScreen",
                                        "Username check response body is null"
                                    )
                                    usernameAvailable = null
                                }
                            } else {
                                Log.w(
                                    "PersonalInfoScreen",
                                    "Username check failed with code: ${response.code()}, message: ${response.message()}"
                                )
                                usernameAvailable = false
                            }
                        } catch (e: Exception) {
                            Log.e("PersonalInfoScreen", "Error checking username availability", e)
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

    LaunchedEffect(email) {
        if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() && email.length <= 255
        ) {
            isCheckingEmail = true
            kotlinx.coroutines.delay(500)

            scope.launch {
                try {
                    val response = authService.checkEmailAvailability(email)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            emailAvailable = body.available
                            Log.d(
                                "PersonalInfoScreen",
                                "Email '$email' availability: ${body.available}"
                            )
                        } else {
                            Log.w("PersonalInfoScreen", "Email check response body is null")
                            emailAvailable = null
                        }
                    } else {
                        Log.w(
                            "PersonalInfoScreen",
                            "Email check failed with code: ${response.code()}, message: ${response.message()}"
                        )
                        emailAvailable = false
                    }
                } catch (e: Exception) {
                    Log.e("PersonalInfoScreen", "Error checking email availability", e)
                    emailAvailable = false
                } finally {
                    isCheckingEmail = false
                }
            }
        } else {
            emailAvailable = null
            isCheckingEmail = false
        }
    }

    val isPasswordValid = remember(password) {
        password.length in 8..128
    }

    val passwordStrength = calculatePasswordStrength(password)

    val isBirthDateValid = remember(birthDate) {
        birthDate?.let {
            val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")
            if (!datePattern.matches(it)) {
                return@remember false
            }
            try {
                val dateFormat =
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val birthDateObj = dateFormat.parse(it)
                val today = java.util.Calendar.getInstance()
                val birthCalendar = java.util.Calendar.getInstance()
                birthDateObj?.let { birthCalendar.time = it }

                var age =
                    today.get(java.util.Calendar.YEAR) - birthCalendar.get(java.util.Calendar.YEAR)
                val monthDiff =
                    today.get(java.util.Calendar.MONTH) - birthCalendar.get(java.util.Calendar.MONTH)
                if (monthDiff < 0 || (monthDiff == 0 && today.get(java.util.Calendar.DAY_OF_MONTH) < birthCalendar.get(
                        java.util.Calendar.DAY_OF_MONTH
                    ))
                ) {
                    age--
                }

                age >= 10
            } catch (e: Exception) {
                false
            }
        } ?: false
    }

    val isFormValid = isFirstNameValid &&
            isLastNameValid &&
            isEmailValid &&
            emailAvailable != false &&
            isUsernameValid &&
            usernameAvailable != false &&
            isPasswordValid &&
            password == confirmPassword &&
            isBirthDateValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-175).dp, y = (-175).dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-135).dp, y = (-135).dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 175.dp, y = 175.dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 135.dp, y = 135.dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
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
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(2.dp)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    "Create\nYour Account",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "First Name *",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { if (it.length <= 100) firstName = it },
                        placeholder = {
                            Text(
                                "First Name",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    when {
                        firstName.isBlank() -> Text(
                            "Required (1-100 characters)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        firstName.length > 100 -> Text(
                            "First name must be 100 characters or less",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        !isFirstNameValid -> Text(
                            "First name is required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        else -> Text(
                            "✓ ${firstName.length}/100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Last Name *",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { if (it.length <= 100) lastName = it },
                        placeholder = { Text("Last Name", color = Color(0xFF757575)) },
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
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    when {
                        lastName.isBlank() -> Text(
                            "Required (1-100 characters)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        lastName.length > 100 -> Text(
                            "Last name must be 100 characters or less",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        !isLastNameValid -> Text(
                            "Last name is required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        else -> Text(
                            "✓ ${lastName.length}/100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Email *",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (it.length <= 255) email = it },
                        placeholder = { Text("Email", color = Color(0xFF757575)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(28.dp)
                            ),
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        trailingIcon = {
                            when {
                                isCheckingEmail -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                emailAvailable == true && isEmailValid -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Available",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                emailAvailable == false -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Taken",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }

                                isEmailValid -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Valid",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                else -> {}
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    when {
                        email.isBlank() -> Text(
                            "Required (max 255 characters)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        email.length > 255 -> Text(
                            "Email must be 255 characters or less",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> Text(
                            "Please enter a valid email address",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        isCheckingEmail -> Text(
                            "Checking availability...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        emailAvailable == false -> Text(
                            "Email is already registered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        emailAvailable == true -> Text(
                            "✓ Email is available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        isEmailValid -> Text(
                            "✓ Valid email",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        else -> {}
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Username *",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Username", color = Color(0xFF757575)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(28.dp)
                            ),
                        singleLine = true,
                        trailingIcon = {
                            when {
                                isCheckingUsername -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                usernameStatus is UsernameStatus.ValidFormat && usernameAvailable == true -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Available",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                usernameStatus is UsernameStatus.ValidFormat && usernameAvailable == false -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Taken",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }

                                usernameStatus is UsernameStatus.ValidFormat -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Valid",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                else -> {}
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    when (val status = usernameStatus) {
                        is UsernameStatus.Idle -> Text(
                            "3-50 characters, letters, numbers, _, ., -",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        is UsernameStatus.TooShort -> Text(
                            "Username must be at least 3 characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        is UsernameStatus.TooLong -> Text(
                            "Username must be 50 characters or less",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        is UsernameStatus.InvalidFormat -> Text(
                            "Invalid format. Use letters, numbers, _, ., -",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        is UsernameStatus.ValidFormat -> {
                            when {
                                isCheckingUsername -> Text(
                                    "Checking availability...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                usernameAvailable == false -> Text(
                                    "Username is already taken",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                usernameAvailable == true -> Text(
                                    "✓ Username is available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                else -> Text(
                                    "✓ Format valid",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Password *",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { if (it.length <= 128) password = it },
                        placeholder = { Text("Password", color = Color(0xFF757575)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(28.dp)
                            ),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color(0xFF757575)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    if (password.isNotBlank()) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            when {
                                password.length < 8 -> Text(
                                    "Password must be at least 8 characters",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )

                                password.length > 128 -> Text(
                                    "Password must be 128 characters or less",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )

                                !isPasswordValid -> Text(
                                    "Password must be 8-128 characters",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )

                                else -> Text(
                                    "Strength: ${passwordStrength.label} (${password.length}/128)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575)
                                )
                            }
                            if (isPasswordValid) {
                                LinearProgressIndicator(
                                    progress = passwordStrength.strength,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = passwordStrength.color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            "Required (8-128 characters)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Confirm Password *",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Confirm Password", color = Color(0xFF757575)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(28.dp)
                            ),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                confirmPasswordVisible = !confirmPasswordVisible
                            }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = Color(0xFF757575)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    when {
                        confirmPassword.isBlank() -> {}
                        password != confirmPassword -> Text(
                            "Passwords don't match",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        else -> Text(
                            "✓",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Birth Date *",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = { },
                        placeholder = { Text("Birthday", color = Color(0xFF757575)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(28.dp)
                            )
                            .clickable { showDatePicker = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = Color(0xFF757575)
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    when {
                        birthDate == null && firstName.isNotBlank() -> Text(
                            "Birth date is required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        birthDate != null && !isBirthDateValid -> {
                            val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")
                            val errorText = if (datePattern.matches(birthDate ?: "")) {
                                "You must be at least 10 years old"
                            } else {
                                "Invalid date format (YYYY-MM-DD)"
                            }
                            Text(
                                errorText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        isBirthDateValid -> Text(
                            "✓ Valid birth date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        else -> Text(
                            "Select your birth date (YYYY-MM-DD, must be at least 10 years old)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.updatePersonalInfo(firstName, lastName, email, password)
                        viewModel.updateUsername(username)
                        viewModel.updateBirthDate(birthDate)
                        navController.navigate(Screen.ProfileDetails.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    enabled = isFormValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    )
                ) {
                    Text(
                        "Continue",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = if (isFormValid) Color.White else Color(0xFF757575)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        birthDate = dateFormat.format(Date(it))
                    }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false },
                datePickerState = datePickerState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength("", 0f, MaterialTheme.colorScheme.error)

    var strength = 0f
    var label = "Weak"
    var color = MaterialTheme.colorScheme.error

    if (password.length >= 8) strength += 0.25f
    if (password.length >= 12) strength += 0.15f

    if (password.any { it.isUpperCase() }) strength += 0.2f
    if (password.any { it.isLowerCase() }) strength += 0.2f
    if (password.any { it.isDigit() }) strength += 0.1f
    if (password.any { !it.isLetterOrDigit() }) strength += 0.1f

    when {
        strength >= 0.7f -> {
            label = "Strong"
            color = MaterialTheme.colorScheme.primary
        }

        strength >= 0.4f -> {
            label = "Medium"
            color = MaterialTheme.colorScheme.tertiary
        }

        else -> {
            label = "Weak"
            color = MaterialTheme.colorScheme.error
        }
    }

    return PasswordStrength(label, strength.coerceIn(0f, 1f), color)
}

data class PasswordStrength(
    val label: String,
    val strength: Float,
    val color: androidx.compose.ui.graphics.Color
)

sealed class UsernameStatus {
    object Idle : UsernameStatus()
    object TooShort : UsernameStatus()
    object TooLong : UsernameStatus()
    object InvalidFormat : UsernameStatus()
    object ValidFormat : UsernameStatus()
}