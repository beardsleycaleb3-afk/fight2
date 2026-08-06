package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ImmersiveDarkColorScheme = darkColorScheme(
    primary = ImmersivePrimary,
    secondary = ImmersiveSecondary,
    tertiary = ImmersiveTertiary,
    background = ImmersiveDarkBackground,
    surface = ImmersiveSurface,
    surfaceVariant = ImmersiveSurfaceVariant,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ImmersiveDarkColorScheme,
        typography = Typography,
        content = content
    )
}
