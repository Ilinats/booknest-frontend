package com.example.booknest.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeInputField(
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    codeLength: Int = 6
) {
    var code by remember { mutableStateOf("") }
    val focusRequesters = remember { List(codeLength) { FocusRequester() } }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        repeat(codeLength) { index ->
            val isFilled = index < code.length
            val currentDigit = if (isFilled) code[index].toString() else ""
            
            BasicTextField(
                value = currentDigit,
                onValueChange = { newValue ->
                    // Handle paste (multiple digits at once)
                    if (newValue.length > 1 && newValue.all { it.isDigit() }) {
                        // Paste detected - fill all fields with the pasted digits
                        val pastedDigits = newValue.take(codeLength)
                        code = pastedDigits.padEnd(codeLength, ' ').trim()
                        
                        // Focus the next empty field or the last field
                        val nextEmptyIndex = pastedDigits.length.coerceAtMost(codeLength - 1)
                        if (nextEmptyIndex < codeLength) {
                            focusRequesters[nextEmptyIndex].requestFocus()
                        }
                        
                        // Notify parent of code change
                        onCodeChange(code)
                    }
                    // Handle single digit input
                    else if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                        val newCode = code.toMutableList()
                        
                        // Ensure we have enough elements
                        while (newCode.size <= index) {
                            newCode.add(' ')
                        }
                        
                        if (newValue.isEmpty()) {
                            // Delete current digit
                            newCode[index] = ' '
                            code = newCode.joinToString("").trim()
                            
                            // Move focus to previous field if current field is now empty
                            if (index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        } else {
                            newCode[index] = newValue.last()
                            code = newCode.joinToString("").trim()
                            
                            // Auto-focus next field
                            if (index < codeLength - 1) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        }
                        
                        // Notify parent of code change
                        onCodeChange(code)
                    }
                },
                modifier = Modifier
                    .width(48.dp)
                    .height(56.dp)
                    .focusRequester(focusRequesters[index])
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 2.dp,
                        color = if (isFilled) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    ),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ResendCodeButton(
    onResend: () -> Unit,
    cooldownSeconds: Int = 60,
    modifier: Modifier = Modifier
) {
    var timeLeft by remember { mutableStateOf(0) }
    var isEnabled by remember { mutableStateOf(true) }
    
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            kotlinx.coroutines.delay(1000)
            timeLeft--
        } else {
            isEnabled = true
        }
    }
    
    LaunchedEffect(Unit) {
        timeLeft = cooldownSeconds
        isEnabled = false
    }
    
    TextButton(
        onClick = {
            if (isEnabled) {
                onResend()
                timeLeft = cooldownSeconds
                isEnabled = false
            }
        },
        enabled = isEnabled,
        modifier = modifier
    ) {
        Text(
            text = if (isEnabled) "Resend code" else "Resend code in ${timeLeft}s",
            color = if (isEnabled) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
