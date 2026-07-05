package com.birdytalk.seedco.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ---- Earthy-luxury brand palette ---------------------------------------------------------------

/** Deep forest green — primary containers and dark backgrounds. */
val ForestGreen = Color(0xFF1E3A2B)
val ForestGreenDeep = Color(0xFF152A20)
val ForestGreenRaised = Color(0xFF24432F)

/** Warm cream — light surfaces and dark-theme text. */
val Cream = Color(0xFFF4ECD8)
val CreamRaised = Color(0xFFFBF6E9)
val CreamSunk = Color(0xFFE9DFC4)

/** Muted gold — accents and highlights. */
val MutedGold = Color(0xFFC9A34E)
val MutedGoldSoft = Color(0xFFD9BE7E)
val MutedGoldDeep = Color(0xFF8A6D2E)

/** Charcoal — high-contrast text on light surfaces. */
val Charcoal = Color(0xFF2A2622)
val CharcoalSoft = Color(0xFF4A443C)

// Soft-amber warning surface (used by the capacity-overflow banner in both themes).
val AmberContainerDark = Color(0xFF3D3316)
val AmberContainerLight = Color(0xFFFBEFC9)
val AmberBorder = Color(0xFFCF9F3A)
val AmberOnContainerDark = Color(0xFFF3D98A)
val AmberOnContainerLight = Color(0xFF6B4E12)

// ---- Material 3 color schemes ------------------------------------------------------------------

val SeedCoDarkColors = darkColorScheme(
    primary = MutedGold,
    onPrimary = Charcoal,
    primaryContainer = ForestGreen,
    onPrimaryContainer = Cream,
    secondary = MutedGoldSoft,
    onSecondary = Charcoal,
    secondaryContainer = ForestGreenRaised,
    onSecondaryContainer = Cream,
    tertiary = MutedGoldSoft,
    onTertiary = Charcoal,
    background = ForestGreenDeep,
    onBackground = Cream,
    surface = ForestGreen,
    onSurface = Cream,
    surfaceVariant = ForestGreenRaised,
    onSurfaceVariant = Color(0xFFCFC6AE),
    outline = Color(0xFF7C866E),
    outlineVariant = Color(0xFF3A4A38),
    error = Color(0xFFE29B7A),
    onError = Charcoal,
)

val SeedCoLightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Cream,
    primaryContainer = Color(0xFFDDE7D8),
    onPrimaryContainer = ForestGreenDeep,
    secondary = MutedGoldDeep,
    onSecondary = Cream,
    secondaryContainer = Color(0xFFEFE3C4),
    onSecondaryContainer = Color(0xFF3E300F),
    tertiary = MutedGoldDeep,
    onTertiary = Cream,
    background = Cream,
    onBackground = Charcoal,
    surface = CreamRaised,
    onSurface = Charcoal,
    surfaceVariant = CreamSunk,
    onSurfaceVariant = CharcoalSoft,
    outline = Color(0xFFB6A98A),
    outlineVariant = Color(0xFFD8CBAA),
    error = Color(0xFF9C4221),
    onError = Cream,
)
