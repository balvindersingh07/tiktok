package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// Tashan Rainbow Vibrant Colorful Palette
// ============================================================================
val RainbowRed = Color(0xFFFF2A54)       // Electric Crimson
val RainbowOrange = Color(0xFFFF6D00)    // Vivid Sunset Orange
val RainbowAmber = Color(0xFFFFAB00)     // Warm Amber
val RainbowYellow = Color(0xFFFFD600)    // Radiant Solar Yellow
val RainbowLime = Color(0xFFAEEA00)      // Electric Lime
val RainbowGreen = Color(0xFF00E676)     // Neon Emerald Green
val RainbowTeal = Color(0xFF1DE9B6)      // Bright Aqua Teal
val RainbowCyan = Color(0xFF00F5D4)      // Vivid Electric Cyan
val RainbowBlue = Color(0xFF00B0FF)      // Bright Azure Blue
val RainbowIndigo = Color(0xFF651FFF)    // Deep Electric Indigo
val RainbowPurple = Color(0xFF7C4DFF)    // Vibrant Royal Purple
val RainbowViolet = Color(0xFF9D4EDD)    // Cosmic Violet
val RainbowPink = Color(0xFFFF2A85)      // Radiant Hot Pink / Magenta

// Core 8-color rainbow spectrum for gradients & borders
val RainbowSpectrum = listOf(
    RainbowRed,
    RainbowOrange,
    RainbowYellow,
    RainbowGreen,
    RainbowCyan,
    RainbowBlue,
    RainbowPurple,
    RainbowPink
)

// Convenient Rainbow Brushes
val RainbowHorizontalBrush = Brush.horizontalGradient(RainbowSpectrum)
val RainbowLinearBrush = Brush.linearGradient(RainbowSpectrum)
val RainbowSweepBrush = Brush.sweepGradient(RainbowSpectrum)
val RainbowAccentBrush = Brush.linearGradient(listOf(RainbowPink, RainbowOrange, RainbowYellow, RainbowCyan, RainbowPurple))

// Dark canvas backgrounds with subtle jewel-toned midnight violet tint
val TashanMidnight = Color(0xFF0C0A14)
val TashanDarkSurface = Color(0xFF161324)
val TashanDarkCard = Color(0xFF1F1B33)
val TashanDarkBorder = Color(0xFF322C52)

// Mapped alias constants to instantly bring the Rainbow Colorful Theme
// across all screens and components without changing any functional logic:
val TikTokBlack = TashanMidnight
val TikTokDarkSurface = TashanDarkSurface
val TikTokDarkCard = TashanDarkCard
val TikTokDarkBorder = TashanDarkBorder
val TikTokDarkGray = Color(0xFF1A162B)

val TikTokCyan = RainbowCyan
val TikTokPink = RainbowPink
val TikTokRed = RainbowRed
val TikTokGold = RainbowYellow
val TikTokOnlineGreen = RainbowGreen
val TikTokLivePulse = RainbowRed

val TikTokWhite = Color(0xFFFFFFFF)
val TikTokWhite80 = Color(0xCCFFFFFF)
val TikTokWhite60 = Color(0x99FFFFFF)
val TikTokWhite40 = Color(0x66FFFFFF)
val TikTokWhite20 = Color(0x33FFFFFF)
val TikTokWhite10 = Color(0x1AFFFFFF)

val TikTokGray = Color(0xFF9590B3)
val TikTokLightGray = Color(0xFFE4E1F0)

