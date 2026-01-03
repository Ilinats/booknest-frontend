package com.example.booknest.ui.author

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.GenresRepository
import com.example.booknest.viewmodel.AuthorViewModel
import com.example.booknest.viewmodel.BookViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

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
                    com.example.booknest.ui.error.GlobalErrorHandler.showError(e)
                    genres = emptyList()
                }
        } catch (e: Exception) {
            com.example.booknest.ui.error.GlobalErrorHandler.showError(e)
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
                                                currentStep = stepNumber
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
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
                                            var coverRemoveError: String? = null
                                            authorViewModel.removeBookCoverImage(
                                                bookId,
                                                onSuccess = {
                                                    coverImageUrl = null
                                                    shouldRemoveCoverImage = false
                                                    initialCoverImageUrl = null
                                                },
                                                onError = { errorMsg ->
                                                    com.example.booknest.ui.error.GlobalErrorHandler.showError(
                                                        errorMsg
                                                    )
                                                    coverRemoveError = errorMsg
                                                }
                                            )
                                            kotlinx.coroutines.delay(300)
                                            if (coverRemoveError != null) {
                                                isSaving = false
                                                saveError = coverRemoveError
                                                return@launch
                                            }
                                        }

                                        authorViewModel.updateBook(bookId, updateRequest)

                                        coverImageUri?.let { uri ->
                                            var coverUploadError: String? = null
                                            authorViewModel.uploadBookCoverImage(
                                                bookId,
                                                uri,
                                                context,
                                                onSuccess = { coverUrl ->
                                                    coverImageUrl = coverUrl
                                                    shouldRemoveCoverImage = false
                                                },
                                                onError = { errorMsg ->
                                                    com.example.booknest.ui.error.GlobalErrorHandler.showError(
                                                        errorMsg
                                                    )
                                                    coverUploadError = errorMsg
                                                }
                                            )
                                            kotlinx.coroutines.delay(300)
                                            if (coverUploadError != null) {
                                                isSaving = false
                                                saveError = coverUploadError
                                                return@launch
                                            }
                                        }

                                        bookFileUri?.let { uri ->
                                            var fileUploadError: String? = null
                                            authorViewModel.uploadBookFile(
                                                bookId,
                                                uri,
                                                context,
                                                onSuccess = {
                                                },
                                                onError = { errorMsg ->
                                                    com.example.booknest.ui.error.GlobalErrorHandler.showError(
                                                        errorMsg
                                                    )
                                                    fileUploadError = errorMsg
                                                }
                                            )
                                            kotlinx.coroutines.delay(300)
                                            if (fileUploadError != null) {
                                                isSaving = false
                                                saveError = fileUploadError
                                                return@launch
                                            }
                                        }

                                        isSaving = false
                                        navController.popBackStack()
                                    } catch (e: Exception) {
                                        isSaving = false
                                        saveError = e.message ?: "Failed to update book"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving && !isLoading && hasChanges
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Save Changes")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { if (currentStep > 1) currentStep-- },
                                enabled = currentStep > 1
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Previous")
                            }

                            if (currentStep < totalSteps) {
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
                                        selectedDistributionType,
                                        bookDetails?.fileUrl
                                    )
                                ) {
                                    Text("Next")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                        }
                    }
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
fun FileUploadStepEdit(
    bookFileUri: Uri?,
    bookFileName: String?,
    bookFileSize: Long?,
    distributionType: DistributionType?,
    existingFileUrl: String?,
    existingFileName: String?,
    existingFileSize: Long?,
    onFileSelected: (Uri?, String?, Long?) -> Unit
) {
    val context = LocalContext.current
    var fileError by remember { mutableStateOf<String?>(null) }

    val isRequired =
        distributionType == DistributionType.DIGITAL || distributionType == DistributionType.BOTH
    val maxFileSize = 50 * 1024 * 1024L
    val hasExistingFile = !existingFileUrl.isNullOrBlank()
    val hasNewFile = bookFileUri != null

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

    fun formatFileSize(bytes: Long?): String {
        if (bytes == null) return "Unknown size"
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasNewFile || hasExistingFile)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    Color.Transparent
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasExistingFile && !hasNewFile) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current File",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = existingFileName ?: "Book file",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                existingFileSize?.let { size ->
                                    Text(
                                        text = "Size: ${formatFileSize(size)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Upload a new file to replace the current one",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (hasNewFile && bookFileName != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "New File Selected",
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
                }

                if (!hasNewFile) {
                    Button(
                        onClick = {
                            filePickerLauncher.launch("application/pdf,application/epub+zip")
                            fileError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Select File",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (hasExistingFile) "Replace File" else "Select Book File",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun isStepValid(
    step: Int,
    title: String,
    ageRating: AgeRating?,
    distributionType: DistributionType?,
    applicationDeadline: String?,
    selectionMethod: SelectionMethod?,
    bookFileUri: Uri?,
    currentDistributionType: DistributionType?,
    existingFileUrl: String?
): Boolean {
    return when (step) {
        1 -> title.isNotBlank()
        2 -> true
        3 -> ageRating != null && distributionType != null
        4 -> applicationDeadline != null
        5 -> {
            val requiresFile = currentDistributionType == DistributionType.DIGITAL ||
                    currentDistributionType == DistributionType.BOTH
            if (requiresFile) {
                bookFileUri != null || !existingFileUrl.isNullOrBlank()
            } else {
                true
            }
        }

        6 -> true
        else -> false
    }
}
