@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.booknest.ui.author.components.bookedit

import android.net.Uri
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.DatePickerState
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.ui.author.components.common.AgeRating
import com.example.booknest.ui.author.components.common.DistributionType
import com.example.booknest.ui.author.components.common.SelectionMethod
import com.example.booknest.ui.author.components.wizard.BasicInfoStep
import com.example.booknest.ui.author.components.wizard.DistributionStep
import com.example.booknest.ui.author.components.wizard.FileUploadStepEdit
import com.example.booknest.ui.author.components.wizard.GenresAndSeriesStep
import com.example.booknest.ui.author.components.wizard.PreviewStep
import com.example.booknest.ui.author.components.wizard.ReviewConfigStep

fun LazyListScope.bookEditWizardSteps(
    currentStep: Int,
    bookDetails: BookResponse?,
    title: String,
    shortDescription: String,
    fullDescription: String,
    pageCount: String,
    coverImageUri: Uri?,
    coverImageUrl: String?,
    titleError: String?,
    shortDescriptionError: String?,
    fullDescriptionError: String?,
    pageCountError: String?,
    onBasicInfoUpdate: (String, String, String, String, Uri?, String?) -> Unit,
    onBasicInfoValidationChange: (String?, String?, String?, String?) -> Unit,
    selectedGenres: List<Int>,
    selectedSeries: SeriesResponse?,
    seriesOrder: String,
    mySeries: List<SeriesResponse>,
    genres: List<GenreResponse>,
    seriesOrderError: String?,
    onGenresUpdate: (List<Int>, SeriesResponse?, String) -> Unit,
    onCreateSeries: (String, String) -> Unit,
    showCreateSeriesDialog: Boolean,
    onShowCreateSeriesDialog: () -> Unit,
    onDismissCreateSeriesDialog: () -> Unit,
    onSeriesOrderValidationChange: (String?) -> Unit,
    selectedAgeRating: AgeRating?,
    selectedDistributionType: DistributionType?,
    totalCopies: String,
    totalCopiesError: String?,
    onDistributionUpdate: (AgeRating?, DistributionType?, String) -> Unit,
    onDistributionValidationChange: (String?) -> Unit,
    applicationDeadline: String?,
    reviewDeadline: String?,
    selectedSelectionMethod: SelectionMethod?,
    selectionCriteria: String,
    showApplicationDatePicker: Boolean,
    showReviewDatePicker: Boolean,
    applicationDatePickerState: DatePickerState,
    reviewDatePickerState: DatePickerState,
    applicationDeadlineError: String?,
    reviewDeadlineError: String?,
    onReviewConfigUpdate: (String?, String?, SelectionMethod?, String) -> Unit,
    onShowApplicationDatePicker: () -> Unit,
    onShowReviewDatePicker: () -> Unit,
    onDismissApplicationDatePicker: () -> Unit,
    onDismissReviewDatePicker: () -> Unit,
    onReviewDeadlineValidationChange: (String?, String?) -> Unit,
    bookFileUri: Uri?,
    bookFileName: String?,
    bookFileSize: Long?,
    onBookFileSelected: (Uri?, String?, Long?) -> Unit,
) {
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
                    onUpdate = onBasicInfoUpdate,
                    onValidationChange = onBasicInfoValidationChange
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
                    onUpdate = onGenresUpdate,
                    onCreateSeries = onCreateSeries,
                    onShowCreateSeriesDialog = onShowCreateSeriesDialog,
                    showCreateSeriesDialog = showCreateSeriesDialog,
                    onDismissCreateSeriesDialog = onDismissCreateSeriesDialog,
                    onValidationChange = onSeriesOrderValidationChange
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
                    onUpdate = onDistributionUpdate,
                    onValidationChange = onDistributionValidationChange
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
                    onUpdate = onReviewConfigUpdate,
                    onShowApplicationDatePicker = onShowApplicationDatePicker,
                    onShowReviewDatePicker = onShowReviewDatePicker,
                    onDismissApplicationDatePicker = onDismissApplicationDatePicker,
                    onDismissReviewDatePicker = onDismissReviewDatePicker,
                    onValidationChange = onReviewDeadlineValidationChange
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
                    onFileSelected = onBookFileSelected
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
}
