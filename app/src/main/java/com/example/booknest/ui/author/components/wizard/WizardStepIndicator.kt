package com.example.booknest.ui.author.components.wizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WizardStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    onStepClick: (Int) -> Unit
) {
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
                                    onStepClick(stepNumber)
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
