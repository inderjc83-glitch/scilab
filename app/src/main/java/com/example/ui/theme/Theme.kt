package com.example.ui.theme

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

val SciLabDarkColorScheme = darkColorScheme(
    primary = SciCyan,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = SciCyanLight,
    secondary = SciEmerald,
    onSecondary = Color(0xFF003915),
    secondaryContainer = Color(0xFF005322),
    onSecondaryContainer = Color(0xFFB9F6CA),
    tertiary = SciAmber,
    onTertiary = Color(0xFF332B00),
    background = SciNavyDark,
    onBackground = SciTextPrimaryDark,
    surface = SciSurfaceDark,
    onSurface = SciTextPrimaryDark,
    surfaceVariant = SciCardDark,
    onSurfaceVariant = SciTextSecondaryDark,
    error = SciCoral,
    onError = Color.White
)

val SciLabLightColorScheme = lightColorScheme(
    primary = SciLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF006064),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF004D40),
    tertiary = Color(0xFFE65100),
    onTertiary = Color.White,
    background = SciLightBackground,
    onBackground = SciLightTextPrimary,
    surface = SciLightSurface,
    onSurface = SciLightTextPrimary,
    surfaceVariant = SciLightCard,
    onSurfaceVariant = SciLightTextSecondary,
    error = SciCoral,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek futuristic dark science mode
    dynamicColor: Boolean = false, // Keep distinctive SciLab palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SciLabDarkColorScheme else SciLabLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
