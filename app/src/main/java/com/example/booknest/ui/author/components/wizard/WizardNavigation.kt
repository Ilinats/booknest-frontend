package com.example.booknest.ui.author.components.wizard

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.ui.author.components.common.*
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.viewmodel.AuthorViewModel

@Composable
fun WizardNavigation(
    currentStep: Int,
    totalSteps: Int,
    title: String,
    selectedAgeRating: AgeRating?,
    selectedDistributionType: DistributionType?,
    applicationDeadline: String?,
    selectedSelectionMethod: SelectionMethod?,
    bookFileUri: android.net.Uri?,
    titleError: String?,
    applicationDeadlineError: String?,
    isCreating: Boolean,
    isUploadingFile: Boolean,
    coverImageUrl: String?,
    shortDescription: String,
    fullDescription: String,
    pageCount: String,
    totalCopies: String,
    selectedGenres: List<Int>,
    selectedSeries: com.example.booknest.domain.model.response.SeriesResponse?,
    seriesOrder: String,
    reviewDeadline: String?,
    selectionCriteria: String,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onSaveAsDraft: () -> Unit,
    onCreateBook: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentStep < totalSteps) {
            OutlinedButton(
                onClick = onPreviousStep,
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
                onClick = onNextStep,
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
                onClick = onSaveAsDraft,
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
                onClick = onCreateBook,
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
