package com.codegps.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codegps.app.location.SatelliteInfo
import com.codegps.app.ui.theme.ReadoutLabelStyle
import com.codegps.app.ui.theme.StatusGood
import com.codegps.app.ui.theme.TextPrimary
import com.codegps.app.ui.theme.TextSecondary
import com.codegps.app.ui.theme.color
import com.codegps.app.ui.theme.shortPrefix

/**
 * Scrollable list of every visible satellite's signal strength (C/N0,
 * dB-Hz), sorted strongest-first — the classic "GPS test app" bar list.
 * Each row shows a constellation-colored dot, a short id (e.g. "G12"), a bar
 * filled proportional to [SatelliteInfo.cn0DbHz] (see [toSignalFraction]),
 * the raw dB-Hz value, and a checkmark badge when the satellite is used in
 * the position fix — the same "used" marker language as
 * [SatelliteSkyPlot]'s halo ring.
 *
 * Renders nothing when [satellites] is empty, matching [SatelliteSummary]'s
 * graceful-degradation pattern.
 */
@Composable
fun SatelliteSignalList(
    satellites: List<SatelliteInfo>,
    modifier: Modifier = Modifier,
) {
    if (satellites.isEmpty()) return

    val sorted = satellites.sortedByDescending { it.cn0DbHz }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // No custom `key` here: a single physical satellite can appear as two
        // separate GnssStatus entries with the *same* constellation+svid when
        // a device tracks it on multiple frequency bands (e.g. L1+L5 dual-
        // frequency GNSS, common on modern flagships) — a "constellation-svid"
        // key would then collide and LazyColumn throws immediately. Falling
        // back to the default position-based key avoids that; the only cost
        // is losing per-row scroll-position identity across re-sorts, which
        // doesn't matter for a read-only signal list.
        items(sorted) { satellite ->
            SatelliteSignalRow(satellite)
        }
    }
}

@Composable
private fun SatelliteSignalRow(satellite: SatelliteInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(satellite.constellation.color()),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${satellite.constellation.shortPrefix()}${satellite.svid}",
            style = ReadoutLabelStyle,
            color = TextSecondary,
            modifier = Modifier.width(34.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        SignalBar(
            fraction = satellite.cn0DbHz.toSignalFraction(),
            color = satellite.constellation.color(),
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "%.1f".format(satellite.cn0DbHz),
            style = ReadoutLabelStyle,
            color = TextPrimary,
            modifier = Modifier.width(38.dp),
            textAlign = TextAlign.End,
        )
        Spacer(modifier = Modifier.width(8.dp))
        UsedBadge(usedInFix = satellite.usedInFix)
    }
}

/** Horizontal track filled proportional to [fraction], in [color]. */
@Composable
private fun SignalBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(TextSecondary.copy(alpha = 0.15f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(4.dp))
                .background(color),
        )
    }
}

/**
 * Checkmark badge marking a satellite as used in the position fix — reserves
 * a fixed-width slot even when hidden, so unused-satellite rows don't shift
 * the columns to their left.
 */
@Composable
private fun UsedBadge(usedInFix: Boolean) {
    Box(modifier = Modifier.width(18.dp)) {
        if (usedInFix) {
            Text(text = "✓", style = ReadoutLabelStyle, color = StatusGood)
        }
    }
}
