package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    secondary = LaserRed,
    tertiary = BioGreen,
    background = CyberBlack,
    surface = SpaceSurface,
    onPrimary = CyberBlack,
    onSecondary = SpaceWhite,
    onTertiary = CyberBlack,
    onBackground = SpaceWhite,
    onSurface = SpaceWhite,
    primaryContainer = CardSurface,
    onPrimaryContainer = SpaceWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force custom dark cyber theme for consistent VR gaming visuals
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
