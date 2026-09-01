package com.codegps.app.ui.theme

import androidx.compose.ui.graphics.Color

/** GPS accuracy at or below this value (meters) is considered a precise fix. */
const val ACCURACY_GOOD_METERS = 10f

/** GPS accuracy at or below this value (meters) is considered a usable, moderate fix. */
const val ACCURACY_MODERATE_METERS = 30f

/**
 * Maps a GPS accuracy reading to a status color: precise = green, moderate =
 * amber, loose = red. `null` (no reading yet) maps to the muted secondary
 * text color. Shared by the status chip, the radar's accuracy ring, and the
 * accuracy readout card so all three agree on the same thresholds.
 */
fun accuracyStatusColor(accuracyMeters: Float?): Color = when {
    accuracyMeters == null -> TextSecondary
    accuracyMeters <= ACCURACY_GOOD_METERS -> StatusGood
    accuracyMeters <= ACCURACY_MODERATE_METERS -> StatusModerate
    else -> StatusPoor
}
