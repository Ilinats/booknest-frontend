package com.example.booknest.ui.auth.components.models

import androidx.compose.ui.graphics.Color

data class PasswordStrength(
    val label: String,
    val strength: Float,
    val color: Color
)

