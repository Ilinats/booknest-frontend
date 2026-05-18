package com.example.booknest.ui.author

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.booknest.domain.repository.GenresRepository
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.port.ToastNotifier
import com.example.booknest.ui.author.components.bookedit.BookEditPublishedWarningCard
import com.example.booknest.ui.author.components.bookedit.BookEditSaveErrorBanner
import com.example.booknest.ui.author.components.bookedit.bookEditWizardSteps
import com.example.booknest.ui.author.components.bookedit.validateDeadlines
import com.example.booknest.ui.author.components.BookEditNavigation
import com.example.booknest.ui.author.components.LeakFingerprintDecodeSection
import com.example.booknest.ui.author.components.common.AgeRating
import com.example.booknest.ui.author.components.common.DatePickerDialog
import com.example.booknest.ui.author.components.common.DistributionType
import com.example.booknest.ui.author.components.common.SelectionMethod
import com.example.booknest.ui.author.components.wizard.WizardStepIndicator
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.author.AuthorBooksViewModel
import com.example.booknest.viewmodel.author.AuthorSeriesViewModel
import com.example.booknest.viewmodel.author.BookStatus
import com.example.booknest.viewmodel.books.BookViewModel
import com.example.booknest.presentation.common.UiState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditScreen(
    navController: NavController,
    bookId: String,
    sessionManager: SessionManager = koinInject(),
    genresRepository: GenresRepository = koinInject(),
    toastNotifier: ToastNotifier = koinInject(),
    authorBooksViewModel: AuthorBooksViewModel = getViewModel(),
    authorSeriesViewModel: AuthorSeriesViewModel = getViewModel(),
    bookViewModel: BookViewModel = getViewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 6

    val bookDetails by bookViewModel.bookDetails.collectAsState()
    val coverImageRemovalState by authorBooksViewModel.coverImageRemovalState.collectAsState()
    val coverImageUploadState by authorBooksViewModel.coverImageUploadState.collectAsState()
    val bookFileUploadState by authorBooksViewModel.bookFileUploadState.collectAsState()
    val leakFingerprintState by authorBooksViewModel.leakFingerprintState.collectAsState()
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
                    initialCoverImageUrl = stateCoverUrl
                    coverImageUri = null
                    shouldRemoveCoverImage = false
                    bookViewModel.getBookDetails(bookId)
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

    val mySeries by authorSeriesViewModel.mySeries.collectAsState()
    var genres by remember { mutableStateOf(listOf<GenreResponse>()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val applicationDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = null
    )
    val reviewDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = null
    )

    val showLeakFingerprintTool =
        bookDetails?.let { b ->
            !b.fileUrl.isNullOrBlank() && b.distributionType != DistributionType.PHYSICAL.value
        } == true

    LaunchedEffect(bookId) {
        bookViewModel.getBookDetails(bookId)
        authorBooksViewModel.clearLeakFingerprintState()
    }

    LaunchedEffect(Unit) {
        authorSeriesViewModel.loadMySeries()
        try {
            val result = genresRepository.getGenres()
            result
                .onSuccess { genreList ->
                    genres = genreList
                }
                .onFailure { e ->
                    toastNotifier.showError(e)
                    genres = emptyList()
                }
        } catch (e: Exception) {
            toastNotifier.showError(e)
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
                        BookEditPublishedWarningCard()
                    }
                }

                if (showLeakFingerprintTool) {
                    item {
                        LeakFingerprintDecodeSection(
                            leakFingerprintState = leakFingerprintState,
                            onFileChosen = { uri ->
                                authorBooksViewModel.decodeLeakFingerprint(bookId, uri, context)
                            },
                            onDismissResult = { authorBooksViewModel.clearLeakFingerprintState() }
                        )
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

                bookEditWizardSteps(
                    currentStep = currentStep,
                    bookDetails = bookDetails,
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
                    onBasicInfoUpdate = { t, sd, fd, pc, uri, url ->
                        title = t
                        shortDescription = sd
                        fullDescription = fd
                        pageCount = pc
                        coverImageUri = uri
                        coverImageUrl = url
                        shouldRemoveCoverImage =
                            initialCoverImageUrl != null && uri == null && url == null
                    },
                    onBasicInfoValidationChange = { tErr, sdErr, fdErr, pcErr ->
                        titleError = tErr
                        shortDescriptionError = sdErr
                        fullDescriptionError = fdErr
                        pageCountError = pcErr
                    },
                    selectedGenres = selectedGenres,
                    selectedSeries = selectedSeries,
                    seriesOrder = seriesOrder,
                    mySeries = mySeries,
                    genres = genres,
                    seriesOrderError = seriesOrderError,
                    onGenresUpdate = { sg, ss, so ->
                        selectedGenres = sg
                        selectedSeries = ss
                        seriesOrder = so
                    },
                    onCreateSeries = { name: String, description: String ->
                        authorSeriesViewModel.createSeries(
                            CreateSeriesRequest(
                                name = name,
                                description = description.ifBlank { null }
                            )
                        )
                        showCreateSeriesDialog = false
                    },
                    showCreateSeriesDialog = showCreateSeriesDialog,
                    onShowCreateSeriesDialog = { showCreateSeriesDialog = true },
                    onDismissCreateSeriesDialog = { showCreateSeriesDialog = false },
                    onSeriesOrderValidationChange = { err ->
                        seriesOrderError = err
                    },
                    selectedAgeRating = selectedAgeRating,
                    selectedDistributionType = selectedDistributionType,
                    totalCopies = totalCopies,
                    totalCopiesError = totalCopiesError,
                    onDistributionUpdate = { ar, dt, tc ->
                        selectedAgeRating = ar
                        selectedDistributionType = dt
                        totalCopies = tc
                    },
                    onDistributionValidationChange = { err ->
                        totalCopiesError = err
                    },
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
                    onReviewConfigUpdate = { ad, rd, ssm, sc ->
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
                    onReviewDeadlineValidationChange = { appErr, revErr ->
                        applicationDeadlineError = appErr
                        reviewDeadlineError = revErr
                    },
                    bookFileUri = bookFileUri,
                    bookFileName = bookFileName,
                    bookFileSize = bookFileSize,
                    onBookFileSelected = { uri, name, size ->
                        bookFileUri = uri
                        bookFileName = name
                        bookFileSize = size
                    },
                )

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
                                        authorBooksViewModel.removeBookCoverImage(bookId)
                                        var removalComplete = false
                                        var attempts = 0
                                        while (!removalComplete && attempts < 100) {
                                            kotlinx.coroutines.delay(100)
                                            val removalState = authorBooksViewModel.coverImageRemovalState.value
                                            removalComplete = removalState is UiState.Success || removalState is UiState.Error
                                            attempts++
                                        }
                                    }

                                    authorBooksViewModel.updateBook(bookId, updateRequest)

                                    coverImageUri?.let { uri ->
                                        authorBooksViewModel.uploadBookCoverImage(bookId, uri, context)
                                        var uploadComplete = false
                                        var attempts = 0
                                        while (!uploadComplete && attempts < 300) {
                                            kotlinx.coroutines.delay(100)
                                            val uploadState = authorBooksViewModel.coverImageUploadState.value
                                            uploadComplete = uploadState is UiState.Success || uploadState is UiState.Error
                                            attempts++
                                        }
                                        coverImageUri = null
                                    }

                                    bookFileUri?.let { uri ->
                                        authorBooksViewModel.uploadBookFile(
                                            bookId = bookId,
                                            fileUri = uri,
                                            context = context,
                                            onSuccess = { },
                                            onError = { }
                                        )
                                        var fileUploadComplete = false
                                        var attempts = 0
                                        while (!fileUploadComplete && attempts < 600) {
                                            kotlinx.coroutines.delay(100)
                                            val fileUploadState = authorBooksViewModel.bookFileUploadState.value
                                            fileUploadComplete = fileUploadState is UiState.Success || fileUploadState is UiState.Error
                                            attempts++
                                        }
                                        bookFileUri = null
                                    }

                                    bookViewModel.getBookDetails(bookId)
                                    kotlinx.coroutines.delay(300)

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
                        BookEditSaveErrorBanner(message = error)
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
