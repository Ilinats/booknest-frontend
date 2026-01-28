package com.example.booknest.ui.author

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.GenresRepository
import com.example.booknest.ui.author.components.wizard.*
import com.example.booknest.ui.author.components.common.*
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.AuthorViewModel
import com.example.booknest.ui.state.UiState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*
import com.example.booknest.ui.author.components.common.DistributionType
import com.example.booknest.ui.author.components.common.AgeRating
import com.example.booknest.ui.author.components.common.SelectionMethod

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
    val bookFileUploadState by authorViewModel.bookFileUploadState.collectAsState()
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
                    com.example.booknest.ui.toast.GlobalToastHandler.showError(e)
                    genres = emptyList()
                }
        } catch (e: Exception) {
            com.example.booknest.ui.toast.GlobalToastHandler.showError(e)
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
                WizardStepIndicator(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    onStepClick = { step ->
                        if (step < currentStep) {
                            currentStep = step
                        }
                    }
                )
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
                                println("DEBUG: BookCreationWizard onValidationChange called with pcErr: $pcErr")
                                titleError = tErr
                                shortDescriptionError = sdErr
                                fullDescriptionError = fdErr
                                pageCountError = pcErr
                                println("DEBUG: BookCreationWizard pageCountError set to: $pageCountError")
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
                                    CreateSeriesRequest(
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
                WizardNavigation(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    title = title,
                    selectedAgeRating = selectedAgeRating,
                    selectedDistributionType = selectedDistributionType,
                    applicationDeadline = applicationDeadline,
                    selectedSelectionMethod = selectedSelectionMethod,
                    bookFileUri = bookFileUri,
                    titleError = titleError,
                    applicationDeadlineError = applicationDeadlineError,
                    isCreating = isCreating,
                    isUploadingFile = isUploadingFile,
                    coverImageUrl = coverImageUrl,
                    shortDescription = shortDescription,
                    fullDescription = fullDescription,
                    pageCount = pageCount,
                    totalCopies = totalCopies,
                    selectedGenres = selectedGenres,
                    selectedSeries = selectedSeries,
                    seriesOrder = seriesOrder,
                    reviewDeadline = reviewDeadline,
                    selectionCriteria = selectionCriteria,
                    onPreviousStep = { if (currentStep > 1) currentStep-- },
                    onNextStep = { currentStep++ },
                    onSaveAsDraft = {
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
                    onCreateBook = {
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
                    }
                )
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
            is UiState.Success -> {
                isCreating = false
                createdBookId = state.data.id
                if (bookFileUri != null) {
                    isUploadingFile = true
                    authorViewModel.uploadBookFile(
                        bookId = state.data.id,
                        fileUri = bookFileUri!!,
                        context = context,
                        onSuccess = {
                        },
                        onError = { errorMsg ->
                            isUploadingFile = false
                            creationError = errorMsg
                            scope.launch {
                                kotlinx.coroutines.delay(2000)
                                creationError = null
                            }
                        }
                    )
                } else {
                    if (shouldPublishAfterCreation) {
                        showPublishDialog = true
                    } else {
                        authorViewModel.clearBookCreationState()
                        navController.popBackStack()
                    }
                }
            }

            is UiState.Error -> {
                isCreating = false
                isUploadingFile = false
                creationError = state.message
                scope.launch {
                    kotlinx.coroutines.delay(3000)
                    creationError = null
                }
            }

            else -> {}
        }
    }

    LaunchedEffect(bookFileUploadState, createdBookId) {
        val state = bookFileUploadState
        when (state) {
            is UiState.Success -> {
                if (state.data == createdBookId) {
                    isUploadingFile = false
                    if (shouldPublishAfterCreation) {
                        showPublishDialog = true
                    } else {
                        authorViewModel.clearBookCreationState()
                        navController.popBackStack()
                    }
                }
            }
            is UiState.Error -> {
                if (createdBookId != null) {
                    isUploadingFile = false
                    creationError = "Book created but file upload failed: ${state.message}"
                    scope.launch {
                        kotlinx.coroutines.delay(2000)
                        creationError = null
                    }
                }
            }
            else -> {}
        }
    }

    if (showPublishDialog && createdBookId != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showPublishDialog = false
                authorViewModel.clearBookCreationState()
                navController.popBackStack()
            },
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
                        Text("Keep as Draft")
                    }
                    Button(
                        onClick = {
                            showPublishDialog = false
                            authorViewModel.publishBook(createdBookId!!)
                            authorViewModel.clearBookCreationState()
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Publish Now")
                    }
                }
            }
        )
    }

    @Composable
    fun validateTitle(title: String): String? {
        return when {
            title.isBlank() -> "Title is required"
            title.length > 255 -> "Title must be 255 characters or less"
            else -> null
        }
    }
}

private fun validateDeadlines(
    applicationDeadline: String?,
    reviewDeadline: String?
): Pair<String?, String?> {
    val appError = if (applicationDeadline.isNullOrBlank()) {
        "Application deadline is required"
    } else null

    val revError = if (!reviewDeadline.isNullOrBlank()) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val appDate = dateFormat.parse(applicationDeadline!!)
            val revDate = dateFormat.parse(reviewDeadline)

            if (appDate != null && revDate != null && revDate.before(appDate)) {
                "Review deadline must be after application deadline"
            } else null
        } catch (e: Exception) {
            "Invalid date format"
        }
    } else {
        null
    }
    return Pair(appError, revError)
}
