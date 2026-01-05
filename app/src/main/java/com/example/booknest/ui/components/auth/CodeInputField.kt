package com.example.booknest.ui.components.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(codeLength) { index ->
            val isFilled = index < code.length
            val currentDigit = if (isFilled) code[index].toString() else ""

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .focusRequester(focusRequesters[index])
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = currentDigit,
                    onValueChange = { newValue ->
                        if (newValue.length > 1 && newValue.all { it.isDigit() }) {
                            val pastedDigits = newValue.take(codeLength)
                            code = pastedDigits.padEnd(codeLength, ' ').trim()

                            val nextEmptyIndex = pastedDigits.length.coerceAtMost(codeLength - 1)
                            if (nextEmptyIndex < codeLength) {
                                focusRequesters[nextEmptyIndex].requestFocus()
                            }

                            onCodeChange(code)
                        } else if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                            val newCode = code.toMutableList()

                            while (newCode.size <= index) {
                                newCode.add(' ')
                            }

                            if (newValue.isEmpty()) {
                                newCode[index] = ' '
                                code = newCode.joinToString("").trim()

                                if (index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            } else {
                                newCode[index] = newValue.last()
                                code = newCode.joinToString("").trim()

                                if (index < codeLength - 1) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }

                            onCodeChange(code)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
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
}

