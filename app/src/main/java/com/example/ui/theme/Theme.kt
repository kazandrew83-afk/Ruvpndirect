package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF8CF4FF),

    secondary = NeonEmerald,
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005328),
    onSecondaryContainer = Color(0xFF70FF9D),

    tertiary = ElectricBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF0043A8),
    onTertiaryContainer = Color(0xFFD9E2FF),

    background = CyberNavyDark,
    onBackground = TextPrimary,

    surface = CyberNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberNavySurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = CyberNavyCardBorder,
    outlineVariant = Color(0xFF1F3250),

    error = DangerRed,
    onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Keep consistent cyber dark aesthetic for VPN utility

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep tailored brand security palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
