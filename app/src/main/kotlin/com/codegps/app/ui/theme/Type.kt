package com.codegps.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material typography for regular UI text (titles, body copy). Live numeric
 * GPS readouts do not use this scale — see [ReadoutValueStyle].
 */
val CodeGpsTypography = Typography()

/**
 * Bold monospace style used for every live numeric GPS readout (lat/lon,
 * speed, altitude, accuracy). Monospace + wide letter-spacing makes the
 * digits read like a digital instrument display rather than form text.
 */
val ReadoutValueStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    letterSpacing = 1.2.sp,
)

/** Small, muted, wide-tracked label style paired with [ReadoutValueStyle]. */
val ReadoutLabelStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    letterSpacing = 1.5.sp,
)
