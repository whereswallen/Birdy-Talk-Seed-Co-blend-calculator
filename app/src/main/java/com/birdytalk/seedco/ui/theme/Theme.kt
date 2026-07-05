package com.birdytalk.seedco.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Extra brand colors that don't map onto a standard Material role (e.g. the amber warning). */
data class SeedCoAccents(
    val warningContainer: Color,
    val onWarningContainer: Color,
    val warningBorder: Color,
    val gold: Color,
)

private val DarkAccents = SeedCoAccents(
    warningContainer = AmberContainerDark,
    onWarningContainer = AmberOnContainerDark,
    warningBorder = AmberBorder,
    gold = MutedGold,
)

private val LightAccents = SeedCoAccents(
    warningContainer = AmberContainerLight,
    onWarningContainer = AmberOnContainerLight,
    warningBorder = AmberBorder,
    gold = MutedGoldDeep,
)

/** Access the current brand accents anywhere under [SeedCoTheme] via `LocalSeedCoAccents.current`. */
val LocalSeedCoAccents = staticCompositionLocalOf { DarkAccents }

@Composable
fun SeedCoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SeedCoDarkColors else SeedCoLightColors
    val accents = if (darkTheme) DarkAccents else LightAccents

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalSeedCoAccents provides accents) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SeedCoTypography,
            content = content,
        )
    }
}
