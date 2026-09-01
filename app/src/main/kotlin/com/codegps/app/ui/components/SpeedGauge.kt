package com.codegps.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.codegps.app.ui.theme.NeonCyan
import com.codegps.app.ui.theme.NeonViolet
import com.codegps.app.ui.theme.TextSecondary

/** Upper bound of the gauge scale; speeds above this still render as "full". */
private const val GAUGE_MAX_SPEED_MPS = 30f

/** Gauge sweep starts at 150° (lower-left) ... */
private const val GAUGE_START_ANGLE = 150f

/** ...and spans 240° round to the lower-right, leaving a bottom gap. */
private const val GAUGE_SWEEP_ANGLE = 240f

/**
 * A semi-circular speed gauge: a faint full-range track plus a cyan→violet
 * arc filled proportionally to [speedMetersPerSecond] against a fixed
 * [GAUGE_MAX_SPEED_MPS] scale (about 108 km/h, a sensible ceiling for a
 * pedestrian/vehicle GPS readout).
 */
@Composable
fun SpeedGauge(
    speedMetersPerSecond: Float,
    modifier: Modifier = Modifier,
) {
    val fraction = (speedMetersPerSecond / GAUGE_MAX_SPEED_MPS).coerceIn(0f, 1f)

    Canvas(modifier = modifier.size(140.dp)) {
        val strokeWidth = 10.dp.toPx()
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

        drawArc(
            color = TextSecondary.copy(alpha = 0.2f),
            startAngle = GAUGE_START_ANGLE,
            sweepAngle = GAUGE_SWEEP_ANGLE,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        if (fraction > 0f) {
            drawArc(
                brush = Brush.sweepGradient(listOf(NeonCyan, NeonViolet)),
                startAngle = GAUGE_START_ANGLE,
                sweepAngle = GAUGE_SWEEP_ANGLE * fraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
    }
}
