package com.example.booknest.ui.author

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.GenresRepository
import com.example.booknest.viewmodel.AuthorViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

enum class AgeRating(val value: String, val displayName: String) {
    ALL("all", "All Ages"),
    THIRTEEN_PLUS("13+", "13+"),
    SIXTEEN_PLUS("16+", "16+"),
    EIGHTEEN_PLUS("18+", "18+")
}

enum class DistributionType(val value: String, val displayName: String) {
    DIGITAL("digital", "Digital"),
    PHYSICAL("physical", "Physical"),
    BOTH("both", "Both")
}

enum class SelectionMethod(val value: String, val displayName: String) {
    AUTHOR_SELECTS("author_selects", "Author Selects"),
    FIRST_COME("first_come", "First Come First Served"),
    RANDOM("lottery", "Random Selection")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCreationWizard(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    genresRepository: GenresRepository = koinInject(),
    authorViewModel: AuthorViewModel = getViewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 6

    var title by remember { mutableStateOf("") }
    var shortDescription by remember { mutableStateOf("") }
    var fullDescription by remember { mutableStateOf("") }
    var pageCount by remember { mutableStateOf("") }
    var selectedAgeRating by remember { mutableStateOf<AgeRating?>(null) }
    var selectedDistributionType by remember { mutableStateOf<DistributionType?>(null) }
    var totalCopies by remember { mutableStateOf("") }
    var applicationDeadline by remember { mutableStateOf<String?>(null) }
    var reviewDeadline by remember { mutableStateOf<String?>(null) }
    var showApplicationDatePicker by remember { mutableStateOf(false) }
    var showReviewDatePicker by remember { mutableStateOf(false) }

    val tomorrowMillis = remember {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }

    val applicationDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = applicationDeadline?.let {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time
            } catch (e: Exception) {
                null
            }
        },
        initialDisplayedMonthMillis = tomorrowMillis
    )
    val reviewDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = reviewDeadline?.let {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time
            } catch (e: Exception) {
                null
            }
        }
    )
    var selectedSelectionMethod by remember { mutableStateOf<SelectionMethod?>(null) }
    var selectionCriteria by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedSeries by remember { mutableStateOf<SeriesResponse?>(null) }
    var seriesOrder by remember { mutableStateOf("") }

    var coverImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var bookFileUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var bookFileName by remember { mutableStateOf<String?>(null) }
    var bookFileSize by remember { mutableStateOf<Long?>(null) }
    var coverImageUrl by remember { mutableStateOf<String?>(null) }

    var isCreating by remember { mutableStateOf(false) }
    var isUploadingFile by remember { mutableStateOf(false) }
    var showCreateSeriesDialog by remember { mutableStateOf(false) }
    var creationError by remember { mutableStateOf<String?>(null) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var createdBookId by remember { mutableStateOf<String?>(null) }
    var shouldPublishAfterCreation by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var shortDescriptionError by remember { mutableStateOf<String?>(null) }
    var fullDescriptionError by remember { mutableStateOf<String?>(null) }
    var pageCountError by remember { mutableStateOf<String?>(null) }
    var totalCopiesError by remember { mutableStateOf<String?>(null) }
    var seriesOrderError by remember { mutableStateOf<String?>(null) }
    var applicationDeadlineError by remember { mutableStateOf<String?>(null) }
    var reviewDeadlineError by remember { mutableStateOf<String?>(null) }

    val mySeries by authorViewModel.mySeries.collectAsState()
    val bookCreationState by authorViewModel.bookCreationState.collectAsState()
    var genres by remember { mutableStateOf(listOf<GenreResponse>()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authorViewModel.loadMySeries()
        try {
            val result = genresRepository.getGenres()
            result
                .onSuccess { genreList ->
                    genres = genreList
                    println("Genres loaded successfully: ${genreList.size} genres")
                }
                .onFailure { e ->
                    com.example.booknest.ui.error.GlobalErrorHandler.showError(e)
                    genres = emptyList()
                }
        } catch (e: Exception) {
            com.example.booknest.ui.error.GlobalErrorHandler.showError(e)
            genres = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Book (Step $currentStep of $totalSteps)",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(totalSteps) { step ->
                        val stepNumber = step + 1
                        val isCompleted = stepNumber < currentStep
                        val isCurrent = stepNumber == currentStep
                        val canJumpTo = isCompleted || isCurrent

                        Card(
                            modifier = Modifier
                                .width(60.dp)
                                .height(40.dp)
                                .then(
                                    if (canJumpTo) {
                                        Modifier.clickable {
                                            if (isCompleted) {
                                                currentStep = stepNumber
                                            }
                                        }
                                    } else {
                                        Modifier
                                    }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isCompleted -> MaterialTheme.colorScheme.primary
                                    isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Completed",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(
                                        text = stepNumber.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            when (currentStep) {
                1 -> {
                    item {
                        BasicInfoStep(
                            title = title,
                            shortDescription = shortDescription,
                            fullDescription = fullDescription,
                            pageCount = pageCount,
                            coverImageUri = coverImageUri,
                            coverImageUrl = coverImageUrl,
                            titleError = titleError,
                            shortDescriptionError = shortDescriptionError,
                            fullDescriptionError = fullDescriptionError,
                            pageCountError = pageCountError,
                            onUpdate = { t, sd, fd, pc, uri, url ->
                                title = t
                                shortDescription = sd
                                fullDescription = fd
                                pageCount = pc
                                coverImageUri = uri
                                coverImageUrl = url
                            },
                            onValidationChange = { tErr, sdErr, fdErr, pcErr ->
                                titleError = tErr
                                shortDescriptionError = sdErr
                                fullDescriptionError = fdErr
                                pageCountError = pcErr
                            }
                        )
                    }
                }

                2 -> {
                    item {
                        GenresAndSeriesStep(
                            selectedGenres = selectedGenres,
                            selectedSeries = selectedSeries,
                            seriesOrder = seriesOrder,
                            mySeries = mySeries,
                            genres = genres,
                            seriesOrderError = seriesOrderError,
                            onUpdate = { sg, ss, so ->
                                selectedGenres = sg
                                selectedSeries = ss
                                seriesOrder = so
                            },
                            onCreateSeries = { name: String, description: String ->
                                authorViewModel.createSeries(
                                    com.example.booknest.domain.model.request.CreateSeriesRequest(
                                        name = name,
                                        description = description.ifBlank { null }
                                    )
                                )
                                showCreateSeriesDialog = false
                            },
                            onShowCreateSeriesDialog = { showCreateSeriesDialog = true },
                            showCreateSeriesDialog = showCreateSeriesDialog,
                            onDismissCreateSeriesDialog = { showCreateSeriesDialog = false },
                            onValidationChange = { err ->
                                seriesOrderError = err
                            }
                        )
                    }
                }

                3 -> {
                    item {
                        DistributionStep(
                            selectedAgeRating = selectedAgeRating,
                            selectedDistributionType = selectedDistributionType,
                            totalCopies = totalCopies,
                            totalCopiesError = totalCopiesError,
                            onUpdate = { ar, dt, tc ->
                                selectedAgeRating = ar
                                selectedDistributionType = dt
                                totalCopies = tc
                            },
                            onValidationChange = { err ->
                                totalCopiesError = err
                            }
                        )
                    }
                }

                4 -> {
                    item {
                        ReviewConfigStep(
                            applicationDeadline = applicationDeadline,
                            reviewDeadline = reviewDeadline,
                            selectedSelectionMethod = selectedSelectionMethod,
                            selectionCriteria = selectionCriteria,
                            showApplicationDatePicker = showApplicationDatePicker,
                            showReviewDatePicker = showReviewDatePicker,
                            applicationDatePickerState = applicationDatePickerState,
                            reviewDatePickerState = reviewDatePickerState,
                            applicationDeadlineError = applicationDeadlineError,
                            reviewDeadlineError = reviewDeadlineError,
                            onUpdate = { ad, rd, ssm, sc ->
                                applicationDeadline = ad
                                reviewDeadline = rd
                                selectedSelectionMethod = ssm
                                selectionCriteria = sc
                                val (appErr, revErr) = validateDeadlines(ad, rd)
                                applicationDeadlineError = appErr
                                reviewDeadlineError = revErr
                            },
                            onShowApplicationDatePicker = { showApplicationDatePicker = true },
                            onShowReviewDatePicker = { showReviewDatePicker = true },
                            onDismissApplicationDatePicker = { showApplicationDatePicker = false },
                            onDismissReviewDatePicker = { showReviewDatePicker = false },
                            onValidationChange = { appErr, revErr ->
                                applicationDeadlineError = appErr
                                reviewDeadlineError = revErr
                            }
                        )
                    }
                }

                5 -> {
                    item {
                        FileUploadStep(
                            bookFileUri = bookFileUri,
                            bookFileName = bookFileName,
                            bookFileSize = bookFileSize,
                            distributionType = selectedDistributionType,
                            onFileSelected = { uri, name, size ->
                                bookFileUri = uri
                                bookFileName = name
                                bookFileSize = size
                            }
                        )
                    }
                }

                6 -> {
                    item {
                        PreviewStep(
                            title = title,
                            shortDescription = shortDescription,
                            fullDescription = fullDescription,
                            pageCount = pageCount,
                            ageRating = selectedAgeRating,
                            distributionType = selectedDistributionType,
                            totalCopies = totalCopies,
                            genres = selectedGenres,
                            genreList = genres,
                            series = selectedSeries,
                            seriesOrder = seriesOrder,
                            applicationDeadline = applicationDeadline,
                            reviewDeadline = reviewDeadline,
                            selectionMethod = selectedSelectionMethod,
                            selectionCriteria = selectionCriteria,
                            hasCoverImage = coverImageUri != null || !coverImageUrl.isNullOrBlank(),
                            hasBookFile = bookFileUri != null
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep < totalSteps) {
                        OutlinedButton(
                            onClick = { if (currentStep > 1) currentStep-- },
                            enabled = currentStep > 1,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Previous",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = { currentStep++ },
                            enabled = isStepValid(
                                currentStep,
                                title,
                                selectedAgeRating,
                                selectedDistributionType,
                                applicationDeadline,
                                selectedSelectionMethod,
                                bookFileUri,
                                selectedDistributionType
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                "Next",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Filled.ArrowForward,
                                contentDescription = "Next",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                isCreating = true
                                creationError = null
                                shouldPublishAfterCreation = false
                                val book = CreateBookRequest(
                                    title = title,
                                    shortDescription = shortDescription.ifBlank { null },
                                    fullDescription = fullDescription.ifBlank { null },
                                    pageCount = pageCount.toIntOrNull(),
                                    ageRating = selectedAgeRating!!.value,
                                    distributionType = selectedDistributionType!!.value,
                                    totalCopies = totalCopies.toIntOrNull() ?: 1,
                                    applicationDeadline = applicationDeadline!!,
                                    reviewDeadline = reviewDeadline,
                                    selectionMethod = (selectedSelectionMethod
                                        ?: SelectionMethod.AUTHOR_SELECTS).value,
                                    selectionCriteria = selectionCriteria.ifBlank { null },
                                    genreIds = selectedGenres.ifEmpty { null },
                                    seriesId = selectedSeries?.id,
                                    seriesOrder = seriesOrder.toIntOrNull(),
                                    coverImageUrl = coverImageUrl
                                )
                                authorViewModel.createBook(
                                    book,
                                    bookFileUri,
                                    coverImageUri,
                                    context
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isCreating && !isUploadingFile && isStepValid(
                                currentStep,
                                title,
                                selectedAgeRating,
                                selectedDistributionType,
                                applicationDeadline,
                                selectedSelectionMethod,
                                bookFileUri,
                                selectedDistributionType,
                                titleError,
                                applicationDeadlineError
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                "Save as Draft",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                isCreating = true
                                creationError = null
                                shouldPublishAfterCreation = true
                                val book = CreateBookRequest(
                                    title = title,
                                    shortDescription = shortDescription.ifBlank { null },
                                    fullDescription = fullDescription.ifBlank { null },
                                    pageCount = pageCount.toIntOrNull(),
                                    ageRating = selectedAgeRating!!.value,
                                    distributionType = selectedDistributionType!!.value,
                                    totalCopies = totalCopies.toIntOrNull() ?: 1,
                                    applicationDeadline = applicationDeadline!!,
                                    reviewDeadline = reviewDeadline,
                                    selectionMethod = (selectedSelectionMethod
                                        ?: SelectionMethod.AUTHOR_SELECTS).value,
                                    selectionCriteria = selectionCriteria.ifBlank { null },
                                    genreIds = selectedGenres.ifEmpty { null },
                                    seriesId = selectedSeries?.id,
                                    seriesOrder = seriesOrder.toIntOrNull(),
                                    coverImageUrl = coverImageUrl
                                )
                                authorViewModel.createBook(
                                    book,
                                    bookFileUri,
                                    coverImageUri,
                                    context
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isCreating && !isUploadingFile && isStepValid(
                                currentStep,
                                title,
                                selectedAgeRating,
                                selectedDistributionType,
                                applicationDeadline,
                                selectedSelectionMethod,
                                bookFileUri,
                                selectedDistributionType,
                                titleError,
                                applicationDeadlineError
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isCreating || isUploadingFile) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                if (isUploadingFile) "Uploading..." else "Create Book",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            creationError?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(bookCreationState) {
        when (val state = bookCreationState) {
            is AuthorViewModel.BookCreationState.Success -> {
                isCreating = false
                if (bookFileUri != null) {
                    isUploadingFile = true
                    authorViewModel.uploadBookFile(
                        bookId = state.book.id,
                        fileUri = bookFileUri!!,
                        context = context,
                        onSuccess = {
                            isUploadingFile = false
                            createdBookId = state.book.id
                            if (shouldPublishAfterCreation) {
                                showPublishDialog = true
                            } else {
                                authorViewModel.clearBookCreationState()
                                navController.popBackStack()
                            }
                        },
                        onError = { error ->
                            scope.launch {
                                isUploadingFile = false
                                creationError = "Book created but file upload failed: $error"
                                kotlinx.coroutines.delay(2000)
                                authorViewModel.clearBookCreationState()
                                navController.popBackStack()
                            }
                        }
                    )
                } else {
                    createdBookId = state.book.id
                    if (shouldPublishAfterCreation) {
                        showPublishDialog = true
                    } else {
                        authorViewModel.clearBookCreationState()
                        navController.popBackStack()
                    }
                }
            }

            is AuthorViewModel.BookCreationState.Error -> {
                isCreating = false
                creationError = state.message
            }

            else -> {}
        }
    }

    if (showApplicationDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = Date(it)
                    val newDeadline = dateFormat.format(selectedDate)

                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val today = calendar.time

                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.time = selectedDate
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0)
                    selectedCalendar.set(Calendar.MINUTE, 0)
                    selectedCalendar.set(Calendar.SECOND, 0)
                    selectedCalendar.set(Calendar.MILLISECOND, 0)

                    if (selectedCalendar.timeInMillis <= calendar.timeInMillis) {
                        applicationDeadlineError = "Application deadline must be at least tomorrow"
                    } else {
                        applicationDeadline = newDeadline
                        val (appErr, revErr) = validateDeadlines(newDeadline, reviewDeadline)
                        applicationDeadlineError = appErr
                        reviewDeadlineError = revErr
                    }
                }
                showApplicationDatePicker = false
            },
            onDismiss = { showApplicationDatePicker = false },
            datePickerState = applicationDatePickerState
        )
    }

    if (showReviewDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val newDeadline = dateFormat.format(Date(it))
                    reviewDeadline = newDeadline
                    val (appErr, revErr) = validateDeadlines(applicationDeadline, newDeadline)
                    applicationDeadlineError = appErr
                    reviewDeadlineError = revErr
                }
                showReviewDatePicker = false
            },
            onDismiss = { showReviewDatePicker = false },
            datePickerState = reviewDatePickerState
        )
    }

    if (showPublishDialog && createdBookId != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { },
            title = { Text("Book Created Successfully!") },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            text = {
                Text("Would you like to publish your book now or keep it as a draft?")
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showPublishDialog = false
                            authorViewModel.clearBookCreationState()
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Keep Draft")
                    }
                    Button(
                        onClick = {
                            authorViewModel.publishBook(createdBookId!!)
                            showPublishDialog = false
                            authorViewModel.clearBookCreationState()
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Publish Now")
                    }
                }
            },
            dismissButton = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
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
fun BasicInfoStep(
    title: String,
    shortDescription: String,
    fullDescription: String,
    pageCount: String,
    coverImageUri: android.net.Uri?,
    coverImageUrl: String?,
    titleError: String? = null,
    shortDescriptionError: String? = null,
    fullDescriptionError: String? = null,
    pageCountError: String? = null,
    onUpdate: (String, String, String, String, android.net.Uri?, String?) -> Unit,
    onValidationChange: ((String?, String?, String?, String?) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = title,
            onValueChange = {
                onUpdate(
                    it,
                    shortDescription,
                    fullDescription,
                    pageCount,
                    coverImageUri,
                    coverImageUrl
                )
                onValidationChange?.invoke(
                    validateTitle(it),
                    validateShortDescription(shortDescription),
                    validateFullDescription(fullDescription),
                    validatePageCount(pageCount)
                )
            },
            label = { Text("Title *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = titleError != null,
            supportingText = titleError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                Text("${title.length}/255 characters")
            }
        )

        OutlinedTextField(
            value = shortDescription,
            onValueChange = {
                onUpdate(title, it, fullDescription, pageCount, coverImageUri, coverImageUrl)
                onValidationChange?.invoke(
                    validateTitle(title),
                    validateShortDescription(it),
                    validateFullDescription(fullDescription),
                    validatePageCount(pageCount)
                )
            },
            label = { Text("Short Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            isError = shortDescriptionError != null,
            supportingText = shortDescriptionError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                Text("${shortDescription.length}/500 characters (optional)")
            }
        )

        OutlinedTextField(
            value = fullDescription,
            onValueChange = {
                onUpdate(title, shortDescription, it, pageCount, coverImageUri, coverImageUrl)
                onValidationChange?.invoke(
                    validateTitle(title),
                    validateShortDescription(shortDescription),
                    validateFullDescription(it),
                    validatePageCount(pageCount)
                )
            },
            label = { Text("Full Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 10,
            isError = fullDescriptionError != null,
            supportingText = fullDescriptionError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                Text("${fullDescription.length}/10,000 characters (optional)")
            }
        )

        OutlinedTextField(
            value = pageCount,
            onValueChange = {
                onUpdate(title, shortDescription, fullDescription, it, coverImageUri, coverImageUrl)
                onValidationChange?.invoke(
                    validateTitle(title),
                    validateShortDescription(shortDescription),
                    validateFullDescription(fullDescription),
                    validatePageCount(it)
                )
            },
            label = { Text("Page Count") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = pageCountError != null,
            supportingText = pageCountError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                Text("Optional: 1-100,000 pages")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Book Cover Image",
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Info",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Image Guidelines",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "• Recommended dimensions: 1200x1800px (2:3 aspect ratio)\n• Maximum file size: 10MB\n• Supported formats: JPG, PNG, GIF, WEBP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        CoverImagePicker(
            imageUri = coverImageUri,
            imageUrl = coverImageUrl,
            onImageSelected = { uri: Uri?, url: String? ->
                onUpdate(title, shortDescription, fullDescription, pageCount, uri, url)
            }
        )
    }
}

@Composable
fun GenresAndSeriesStep(
    selectedGenres: List<Int>,
    selectedSeries: SeriesResponse?,
    seriesOrder: String,
    mySeries: List<SeriesResponse>,
    genres: List<GenreResponse>,
    seriesOrderError: String? = null,
    onUpdate: (List<Int>, SeriesResponse?, String) -> Unit,
    onCreateSeries: (String, String) -> Unit,
    onShowCreateSeriesDialog: () -> Unit,
    showCreateSeriesDialog: Boolean,
    onDismissCreateSeriesDialog: () -> Unit,
    onValidationChange: ((String?) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Genres & Series",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Select Genres",
            style = MaterialTheme.typography.titleMedium
        )

        val sortedGenres = remember(genres, selectedGenres) {
            genres.sortedBy { genre ->
                if (selectedGenres.contains(genre.id)) 0 else 1
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedGenres) { genre ->
                val isSelected = selectedGenres.contains(genre.id)
                Card(
                    modifier = Modifier
                        .clickable {
                            val newSelection = if (isSelected) {
                                selectedGenres - genre.id
                            } else {
                                selectedGenres + genre.id
                            }
                            onUpdate(newSelection, selectedSeries, seriesOrder)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = genre.name,
                        modifier = Modifier.padding(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = "Select Series (Optional)",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Choose an existing series or create a new one:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onShowCreateSeriesDialog,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Create New Series",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Series")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selectedSeries == null,
                    onClick = { onUpdate(selectedGenres, null, seriesOrder) },
                    role = Role.RadioButton
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedSeries == null,
                onClick = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "No Series",
                style = MaterialTheme.typography.titleSmall
            )
        }

        if (mySeries.isNotEmpty()) {
            Text(
                text = "Existing Series:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Column(modifier = Modifier.selectableGroup()) {
                mySeries.forEach { series ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedSeries?.id == series.id,
                                onClick = { onUpdate(selectedGenres, series, seriesOrder) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSeries?.id == series.id,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = series.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                            series.description?.let { desc ->
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedSeries != null) {
            OutlinedTextField(
                value = seriesOrder,
                onValueChange = {
                    onUpdate(selectedGenres, selectedSeries, it)
                    onValidationChange?.invoke(validateSeriesOrder(it))
                },
                label = { Text("Order in Series") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = seriesOrderError != null,
                supportingText = seriesOrderError?.let {
                    {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } ?: {
                    Text("Optional: Minimum 1")
                }
            )
        }
    }

    if (showCreateSeriesDialog) {
        BookCreationCreateSeriesDialog(
            onDismiss = onDismissCreateSeriesDialog,
            onCreateSeries = { name: String, description: String ->
                onCreateSeries(name, description)
                onDismissCreateSeriesDialog()
            }
        )
    }
}

@Composable
fun DistributionStep(
    selectedAgeRating: AgeRating?,
    selectedDistributionType: DistributionType?,
    totalCopies: String,
    totalCopiesError: String? = null,
    onUpdate: (AgeRating?, DistributionType?, String) -> Unit,
    onValidationChange: ((String?) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Distribution Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Age Rating *",
            style = MaterialTheme.typography.titleMedium
        )

        Column(modifier = Modifier.selectableGroup()) {
            AgeRating.values().forEach { rating ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedAgeRating == rating,
                            onClick = { onUpdate(rating, selectedDistributionType, totalCopies) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedAgeRating == rating,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rating.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }

        Text(
            text = "Distribution Type *",
            style = MaterialTheme.typography.titleMedium
        )

        Column(modifier = Modifier.selectableGroup()) {
            DistributionType.values().forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedDistributionType == type,
                            onClick = { onUpdate(selectedAgeRating, type, totalCopies) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedDistributionType == type,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = type.name.lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }

        OutlinedTextField(
            value = totalCopies,
            onValueChange = {
                onUpdate(selectedAgeRating, selectedDistributionType, it)
                onValidationChange?.invoke(validateTotalCopies(it))
            },
            label = { Text("Total Copies") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = totalCopiesError != null,
            supportingText = totalCopiesError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                Text("Optional: Minimum 1 copy")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewConfigStep(
    applicationDeadline: String?,
    reviewDeadline: String?,
    selectedSelectionMethod: SelectionMethod?,
    selectionCriteria: String,
    showApplicationDatePicker: Boolean,
    showReviewDatePicker: Boolean,
    applicationDatePickerState: DatePickerState,
    reviewDatePickerState: DatePickerState,
    applicationDeadlineError: String? = null,
    reviewDeadlineError: String? = null,
    onUpdate: (String?, String?, SelectionMethod?, String) -> Unit,
    onShowApplicationDatePicker: () -> Unit,
    onShowReviewDatePicker: () -> Unit,
    onDismissApplicationDatePicker: () -> Unit,
    onDismissReviewDatePicker: () -> Unit,
    onValidationChange: ((String?, String?) -> Unit)? = null
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val inputDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val formattedApplicationDeadline = remember(applicationDeadline) {
        applicationDeadline?.let {
            try {
                val date = inputDateFormat.parse(it)
                date?.let { dateFormat.format(it) } ?: it
            } catch (e: Exception) {
                it
            }
        } ?: ""
    }

    val formattedReviewDeadline = remember(reviewDeadline) {
        reviewDeadline?.let {
            try {
                val date = inputDateFormat.parse(it)
                date?.let { dateFormat.format(it) } ?: it
            } catch (e: Exception) {
                it
            }
        } ?: ""
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Campaign Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = formattedApplicationDeadline,
            onValueChange = { },
            label = { Text("Application Deadline *") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowApplicationDatePicker() },
            readOnly = true,
            singleLine = true,
            placeholder = { Text("Select date") },
            trailingIcon = {
                IconButton(onClick = onShowApplicationDatePicker) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Select Date"
                    )
                }
            },
            isError = applicationDeadlineError != null || applicationDeadline == null,
            supportingText = applicationDeadlineError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                if (applicationDeadline == null) {
                    Text(
                        "Application deadline is required",
                        color = MaterialTheme.colorScheme.error
                    )
                } else null
            }
        )

        OutlinedTextField(
            value = formattedReviewDeadline,
            onValueChange = { },
            label = { Text("Review Deadline") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowReviewDatePicker() },
            readOnly = true,
            singleLine = true,
            placeholder = { Text("Select date (optional)") },
            trailingIcon = {
                IconButton(onClick = onShowReviewDatePicker) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Select Date"
                    )
                }
            },
            isError = reviewDeadlineError != null,
            supportingText = reviewDeadlineError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Text(
            text = "Selection Method *",
            style = MaterialTheme.typography.titleMedium
        )

        Column(modifier = Modifier.selectableGroup()) {
            SelectionMethod.values().forEach { method ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedSelectionMethod == method,
                            onClick = {
                                onUpdate(
                                    applicationDeadline,
                                    reviewDeadline,
                                    method,
                                    selectionCriteria
                                )
                            },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedSelectionMethod == method)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSelectionMethod == method,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = method.displayName,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = when (method) {
                                    SelectionMethod.AUTHOR_SELECTS -> "You manually review and select reviewers from applications"
                                    SelectionMethod.FIRST_COME -> "Applications are automatically approved on a first-come, first-served basis"
                                    SelectionMethod.RANDOM -> "Reviewers are randomly selected via lottery after the application deadline"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = selectionCriteria,
            onValueChange = {
                onUpdate(
                    applicationDeadline,
                    reviewDeadline,
                    selectedSelectionMethod,
                    it
                )
            },
            label = { Text("Selection Criteria") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            placeholder = { Text("Describe what you're looking for in reviewers (optional)") },
            supportingText = {
                Text(
                    text = "Help reviewers understand what you're looking for",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

@Composable
fun PreviewStep(
    title: String,
    shortDescription: String,
    fullDescription: String,
    pageCount: String,
    ageRating: AgeRating?,
    distributionType: DistributionType?,
    totalCopies: String,
    genres: List<Int>,
    genreList: List<GenreResponse>,
    series: SeriesResponse?,
    seriesOrder: String,
    applicationDeadline: String?,
    reviewDeadline: String?,
    selectionMethod: SelectionMethod?,
    selectionCriteria: String,
    hasCoverImage: Boolean,
    hasBookFile: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val inputDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val formattedApplicationDeadline = remember(applicationDeadline) {
        applicationDeadline?.let {
            try {
                val date = inputDateFormat.parse(it)
                date?.let { dateFormat.format(it) } ?: it
            } catch (e: Exception) {
                it
            }
        } ?: "Not set"
    }

    val formattedReviewDeadline = remember(reviewDeadline) {
        reviewDeadline?.let {
            try {
                val date = inputDateFormat.parse(it)
                date?.let { dateFormat.format(it) } ?: it
            } catch (e: Exception) {
                it
            }
        } ?: "Not set"
    }

    val selectedGenreNames = remember(genres, genreList) {
        genres.mapNotNull { genreId ->
            genreList.find { it.id == genreId }?.name
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Preview & Publish",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Required Items Checklist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ChecklistItem(
                    label = "Title",
                    isComplete = title.isNotBlank()
                )
                ChecklistItem(
                    label = "Age Rating",
                    isComplete = ageRating != null
                )
                ChecklistItem(
                    label = "Distribution Type",
                    isComplete = distributionType != null
                )
                ChecklistItem(
                    label = "Application Deadline",
                    isComplete = applicationDeadline != null
                )
                if (distributionType == DistributionType.DIGITAL || distributionType == DistributionType.BOTH) {
                    ChecklistItem(
                        label = "Book File (Required for Digital)",
                        isComplete = hasBookFile
                    )
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
                    text = "Book Preview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                Text(
                    text = title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                if (shortDescription.isNotBlank()) {
                    Text(
                        text = shortDescription,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (fullDescription.isNotBlank()) {
                    Text(
                        text = "Full Description:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = fullDescription,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Divider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (pageCount.isNotBlank()) {
                        PreviewDetail("Page Count", pageCount)
                    }

                    ageRating?.let { rating ->
                        PreviewDetail("Age Rating", rating.displayName)
                    }

                    distributionType?.let { type ->
                        PreviewDetail("Distribution Type", type.displayName)
                    }

                    if (totalCopies.isNotBlank()) {
                        PreviewDetail("Total Copies", totalCopies)
                    }

                    if (selectedGenreNames.isNotEmpty()) {
                        PreviewDetail("Genres", selectedGenreNames.joinToString(", "))
                    }

                    series?.let { s ->
                        val seriesText = if (seriesOrder.isNotBlank()) {
                            "${s.name} (#$seriesOrder)"
                        } else {
                            s.name
                        }
                        PreviewDetail("Series", seriesText)
                    }

                    PreviewDetail("Application Deadline", formattedApplicationDeadline)

                    if (reviewDeadline != null) {
                        PreviewDetail("Review Deadline", formattedReviewDeadline)
                    }

                    selectionMethod?.let { method ->
                        PreviewDetail("Selection Method", method.displayName)
                    }

                    if (selectionCriteria.isNotBlank()) {
                        PreviewDetail("Selection Criteria", selectionCriteria)
                    }

                    PreviewDetail("Cover Image", if (hasCoverImage) "Uploaded" else "Not uploaded")

                    if (distributionType == DistributionType.DIGITAL || distributionType == DistributionType.BOTH) {
                        PreviewDetail(
                            "Book File",
                            if (hasBookFile) "Uploaded" else "Not uploaded (Required)"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistItem(label: String, isComplete: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            if (isComplete) Icons.Filled.Check else Icons.Filled.Info,
            contentDescription = if (isComplete) "Complete" else "Incomplete",
            modifier = Modifier.size(20.dp),
            tint = if (isComplete)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isComplete)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun PreviewDetail(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BookCreationCreateSeriesDialog(
    onDismiss: () -> Unit,
    onCreateSeries: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Series") },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Series Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateSeries(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CoverImagePicker(
    imageUri: Uri?,
    imageUrl: String?,
    onImageSelected: (Uri?, String?) -> Unit
) {
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onImageSelected(it, null)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = !isUploading) {
                        imagePickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    isUploading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                    }

                    imageUri != null -> {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Cover Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    !imageUrl.isNullOrBlank() -> {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Cover Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Add Cover Image",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap to select cover image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                    enabled = !isUploading
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Select Image",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (imageUri != null || !imageUrl.isNullOrBlank()) "Change" else "Select Image")
                }

                if (imageUri != null || !imageUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            onImageSelected(null, null)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isUploading
                    ) {
                        Icon(
                            Icons.Filled.Delete,
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
}

@Composable
fun FileUploadStep(
    bookFileUri: android.net.Uri?,
    bookFileName: String?,
    bookFileSize: Long?,
    distributionType: DistributionType?,
    onFileSelected: (android.net.Uri?, String?, Long?) -> Unit
) {
    val context = LocalContext.current
    var fileError by remember { mutableStateOf<String?>(null) }

    val isRequired =
        distributionType == DistributionType.DIGITAL || distributionType == DistributionType.BOTH
    val maxFileSize = 50 * 1024 * 1024L

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    it.moveToFirst()
                    val fileName = it.getString(nameIndex) ?: "Unknown file"
                    val fileSize = it.getLong(sizeIndex)

                    val fileExtension = fileName.substringAfterLast('.', "").lowercase()
                    if (fileExtension !in listOf("pdf", "epub")) {
                        fileError = "Only PDF and EPUB files are supported"
                        onFileSelected(null, null, null)
                        return@let
                    }

                    if (fileSize > maxFileSize) {
                        fileError = "File size exceeds maximum of 50MB"
                        onFileSelected(null, null, null)
                        return@let
                    }

                    fileError = null
                    onFileSelected(uri, fileName, fileSize)
                } ?: run {
                    fileError = "Could not read file information"
                    onFileSelected(null, null, null)
                }
            } catch (e: Exception) {
                fileError = "Error reading file: ${e.message}"
                onFileSelected(null, null, null)
            }
        } ?: run {
            onFileSelected(null, null, null)
        }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isRequired) "Book File Upload *" else "Book File Upload",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (isRequired) {
                Text(
                    text = "Required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Info",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "File Requirements",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "• Supported formats: PDF, EPUB only\n• Maximum file size: ${
                        formatFileSize(
                            maxFileSize
                        )
                    }\n• File will be validated before upload",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (fileError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = fileError!!,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (bookFileUri != null && bookFileName != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "File Selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = bookFileName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        bookFileSize?.let { size ->
                            Text(
                                text = "Size: ${formatFileSize(size)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = {
                        onFileSelected(null, null, null)
                        fileError = null
                    }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Remove File",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    filePickerLauncher.launch("*/*")
                    fileError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Select File",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Select Book File",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

internal fun validateTitle(title: String): String? {
    return when {
        title.isBlank() -> "Title is required"
        title.length > 255 -> "Title must be 255 characters or less"
        else -> null
    }
}

internal fun validateShortDescription(shortDescription: String): String? {
    return if (shortDescription.length > 500) {
        "Short description must be 500 characters or less"
    } else null
}

internal fun validateFullDescription(fullDescription: String): String? {
    return if (fullDescription.length > 10000) {
        "Full description must be 10,000 characters or less"
    } else null
}

internal fun validatePageCount(pageCount: String): String? {
    if (pageCount.isBlank()) return null
    val count = pageCount.toIntOrNull()
    return when {
        count == null -> "Page count must be a number"
        count < 1 -> "Page count must be at least 1"
        count > 100000 -> "Page count must be 100,000 or less"
        else -> null
    }
}

internal fun validateTotalCopies(totalCopies: String): String? {
    if (totalCopies.isBlank()) return null
    val copies = totalCopies.toIntOrNull()
    return when {
        copies == null -> "Total copies must be a number"
        copies < 1 -> "Total copies must be at least 1"
        else -> null
    }
}

internal fun validateSeriesOrder(seriesOrder: String): String? {
    if (seriesOrder.isBlank()) return null
    val order = seriesOrder.toIntOrNull()
    return when {
        order == null -> "Series order must be a number"
        order < 1 -> "Series order must be at least 1"
        else -> null
    }
}

internal fun validateDeadlines(
    applicationDeadline: String?,
    reviewDeadline: String?
): Pair<String?, String?> {
    var appError: String? = null
    var reviewError: String? = null

    if (applicationDeadline == null) {
        appError = "Application deadline is required"
    } else {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val appDate = dateFormat.parse(applicationDeadline)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            if (appDate != null) {
                val appCalendar = Calendar.getInstance().apply {
                    time = appDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val todayCalendar = Calendar.getInstance().apply {
                    time = today
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (appCalendar.timeInMillis <= todayCalendar.timeInMillis) {
                    appError = "Application deadline must be at least tomorrow"
                }
            }
        } catch (e: Exception) {
            appError = "Invalid date format"
        }
    }

    if (reviewDeadline != null && applicationDeadline != null) {
        try {
            val appDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(applicationDeadline)
            val reviewDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(reviewDeadline)
            if (appDate != null && reviewDate != null && reviewDate <= appDate) {
                reviewError = "Review deadline must be after application deadline"
            }
        } catch (e: Exception) {
        }
    }

    return Pair(appError, reviewError)
}

private fun isStepValid(
    step: Int,
    title: String,
    ageRating: AgeRating?,
    distributionType: DistributionType?,
    applicationDeadline: String?,
    selectionMethod: SelectionMethod?,
    bookFileUri: android.net.Uri?,
    currentDistributionType: DistributionType?,
    titleError: String? = null,
    applicationDeadlineError: String? = null
): Boolean {
    return when (step) {
        1 -> title.isNotBlank() && titleError == null
        2 -> true
        3 -> ageRating != null && distributionType != null
        4 -> applicationDeadline != null && applicationDeadlineError == null
        5 -> {
            val requiresFile = currentDistributionType == DistributionType.DIGITAL ||
                    currentDistributionType == DistributionType.BOTH
            if (requiresFile) {
                bookFileUri != null
            } else {
                true
            }
        }

        6 -> true
        else -> false
    }
}
