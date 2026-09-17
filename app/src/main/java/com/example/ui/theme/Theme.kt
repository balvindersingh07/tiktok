package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TikTokDarkColorScheme = darkColorScheme(
    primary = TikTokPink,
    onPrimary = TikTokWhite,
    secondary = TikTokCyan,
    onSecondary = TikTokBlack,
    tertiary = TikTokPink,
    background = TikTokBlack,
    onBackground = TikTokWhite,
    surface = TikTokDarkSurface,
    onSurface = TikTokWhite,
    surfaceVariant = TikTokDarkCard,
    onSurfaceVariant = TikTokWhite80,
    outline = TikTokDarkBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TikTokDarkColorScheme,
        typography = Typography,
        content = content
    )
}
