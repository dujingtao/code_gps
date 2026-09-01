package com.codegps.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.codegps.app.ui.theme.NeonCyan
import com.codegps.app.ui.theme.TextSecondary
import com.codegps.app.ui.theme.accuracyStatusColor
import kotlin.math.min

/** GPS accuracy (meters) mapped to the smallest visible accuracy ring. */
private const val ACCURACY_RING_MIN_METERS = 3f

/** GPS accuracy (meters) mapped to the largest visible accuracy ring. */
private const val ACCURACY_RING_MAX_METERS = 60f

/** Smallest fraction of the widget radius the accuracy ring can shrink to. */
private const val RING_MIN_FRACTION = 0.2f

/**
 * A radar-style HUD widget: a faint concentric grid, a rotating sweep wedge
 * while [isSearching] is true, and an outer ring whose radius/color encode
 * [accuracyMeters] — tighter and green means a precise fix, larger and
 * amber/red means a loose one, the same convention map apps use for a GPS
 * accuracy circle.
 */
@Composable
fun RadarIndicator(
    accuracyMeters: Float?,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_motion")

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "sweep_angle",
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1100), repeatMode = RepeatMode.Reverse),
        label = "pulse_alpha",
    )

    val ringColor = accuracyStatusColor(accuracyMeters)
    val ringFraction = accuracyRingFraction(accuracyMeters)

    Canvas(modifier = modifier.size(220.dp)) {
        val radius = min(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Static concentric grid, like a radar scope's range rings.
        listOf(0.35f, 0.6f, 0.85f, 1f).forEach { fraction ->
            drawCircle(
                color = TextSecondary.copy(alpha = 0.18f),
                radius = radius * fraction,
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )
        }

        // Rotating sweep wedge — only shown while waiting for a fix.
        if (isSearching) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, NeonCyan.copy(alpha = 0.55f), Color.Transparent),
                ),
                startAngle = sweepAngle,
                sweepAngle = 70f,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
            )
        }

        // Accuracy ring: radius/color encode the current fix quality.
        drawCircle(
            color = ringColor,
            radius = radius * ringFraction,
            center = center,
            style = Stroke(width = 3.dp.toPx()),
        )

        // Center marker: pulsing while searching, solid once a reading exists.
        drawCircle(
            color = ringColor.copy(alpha = if (isSearching) pulseAlpha else 1f),
            radius = 6.dp.toPx(),
            center = center,
        )
    }
}

/**
 * Maps GPS accuracy (meters) to a ring radius fraction of the widget,
 * clamped to [[RING_MIN_FRACTION], 1.0] so both very precise and very loose
 * fixes stay legible on screen. `null` (no reading yet) renders at the
 * largest radius, matching an "unknown / worst case" fix.
 */
private fun accuracyRingFraction(accuracyMeters: Float?): Float {
    val meters = accuracyMeters ?: return 1f
    val clamped = meters.coerceIn(ACCURACY_RING_MIN_METERS, ACCURACY_RING_MAX_METERS)
    val normalized = (clamped - ACCURACY_RING_MIN_METERS) / (ACCURACY_RING_MAX_METERS - ACCURACY_RING_MIN_METERS)
    return RING_MIN_FRACTION + normalized * (1f - RING_MIN_FRACTION)
}
