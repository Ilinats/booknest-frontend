package com.example.booknest.ui.author

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.GenresRepository
import com.example.booknest.ui.author.components.BookEditNavigation
import com.example.booknest.ui.author.components.wizard.*
import com.example.booknest.ui.author.components.common.*
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.AuthorViewModel
import com.example.booknest.viewmodel.BookViewModel
import com.example.booknest.ui.state.UiState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*
import com.example.booknest.ui.author.components.common.SelectionMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditScreen(
    navController: NavController,
    bookId: String,
    sessionManager: SessionManager = koinInject(),
    genresRepository: GenresRepository = koinInject(),
    authorViewModel: AuthorViewModel = getViewModel(),
    bookViewModel: BookViewModel = getViewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 6

    val bookDetails by bookViewModel.bookDetails.collectAsState()
    val coverImageRemovalState by authorViewModel.coverImageRemovalState.collectAsState()
    val coverImageUploadState by authorViewModel.coverImageUploadState.collectAsState()
    val bookFileUploadState by authorViewModel.bookFileUploadState.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    val isPublished = remember(bookDetails) {
        bookDetails?.let { book ->
            book.status == BookStatus.ACTIVE.value || !book.publishedAt.isNullOrBlank()
        } ?: false
    }

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
    var selectedSelectionMethod by remember { mutableStateOf<SelectionMethod?>(null) }
    var selectionCriteria by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedSeries by remember { mutableStateOf<SeriesResponse?>(null) }
    var seriesOrder by remember { mutableStateOf("") }

    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var coverImageUrl by remember { mutableStateOf<String?>(null) }
    var shouldRemoveCoverImage by remember { mutableStateOf(false) }
    var bookFileUri by remember { mutableStateOf<Uri?>(null) }
    var bookFileName by remember { mutableStateOf<String?>(null) }
    var bookFileSize by remember { mutableStateOf<Long?>(null) }

    var initialTitle by remember { mutableStateOf<String?>(null) }
    var initialShortDescription by remember { mutableStateOf<String?>(null) }
    var initialFullDescription by remember { mutableStateOf<String?>(null) }
    var initialPageCount by remember { mutableStateOf<String?>(null) }
    var initialAgeRating by remember { mutableStateOf<AgeRating?>(null) }
    var initialDistributionType by remember { mutableStateOf<DistributionType?>(null) }
    var initialTotalCopies by remember { mutableStateOf<String?>(null) }
    var initialApplicationDeadline by remember { mutableStateOf<String?>(null) }
    var initialReviewDeadline by remember { mutableStateOf<String?>(null) }
    var initialSelectionMethod by remember { mutableStateOf<SelectionMethod?>(null) }
    var initialSelectionCriteria by remember { mutableStateOf<String?>(null) }
    var initialGenres by remember { mutableStateOf<List<Int>>(emptyList()) }
    var initialSeriesId by remember { mutableStateOf<String?>(null) }
    var initialSeriesOrder by remember { mutableStateOf<String?>(null) }
    var initialCoverImageUrl by remember { mutableStateOf<String?>(null) }
    var initialBookFileUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(coverImageRemovalState) {
        when (coverImageRemovalState) {
            is UiState.Success -> {
                coverImageUrl = null
                shouldRemoveCoverImage = false
                initialCoverImageUrl = null
            }
            is UiState.Error -> {
            }
            else -> {}
        }
    }

    LaunchedEffect(coverImageUploadState) {
        val state = coverImageUploadState
        when (state) {
            is UiState.Success -> {
                val (stateBookId, stateCoverUrl) = state.data
                if (stateBookId == bookId) {
                    coverImageUrl = stateCoverUrl
                    shouldRemoveCoverImage = false
                }
            }
            is UiState.Error -> {
            }
            else -> {}
        }
    }

    var titleError by remember { mutableStateOf<String?>(null) }
    var shortDescriptionError by remember { mutableStateOf<String?>(null) }
    var fullDescriptionError by remember { mutableStateOf<String?>(null) }
    var pageCountError by remember { mutableStateOf<String?>(null) }
    var totalCopiesError by remember { mutableStateOf<String?>(null) }
    var seriesOrderError by remember { mutableStateOf<String?>(null) }
    var applicationDeadlineError by remember { mutableStateOf<String?>(null) }
    var reviewDeadlineError by remember { mutableStateOf<String?>(null) }

    var showCreateSeriesDialog by remember { mutableStateOf(false) }

    val mySeries by authorViewModel.mySeries.collectAsState()
    var genres by remember { mutableStateOf(listOf<GenreResponse>()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val applicationDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = null
    )
    val reviewDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = null
    )

    LaunchedEffect(bookId) {
        bookViewModel.getBookDetails(bookId)
    }

    LaunchedEffect(Unit) {
        authorViewModel.loadMySeries()
        try {
            val result = genresRepository.getGenres()
            result
                .onSuccess { genreList ->
                    genres = genreList
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

    LaunchedEffect(bookDetails) {
        bookDetails?.let { book ->
            title = book.title
            shortDescription = book.shortDescription ?: ""
            fullDescription = book.fullDescription ?: ""
            pageCount = book.pageCount?.toString() ?: ""
            selectedAgeRating = AgeRating.values().find { it.value == book.ageRating }
            selectedDistributionType =
                DistributionType.values().find { it.value == book.distributionType }
            totalCopies = book.totalCopies?.toString() ?: "1"

            book.applicationDeadline?.let { dateStr ->
                try {
                    val isoFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val date = isoFormat.parse(dateStr) ?: SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).parse(dateStr)
                    date?.let {
                        val formattedDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                        applicationDeadline = formattedDate
                        applicationDatePickerState.selectedDateMillis = it.time
                    }
                } catch (e: Exception) {
                    try {
                        val date =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                        date?.let {
                            applicationDeadline = dateStr
                            applicationDatePickerState.selectedDateMillis = it.time
                        }
                    } catch (e2: Exception) {
                        applicationDeadline = dateStr
                    }
                }
            }

            book.reviewDeadline?.let { dateStr ->
                try {
                    val isoFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val date = isoFormat.parse(dateStr) ?: SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).parse(dateStr)
                    date?.let {
                        val formattedDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                        reviewDeadline = formattedDate
                        reviewDatePickerState.selectedDateMillis = it.time
                    }
                } catch (e: Exception) {
                    try {
                        val date =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                        date?.let {
                            reviewDeadline = dateStr
                            reviewDatePickerState.selectedDateMillis = it.time
                        }
                    } catch (e2: Exception) {
                        reviewDeadline = dateStr
                    }
                }
            }

            selectedSelectionMethod =
                SelectionMethod.values().find { it.value == book.selectionMethod }
            selectionCriteria = book.selectionCriteria ?: ""

            selectedGenres = book.resolvedGenres.map { it.id }

            book.seriesId?.let { seriesId ->
                mySeries.find { it.id == seriesId }?.let { series ->
                    selectedSeries = series
                    seriesOrder = book.seriesOrder?.toString() ?: ""
                }
            }

            coverImageUrl = book.coverImageUrl
            shouldRemoveCoverImage = false

            if (initialTitle == null) {
                initialTitle = title
                initialShortDescription = shortDescription
                initialFullDescription = fullDescription
                initialPageCount = pageCount
                initialAgeRating = selectedAgeRating
                initialDistributionType = selectedDistributionType
                initialTotalCopies = totalCopies
                initialApplicationDeadline = applicationDeadline
                initialReviewDeadline = reviewDeadline
                initialSelectionMethod = selectedSelectionMethod
                initialSelectionCriteria = selectionCriteria
                initialGenres = selectedGenres.toList()
                initialSeriesId = selectedSeries?.id
                initialSeriesOrder = seriesOrder
                initialCoverImageUrl = coverImageUrl
                initialBookFileUrl = book.fileUrl
            }

            isLoading = false
        }
    }

    val hasChanges = remember(
        title, shortDescription, fullDescription, pageCount,
        selectedAgeRating, selectedDistributionType, totalCopies,
        applicationDeadline, reviewDeadline, selectedSelectionMethod,
        selectionCriteria, selectedGenres, selectedSeries?.id, seriesOrder,
        coverImageUrl, coverImageUri, bookFileUri,
        initialTitle, initialShortDescription, initialFullDescription, initialPageCount,
        initialAgeRating, initialDistributionType, initialTotalCopies,
        initialApplicationDeadline, initialReviewDeadline, initialSelectionMethod,
        initialSelectionCriteria, initialGenres, initialSeriesId, initialSeriesOrder,
        initialCoverImageUrl, initialBookFileUrl
    ) {
        if (initialTitle == null) return@remember false

        title.trim() != (initialTitle ?: "") ||
                shortDescription.trim() != (initialShortDescription ?: "") ||
                fullDescription.trim() != (initialFullDescription ?: "") ||
                pageCount.trim() != (initialPageCount ?: "") ||
                selectedAgeRating != initialAgeRating ||
                selectedDistributionType != initialDistributionType ||
                totalCopies.trim() != (initialTotalCopies ?: "") ||
                applicationDeadline != initialApplicationDeadline ||
                reviewDeadline != initialReviewDeadline ||
                selectedSelectionMethod != initialSelectionMethod ||
                selectionCriteria.trim() != (initialSelectionCriteria ?: "") ||
                selectedGenres.sorted() != initialGenres.sorted() ||
                selectedSeries?.id != initialSeriesId ||
                seriesOrder.trim() != (initialSeriesOrder ?: "") ||
                coverImageUrl != initialCoverImageUrl ||
                coverImageUri != null ||
                bookFileUri != null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Book (Step $currentStep of $totalSteps)",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isPublished) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "This book is published",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Changes will affect the live listing and may impact existing applications and reviews.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

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
                                    shouldRemoveCoverImage =
                                        initialCoverImageUrl != null && uri == null && url == null
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
                                onDismissApplicationDatePicker = {
                                    showApplicationDatePicker = false
                                },
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
                            FileUploadStepEdit(
                                bookFileUri = bookFileUri,
                                bookFileName = bookFileName,
                                bookFileSize = bookFileSize,
                                distributionType = selectedDistributionType,
                                existingFileUrl = bookDetails?.fileUrl,
                                existingFileName = bookDetails?.fileUrl?.substringAfterLast("/")
                                    ?.substringBefore("?"),
                                existingFileSize = bookDetails?.fileSize?.toLongOrNull(),
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
                                hasBookFile = bookFileUri != null || !bookDetails?.fileUrl.isNullOrBlank()
                            )
                        }
                    }
                }

                item {
                    BookEditNavigation(
                        currentStep = currentStep,
                        totalSteps = totalSteps,
                        title = title,
                        selectedAgeRating = selectedAgeRating,
                        selectedDistributionType = selectedDistributionType,
                        applicationDeadline = applicationDeadline,
                        selectedSelectionMethod = selectedSelectionMethod,
                        bookFileUri = bookFileUri,
                        existingFileUrl = bookDetails?.fileUrl,
                        isSaving = isSaving,
                        isLoading = isLoading,
                        hasChanges = hasChanges,
                        onSave = {
                            isSaving = true
                            saveError = null
                            val updateRequest = UpdateBookRequest(
                                title = title.ifBlank { null },
                                shortDescription = shortDescription.ifBlank { null },
                                fullDescription = fullDescription.ifBlank { null },
                                pageCount = pageCount.toIntOrNull(),
                                ageRating = selectedAgeRating?.value,
                                distributionType = selectedDistributionType?.value,
                                totalCopies = totalCopies.toIntOrNull(),
                                applicationDeadline = applicationDeadline,
                                reviewDeadline = reviewDeadline,
                                selectionMethod = selectedSelectionMethod?.value,
                                selectionCriteria = selectionCriteria.ifBlank { null },
                                genreIds = selectedGenres.ifEmpty { null },
                                seriesId = selectedSeries?.id,
                                seriesOrder = seriesOrder.toIntOrNull()
                            )
                            scope.launch {
                                try {
                                    if (shouldRemoveCoverImage) {
                                        authorViewModel.removeBookCoverImage(bookId)
                                        kotlinx.coroutines.delay(500)
                                    }

                                    authorViewModel.updateBook(bookId, updateRequest)

                                    coverImageUri?.let { uri ->
                                        authorViewModel.uploadBookCoverImage(bookId, uri, context)
                                        kotlinx.coroutines.delay(500)
                                    }

                                    bookFileUri?.let { uri ->
                                        authorViewModel.uploadBookFile(
                                            bookId = bookId,
                                            fileUri = uri,
                                            context = context,
                                            onSuccess = { },
                                            onError = { }
                                        )
                                        kotlinx.coroutines.delay(500)
                                    }

                                    isSaving = false
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    isSaving = false
                                    saveError = e.message ?: "Failed to update book"
                                }
                            }
                        },
                        onPreviousStep = { if (currentStep > 1) currentStep-- },
                        onNextStep = { currentStep++ }
                    )
                }

                saveError?.let { error ->
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
    }

    if (showApplicationDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val newDeadline = dateFormat.format(Date(it))
                    applicationDeadline = newDeadline
                    val (appErr, revErr) = validateDeadlines(newDeadline, reviewDeadline)
                    applicationDeadlineError = appErr
                    reviewDeadlineError = revErr
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
    } else null

    return Pair(appError, revError)
}
