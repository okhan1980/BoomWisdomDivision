package com.jomar.boomwisdomdivision.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CRTColorScheme = darkColorScheme(
    primary = CRTGlow,
    secondary = CRTGlowDim,
    tertiary = CRTText,
    background = CRTBackground,
    surface = CRTScreen,
    onPrimary = CRTBackground,
    onSecondary = CRTBackground,
    onTertiary = CRTBackground,
    onBackground = CRTText,
    onSurface = CRTText,
    outline = CRTFrame,
    surfaceVariant = CRTFrame
)

// Legacy color schemes (kept for compatibility)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun BoomWisdomDivisionTheme(
    darkTheme: Boolean = true, // Always use dark theme for CRT aesthetic
    // Dynamic color disabled for consistent CRT look
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always use CRT color scheme for consistent retro aesthetic
    val colorScheme = CRTColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
