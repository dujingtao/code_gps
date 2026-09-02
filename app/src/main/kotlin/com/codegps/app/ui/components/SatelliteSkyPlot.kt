package com.codegps.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegps.app.R
import com.codegps.app.location.SatelliteInfo
import com.codegps.app.ui.theme.NeonCyan
import com.codegps.app.ui.theme.ReadoutLabelStyle
import com.codegps.app.ui.theme.TextSecondary
import com.codegps.app.ui.theme.color
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Elevation angles (degrees above horizon) the sky plot draws reference rings at. */
private val ELEVATION_RING_DEGREES = listOf(0f, 30f, 60f)

/** Overall size of the sky-plot canvas. */
private val SKY_PLOT_SIZE_DP = 248.dp

/** Radius, in dp, of a satellite dot that is currently used in the position fix. */
private const val USED_DOT_RADIUS_DP = 7f

/** Radius, in dp, of a satellite dot that is visible but not used in the fix. */
private const val UNUSED_DOT_RADIUS_DP = 5.5f

/** Gap, in dp, between a used-in-fix dot's edge and its "lock" halo ring. */
private const val USED_HALO_GAP_DP = 3f

/**
 * A polar "sky plot" / dome view of every visible GNSS satellite: each dot
 * is placed by (azimuth, elevation), the same projection dedicated GPS-test
 * apps use. Zenith (directly overhead, elevation 90°) is the center of the
 * plot and the horizon (elevation 0°) is the outer rim, so a dot's distance
 * from center shows how low it sits in the sky, and its angle clockwise from
 * straight up shows compass azimuth (0°=N, 90°=E, ...).
 *
 * Dots are colored per constellation (see
 * [com.codegps.app.ui.theme.color]). A satellite actually used in the
 * position fix ([SatelliteInfo.usedInFix]) gets a solid dot plus a "lock"
 * halo ring drawn around it — an unambiguous marker, not just a subtler
 * fill — while a satellite that is only visible renders as a smaller hollow
 * ring with no halo. [SatelliteUsedLegend] spells this out in text.
 */
@Composable
fun SatelliteSkyPlot(
    satellites: List<SatelliteInfo>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = TextSecondary, fontSize = 11.sp)

    Canvas(modifier = modifier.size(SKY_PLOT_SIZE_DP)) {
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
            // stay the primary signal, so the fraction never drops low
            // enough to make a weak-signal satellite effectively invisible.
            val signalFraction = satellite.cn0DbHz.toSignalFraction()

            if (satellite.usedInFix) {
                val dotRadius = USED_DOT_RADIUS_DP.dp.toPx() * signalFraction

                // "Lock" halo: the primary visual marker for "this satellite
                // is factored into the position fix," independent of the
                // solid-vs-hollow fill so it reads clearly even at a glance.
                drawCircle(
                    color = dotColor.copy(alpha = 0.9f),
                    radius = dotRadius + USED_HALO_GAP_DP.dp.toPx(),
                    center = dotCenter,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
                drawCircle(
                    color = dotColor.copy(alpha = 0.5f + 0.5f * signalFraction),
                    radius = dotRadius,
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
 * Small caption explaining the "lock" halo ring [SatelliteSkyPlot] draws
 * around used-in-fix satellites (and the matching checkmark badge in
 * [SatelliteSignalList]) — both views share the same visual language, so one
 * legend, drawn the same way as the halo itself, covers both.
 */
@Composable
fun SatelliteUsedLegend(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = NeonCyan, radius = 3.dp.toPx(), center = center)
            drawCircle(
                color = NeonCyan.copy(alpha = 0.9f),
                radius = 3.dp.toPx() + USED_HALO_GAP_DP.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.satellite_used_legend),
            style = ReadoutLabelStyle,
            color = TextSecondary,
        )
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
