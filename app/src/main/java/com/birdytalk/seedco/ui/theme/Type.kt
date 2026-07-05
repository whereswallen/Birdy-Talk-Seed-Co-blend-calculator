package com.birdytalk.seedco.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.birdytalk.seedco.R

private fun playfair(weight: Int) = Font(
    resId = R.font.playfair_display_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun mono(weight: Int) = Font(
    resId = R.font.jetbrains_mono_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Sophisticated serif for headers and blend titles. */
val PlayfairDisplay = FontFamily(
    playfair(400),
    playfair(500),
    playfair(600),
    playfair(700),
)

/**
 * Razor-sharp monospace for every number (weights and percentages). Fixed advance widths keep
 * counters from jittering as values animate.
 */
val JetBrainsMono = FontFamily(
    mono(400),
    mono(500),
    mono(600),
    mono(700),
)

/**
 * Ready-made style for large numeric readouts. Kept here so every numeric surface shares one
 * tabular, non-jittering look.
 */
val NumberDisplayStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.SemiBold,
    fontSize = 30.sp,
    letterSpacing = (-0.5).sp,
)

val NumberBodyStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
)

val SeedCoTypography = Typography(
    displayLarge = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 50.sp),
    displayMedium = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
