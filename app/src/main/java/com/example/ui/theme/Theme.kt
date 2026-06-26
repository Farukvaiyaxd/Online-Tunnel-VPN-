package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberpunkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberpunkBackground,
    secondary = NeonCyanVariant,
    onSecondary = CyberpunkBackground,
    background = CyberpunkBackground,
    onBackground = TextPrimary,
    surface = CyberpunkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberpunkDivider,
    onSurfaceVariant = TextSecondary,
    error = WarningRed,
    onError = TextPrimary,
    outline = CyberpunkDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force custom theme
    content: @Composable () -> Unit,
) {
    // Strictly Cyberpunk dark theme regardless of system
    val colorScheme = CyberpunkColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
