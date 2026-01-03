package com.example.booknest.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SkyBluePeriwinkle,
    secondary = LightBlueGray,
    tertiary = LightBlueGray,
    primaryContainer = SkyBluePeriwinkle.copy(alpha = 0.3f),
    secondaryContainer = LightBlueGray.copy(alpha = 0.3f),
    tertiaryContainer = LightBlueGray.copy(alpha = 0.3f),
    background = VeryDarkNavy,
    surface = DarkTealSlate,
    surfaceVariant = DarkTealSlate.copy(alpha = 0.8f),
    onPrimary = Color.White,
    onSecondary = VeryDarkNavy,
    onTertiary = Color.White,
    onPrimaryContainer = SkyBluePeriwinkle,
    onSecondaryContainer = LightBlueGray,
    onTertiaryContainer = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = LightBlueGray
)

private val LightColorScheme = lightColorScheme(
    primary = DarkNavyBlue,
    secondary = SkyBluePeriwinkle,
    tertiary = LightBlueGray,
    primaryContainer = SkyBluePeriwinkle.copy(alpha = 0.2f),
    secondaryContainer = SkyBluePeriwinkle.copy(alpha = 0.3f),
    tertiaryContainer = LightBlueGray.copy(alpha = 0.3f),
    background = BackgroundWhite,
    surface = BackgroundWhite,
    surfaceVariant = LightGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = DarkNavyBlue,
    onPrimaryContainer = DarkNavyBlue,
    onSecondaryContainer = DarkNavyBlue,
    onTertiaryContainer = DarkNavyBlue,
    onBackground = DarkNavyBlue,
    onSurface = DarkNavyBlue,
    onSurfaceVariant = DarkNavyBlue
)

@Composable
fun BookNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}