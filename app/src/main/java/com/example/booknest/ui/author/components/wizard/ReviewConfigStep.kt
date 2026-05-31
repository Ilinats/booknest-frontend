package com.example.booknest.ui.author.components.wizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.validation.BookFormRules
import com.example.booknest.ui.author.components.bookedit.REVIEW_DEADLINE_MIN_OFFSET_MESSAGE
import com.example.booknest.ui.author.components.bookedit.validateDeadlines
import com.example.booknest.ui.author.components.common.bookFormFieldSupportingText
import com.example.booknest.ui.author.components.common.DatePickerDialog
import com.example.booknest.ui.author.components.common.SelectionMethod
import com.example.booknest.utils.BookDateUtils

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
    selectionCriteriaError: String? = null,
    onUpdate: (String?, String?, SelectionMethod?, String) -> Unit,
    onShowApplicationDatePicker: () -> Unit,
    onShowReviewDatePicker: () -> Unit,
    onDismissApplicationDatePicker: () -> Unit,
    onDismissReviewDatePicker: () -> Unit,
    onValidationChange: ((String?, String?, String?) -> Unit)? = null
) {
    val formattedApplicationDeadline = remember(applicationDeadline) {
        applicationDeadline?.let { BookDateUtils.formatDateOnlyForDisplay(it) } ?: ""
    }

    val formattedReviewDeadline = remember(reviewDeadline) {
        reviewDeadline?.let { BookDateUtils.formatDateOnlyForDisplay(it) } ?: ""
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
            isError = applicationDeadlineError != null,
            supportingText = applicationDeadlineError?.let {
                {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } ?: {
                Text(
                    "Must be at least tomorrow",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            } ?: {
                Text(
                    REVIEW_DEADLINE_MIN_OFFSET_MESSAGE,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                                    SelectionMethod.RANDOM -> "Reviewers are randomly selected via lottery after application deadline"
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
                onValidationChange?.invoke(
                    applicationDeadlineError,
                    reviewDeadlineError,
                    BookFormRules.validateSelectionCriteria(it),
                )
            },
            label = { Text("Selection Criteria") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            placeholder = { Text("Describe what you're looking for in reviewers (optional)") },
            isError = selectionCriteriaError != null,
            supportingText = bookFormFieldSupportingText(selectionCriteriaError),
        )
    }

    if (showApplicationDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis: Long? ->
                selectedDateMillis?.let { millis: Long ->
                    val newDeadline = BookDateUtils.pickerMillisToDateOnly(millis)
                    if (!BookDateUtils.isDateAtLeastTomorrow(newDeadline)) {
                        onValidationChange?.invoke(
                            "Application deadline must be at least tomorrow",
                            reviewDeadlineError,
                            BookFormRules.validateSelectionCriteria(selectionCriteria),
                        )
                    } else {
                        onUpdate(newDeadline, reviewDeadline, selectedSelectionMethod, selectionCriteria)
                        val (appErr, revErr) = validateDeadlines(newDeadline, reviewDeadline)
                        onValidationChange?.invoke(
                            appErr,
                            revErr,
                            BookFormRules.validateSelectionCriteria(selectionCriteria),
                        )
                    }
                }
                onDismissApplicationDatePicker()
            },
            onDismiss = onDismissApplicationDatePicker,
            datePickerState = applicationDatePickerState
        )
    }

    if (showReviewDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis: Long? ->
                selectedDateMillis?.let { millis: Long ->
                    val newDeadline = BookDateUtils.pickerMillisToDateOnly(millis)
                    onUpdate(applicationDeadline, newDeadline, selectedSelectionMethod, selectionCriteria)
                    val (appErr, revErr) = validateDeadlines(applicationDeadline, newDeadline)
                    onValidationChange?.invoke(
                        appErr,
                        revErr,
                        BookFormRules.validateSelectionCriteria(selectionCriteria),
                    )
                }
                onDismissReviewDatePicker()
            },
            onDismiss = onDismissReviewDatePicker,
            datePickerState = reviewDatePickerState
        )
    }
}

