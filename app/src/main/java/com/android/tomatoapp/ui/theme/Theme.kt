package com.android.tomatoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary Colors
val RedPrimary = Color(0xFFD93025)
val RedDark = Color(0xFF9B1C1C)
val RedLight = Color(0xFFFDECEA)

// Green Colors
val GreenPrimary = Color(0xFF2D7A3A)
val GreenAccent = Color(0xFF3DBE5A)
val GreenLight = Color(0xFFE6F4EA)
val GreenSave = Color(0xFF38A315)

// Orange & Blue
val Orange = Color(0xFFE67E22)
val OrangeLight = Color(0xFFFEF3E2)
val Blue = Color(0xFF1976D2)
val BlueLight = Color(0xFFE3F2FD)

// Neutral Colors
val CreamBackground = Color(0xFFFFF9F5)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1A1A1A)
val TextMuted = Color(0xFF6B7280)
val Border = Color(0x14000000) // rgba(0,0,0,0.08)

// Dark Green for Disease header
val DarkGreen = Color(0xFF1B5E20)
val DarkGreenBackground = Color(0xFF0D3E12)

private val lightColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,
    primaryContainer = RedLight,
    onPrimaryContainer = RedDark,
    secondary = GreenPrimary,
    onSecondary = Color.White,
    secondaryContainer = GreenLight,
    onSecondaryContainer = GreenPrimary,
    tertiary = Orange,
    onTertiary = Color.White,
    error = RedPrimary,
    onError = Color.White,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    outline = Border,
)

@Composable
fun TomatoAppTheme(
    darkTheme: Boolean = isSystemInDarkMode(),
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TomatoTypography,
        shapes = TomatoShapes,
        content = content
    )
}
