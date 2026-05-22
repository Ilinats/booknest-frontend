package com.example.booknest.ui.author.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.validation.BookFormRules
import com.example.booknest.ui.author.components.common.AgeRating
import com.example.booknest.ui.author.components.common.DistributionType
import com.example.booknest.ui.author.components.common.SelectionMethod

@Composable
fun BookEditNavigation(
    currentStep: Int,
    totalSteps: Int,
    title: String,
    shortDescription: String,
    fullDescription: String,
    pageCount: String,
    selectionCriteriaError: String?,
    selectedAgeRating: AgeRating?,
    selectedDistributionType: DistributionType?,
    applicationDeadline: String?,
    applicationDeadlineError: String?,
    reviewDeadlineError: String?,
    selectedSelectionMethod: SelectionMethod?,
    bookFileUri: Uri?,
    existingFileUrl: String?,
    isSaving: Boolean,
    isLoading: Boolean,
    hasChanges: Boolean,
    onSave: () -> Unit,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving &&
                !isLoading &&
                hasChanges &&
                BookFormRules.isBasicInfoValid(title, shortDescription, fullDescription, pageCount) &&
                applicationDeadlineError == null &&
                reviewDeadlineError == null &&
                selectionCriteriaError == null
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
                onClick = onPreviousStep,
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
                    onClick = onNextStep,
                    enabled = isStepValid(
                        step = currentStep,
                        title = title,
                        shortDescription = shortDescription,
                        fullDescription = fullDescription,
                        pageCount = pageCount,
                        ageRating = selectedAgeRating,
                        distributionType = selectedDistributionType,
                        applicationDeadline = applicationDeadline,
                        applicationDeadlineError = applicationDeadlineError,
                        reviewDeadlineError = reviewDeadlineError,
                        selectionCriteriaError = selectionCriteriaError,
                        selectionMethod = selectedSelectionMethod,
                        bookFileUri = bookFileUri,
                        currentDistributionType = selectedDistributionType,
                        existingFileUrl = existingFileUrl,
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

private fun isStepValid(
    step: Int,
    title: String,
    shortDescription: String,
    fullDescription: String,
    pageCount: String,
    ageRating: AgeRating?,
    distributionType: DistributionType?,
    applicationDeadline: String?,
    applicationDeadlineError: String?,
    reviewDeadlineError: String?,
    selectionCriteriaError: String?,
    selectionMethod: SelectionMethod?,
    bookFileUri: Uri?,
    currentDistributionType: DistributionType?,
    existingFileUrl: String?,
): Boolean {
    return when (step) {
        1 -> BookFormRules.isBasicInfoValid(title, shortDescription, fullDescription, pageCount)
        2 -> true
        3 -> ageRating != null && distributionType != null
        4 -> applicationDeadline != null &&
            applicationDeadlineError == null &&
            reviewDeadlineError == null &&
            selectionCriteriaError == null
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
