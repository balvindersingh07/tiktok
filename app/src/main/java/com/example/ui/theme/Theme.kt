package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TashanRainbowColorScheme = darkColorScheme(
    primary = RainbowPink,
    onPrimary = TikTokWhite,
    secondary = RainbowCyan,
    onSecondary = TashanMidnight,
    tertiary = RainbowYellow,
    background = TashanMidnight,
    onBackground = TikTokWhite,
    surface = TashanDarkSurface,
    onSurface = TikTokWhite,
    surfaceVariant = TashanDarkCard,
    onSurfaceVariant = TikTokWhite80,
    outline = TashanDarkBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TashanRainbowColorScheme,
        typography = Typography,
        content = content
    )
}
