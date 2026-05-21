package com.example.booknest.ui.onboarding

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.service.AuthService
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.components.BackgroundDecoration
import com.example.booknest.ui.components.models.UsernameStatus
import com.example.booknest.ui.onboarding.components.dialogs.DatePickerDialog
import com.example.booknest.ui.onboarding.components.personalinfo.PersonalInfoBirthDateField
import com.example.booknest.ui.onboarding.components.personalinfo.PersonalInfoContinueButton
import com.example.booknest.ui.onboarding.components.personalinfo.PersonalInfoEmailField
import com.example.booknest.ui.onboarding.components.personalinfo.PersonalInfoNameFields
import com.example.booknest.ui.onboarding.components.personalinfo.PersonalInfoPasswordFields
import com.example.booknest.ui.onboarding.components.personalinfo.PersonalInfoSignupProgressBar
import com.example.booknest.ui.onboarding.components.personalinfo.PersonalInfoSignupTitle
import com.example.booknest.ui.onboarding.components.personalinfo.PersonalInfoUsernameField
import com.example.booknest.viewmodel.auth.SignupViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat =
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                it
            }
        } ?: ""
    }

    val isEmailValid = remember(email) {
        email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
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
        if (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() && email.length <= 255
        ) {
            isCheckingEmail = true
            delay(500)

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

    val isBirthDateValid = remember(birthDate) {
        birthDate?.let {
            val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")
            if (!datePattern.matches(it)) {
                return@remember false
            }
            try {
                val dateFormat =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val birthDateObj = dateFormat.parse(it)
                val today = Calendar.getInstance()
                val birthCalendar = Calendar.getInstance()
                birthDateObj?.let { birthCalendar.time = it }

                var age =
                    today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                val monthDiff =
                    today.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)
                if (monthDiff < 0 || (monthDiff == 0 && today.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(
                        Calendar.DAY_OF_MONTH
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
        BackgroundDecoration(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PersonalInfoSignupProgressBar()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PersonalInfoSignupTitle()

                PersonalInfoNameFields(
                    firstName = firstName,
                    onFirstNameChange = { if (it.length <= 100) firstName = it },
                    isFirstNameValid = isFirstNameValid,
                    lastName = lastName,
                    onLastNameChange = { if (it.length <= 100) lastName = it },
                    isLastNameValid = isLastNameValid
                )

                PersonalInfoEmailField(
                    email = email,
                    onEmailChange = { if (it.length <= 255) email = it },
                    isEmailValid = isEmailValid,
                    isCheckingEmail = isCheckingEmail,
                    emailAvailable = emailAvailable
                )

                PersonalInfoUsernameField(
                    username = username,
                    onUsernameChange = { username = it },
                    isCheckingUsername = isCheckingUsername,
                    usernameStatus = usernameStatus,
                    usernameAvailable = usernameAvailable
                )

                PersonalInfoPasswordFields(
                    password = password,
                    onPasswordChange = { if (it.length <= 128) password = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibleChange = { passwordVisible = it },
                    isPasswordValid = isPasswordValid,
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    confirmPasswordVisible = confirmPasswordVisible,
                    onConfirmPasswordVisibleChange = { confirmPasswordVisible = it }
                )

                PersonalInfoBirthDateField(
                    formattedDate = formattedDate,
                    birthDate = birthDate,
                    isBirthDateValid = isBirthDateValid,
                    firstName = firstName,
                    onOpenDatePicker = { showDatePicker = true }
                )

                PersonalInfoContinueButton(
                    isFormValid = isFormValid,
                    onContinue = {
                        viewModel.updatePersonalInfo(firstName, lastName, email, password)
                        viewModel.updateUsername(username)
                        viewModel.updateBirthDate(birthDate)
                        navController.navigate(Screen.ProfileDetails.route)
                    }
                )
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
