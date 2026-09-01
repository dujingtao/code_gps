package com.codegps.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegps.app.location.SatelliteInfo
import com.codegps.app.ui.theme.TextSecondary
import com.codegps.app.ui.theme.color
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Elevation angles (degrees above horizon) the sky plot draws reference rings at. */
private val ELEVATION_RING_DEGREES = listOf(0f, 30f, 60f)

/** Radius, in dp, of a satellite dot that is currently used in the position fix. */
private const val USED_DOT_RADIUS_DP = 5f

/** Radius, in dp, of a satellite dot that is visible but not used in the fix. */
private const val UNUSED_DOT_RADIUS_DP = 4f

/** Typical strong-signal ceiling (dB-Hz), used only to normalize dot size/alpha — not a hard limit. */
private const val MAX_EXPECTED_CN0_DB_HZ = 45f

/**
 * A polar "sky plot" / dome view of every visible GNSS satellite: each dot
 * is placed by (azimuth, elevation), the same projection dedicated GPS-test
 * apps use. Zenith (directly overhead, elevation 90°) is the center of the
 * plot and the horizon (elevation 0°) is the outer rim, so a dot's distance
 * from center shows how low it sits in the sky, and its angle clockwise from
 * straight up shows compass azimuth (0°=N, 90°=E, ...).
 *
 * Dots are colored per constellation (see
 * [com.codegps.app.ui.theme.color]) and rendered solid when
 * [SatelliteInfo.usedInFix] is true, or as a smaller hollow ring when the
 * satellite is only visible but not used — the same "in use" vs
 * "in view only" distinction every GPS-test app shows.
 */
@Composable
fun SatelliteSkyPlot(
    satellites: List<SatelliteInfo>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = TextSecondary, fontSize = 11.sp)

    Canvas(modifier = modifier.size(220.dp)) {
        val radius = min(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Elevation rings: horizon (0°) at the rim, zenith (90°) at center.
        ELEVATION_RING_DEGREES.forEach { elevation ->
            drawCircle(
                color = TextSecondary.copy(alpha = 0.18f),
                radius = radius * elevationToRadiusFraction(elevation),
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )
        }

        // Cardinal direction labels around the rim (0°=N at top, clockwise).
        listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f).forEach { (label, azimuth) ->
            val layout = textMeasurer.measure(label, style = labelStyle)
            val position = polarToOffset(center, radius + 12.dp.toPx(), azimuth)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = position.x - layout.size.width / 2f,
                    y = position.y - layout.size.height / 2f,
                ),
            )
        }

        // One dot per visible satellite, positioned by azimuth/elevation.
        satellites.forEach { satellite ->
            val dotCenter = polarToOffset(
                center = center,
                radius = radius * elevationToRadiusFraction(satellite.elevationDegrees),
                azimuthDegrees = satellite.azimuthDegrees,
            )
            val dotColor = satellite.constellation.color()

            // Stronger signal renders a touch bigger/brighter, but this is a
            // subtle modulation only — used/not-used + constellation color
            // stay the primary signal, so alpha never drops low enough to
            // make a weak-signal satellite effectively invisible.
            val signalFraction = (satellite.cn0DbHz / MAX_EXPECTED_CN0_DB_HZ).coerceIn(0.3f, 1f)

            if (satellite.usedInFix) {
                drawCircle(
                    color = dotColor.copy(alpha = 0.5f + 0.5f * signalFraction),
                    radius = USED_DOT_RADIUS_DP.dp.toPx() * signalFraction,
                    center = dotCenter,
                )
            } else {
                drawCircle(
                    color = dotColor.copy(alpha = 0.35f + 0.35f * signalFraction),
                    radius = UNUSED_DOT_RADIUS_DP.dp.toPx() * signalFraction,
                    center = dotCenter,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
    }
}

/**
 * Maps elevation (0° horizon, 90° zenith) to a 0..1 fraction of the plot
 * radius, with zenith at the center — the standard sky-plot projection.
 */
private fun elevationToRadiusFraction(elevationDegrees: Float): Float =
    1f - (elevationDegrees.coerceIn(0f, 90f) / 90f)

/**
 * Converts a compass azimuth (0°=N, clockwise) at the given [radius] from
 * [center] into a Cartesian [Offset], so 0° points straight up rather than
 * along the standard math "0° points right" convention.
 */
private fun polarToOffset(center: Offset, radius: Float, azimuthDegrees: Float): Offset {
    val radians = Math.toRadians(azimuthDegrees.toDouble())
    return Offset(
        x = center.x + radius * sin(radians).toFloat(),
        y = center.y - radius * cos(radians).toFloat(),
    )
}
