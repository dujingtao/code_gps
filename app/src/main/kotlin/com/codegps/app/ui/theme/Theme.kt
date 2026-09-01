package com.codegps.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Fixed dark "HUD" color scheme. The app intentionally has no light variant
 * yet — it is meant to look like a satellite-tracker instrument panel, which
 * only makes sense on a dark background. A light theme could be added later
 * by branching on [androidx.compose.foundation.isSystemInDarkTheme] if
 * requested.
 */
private val HudColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonViolet,
    background = SpaceBlackMid,
    surface = SpaceBlackMid,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = StatusPoor,
)

/** App-wide Compose theme: [HudColorScheme] plus [CodeGpsTypography]. */
@Composable
fun CodeGpsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HudColorScheme,
        typography = CodeGpsTypography,
        content = content,
    )
}
