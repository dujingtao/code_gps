package com.codegps.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codegps.app.R
import com.codegps.app.location.GpsReading
import com.codegps.app.location.SatelliteInfo
import com.codegps.app.location.metersPerSecondToKmh
import com.codegps.app.ui.components.GlassSurface
import com.codegps.app.ui.components.ReadoutCard
import com.codegps.app.ui.components.SatelliteSignalList
import com.codegps.app.ui.components.SatelliteSkyPlot
import com.codegps.app.ui.components.SatelliteSummary
import com.codegps.app.ui.components.SatelliteUsedLegend
import com.codegps.app.ui.components.StatusChip
import com.codegps.app.ui.theme.NeonCyan
import com.codegps.app.ui.theme.NeonViolet
import com.codegps.app.ui.theme.SpaceBlackBottom
import com.codegps.app.ui.theme.SpaceBlackMid
import com.codegps.app.ui.theme.SpaceBlackTop
import com.codegps.app.ui.theme.TextPrimary
import com.codegps.app.ui.theme.TextSecondary
import com.codegps.app.ui.theme.accuracyStatusColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Whether the runtime location permission has been resolved yet, and if so
 * how. Kept separate from [GpsReading] because "no reading yet" and
 * "permission denied" require different UI treatment.
 */
enum class LocationPermissionStatus {
    UNKNOWN,
    GRANTED,
    DENIED,
}

/** Background gradient shared by every state of the screen. */
private val HudBackground = Brush.verticalGradient(
    listOf(SpaceBlackTop, SpaceBlackMid, SpaceBlackBottom),
)

/**
 * Minimum window width, in dp, treated as "wide" (a tablet, or a foldable's
 * unfolded inner display) rather than "compact" (a phone, or a foldable's
 * cover screen). This is the same breakpoint Material's window-size-class
 * uses for the compact→medium boundary; it's applied directly against
 * [LocalConfiguration.screenWidthDp] here rather than pulling in the
 * separate `material3-window-size-class` artifact, since the layout only
 * needs this one width check.
 */
private const val WIDE_LAYOUT_MIN_WIDTH_DP = 600

/**
 * Root screen: a dark "HUD" style GPS readout. Shows a permission prompt
 * until location access is granted, then a satellite sky plot with a
 * per-constellation count summary and signal-strength list, plus live
 * position/speed/altitude/accuracy readout cards — all continuously
 * updated from [reading] and [satellites]. Below [WIDE_LAYOUT_MIN_WIDTH_DP]
 * the satellite geometry stays pinned at the top while everything else
 * scrolls beneath it; at or above it, the screen switches to a two-pane
 * layout (geometry fixed on the left, everything else scrolling on the
 * right) so a wide/unfolded display isn't left with a narrow column and
 * empty space (see [CompactHudLayout] / [WideHudLayout]).
 */
@Composable
fun LocationScreen(
    permissionStatus: LocationPermissionStatus,
    reading: GpsReading?,
    satellites: List<SatelliteInfo>,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground),
    ) {
        when (permissionStatus) {
            LocationPermissionStatus.UNKNOWN -> PermissionPrompt(
                body = stringResource(R.string.permission_rationale_body),
                onRequestPermission = onRequestPermission,
            )
            LocationPermissionStatus.DENIED -> PermissionPrompt(
                body = stringResource(R.string.permission_denied_message),
                onRequestPermission = onRequestPermission,
            )
            LocationPermissionStatus.GRANTED -> HudContent(reading = reading, satellites = satellites)
        }
    }
}

@Composable
private fun PermissionPrompt(body: String, onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GlassSurface(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.permission_rationale_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = body, color = TextSecondary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                ) {
                    Text(text = stringResource(R.string.permission_grant_button), color = SpaceBlackTop)
                }
            }
        }
    }
}

@Composable
private fun HudContent(reading: GpsReading?, satellites: List<SatelliteInfo>) {
    val isSearching = reading == null
    val usedCount = satellites.count { it.usedInFix }
    val totalCount = satellites.size

    // Fold the satellite count into the status label once GNSS data starts
    // arriving; fall back to the plain label if no satellites are visible
    // yet (e.g. right after permission is granted).
    val statusLabel = when {
        !isSearching && totalCount > 0 ->
            stringResource(R.string.status_locked_with_satellites, usedCount, totalCount)
        !isSearching -> stringResource(R.string.status_locked)
        isSearching && totalCount > 0 -> stringResource(R.string.status_searching_with_satellites, totalCount)
        else -> stringResource(R.string.status_searching)
    }
    val statusColor = if (isSearching) NeonCyan else accuracyStatusColor(reading?.accuracyMeters)
    val isWideScreen = LocalConfiguration.current.screenWidthDp >= WIDE_LAYOUT_MIN_WIDTH_DP

    if (isWideScreen) {
        WideHudLayout(
            reading = reading,
            satellites = satellites,
            statusLabel = statusLabel,
            statusColor = statusColor,
            isSearching = isSearching,
        )
    } else {
        CompactHudLayout(
            reading = reading,
            satellites = satellites,
            statusLabel = statusLabel,
            statusColor = statusColor,
            isSearching = isSearching,
        )
    }
}

/**
 * Fixed sky-plot header on top, everything else scrolling underneath. Used
 * below [WIDE_LAYOUT_MIN_WIDTH_DP] (phones, and a foldable's folded/cover
 * screen) where there isn't enough width for a useful side-by-side split.
 *
 * The satellite geometry (status/summary/sky plot/legend) doesn't scroll —
 * it's the thing you glance at, not read line by line, so pinning it means
 * it's always visible while flicking through the numeric readouts below,
 * rather than scrolling out of view along with everything else.
 */
@Composable
private fun CompactHudLayout(
    reading: GpsReading?,
    satellites: List<SatelliteInfo>,
    statusLabel: String,
    statusColor: Color,
    isSearching: Boolean,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StatusChip(label = statusLabel, color = statusColor, isPulsing = isSearching)
            Spacer(modifier = Modifier.height(16.dp))
            SatelliteSummary(satellites = satellites)
            Spacer(modifier = Modifier.height(16.dp))
            SatelliteSkyPlot(satellites = satellites)
            Spacer(modifier = Modifier.height(10.dp))
            SatelliteUsedLegend()
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SatelliteSignalList(
                satellites = satellites,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            LatLonCards(reading)
            Spacer(modifier = Modifier.height(20.dp))
            SpeedSection(reading)
            Spacer(modifier = Modifier.height(20.dp))
            AltitudeAccuracyCards(reading)
            Spacer(modifier = Modifier.height(12.dp))
            LastUpdatedCard(reading)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Two-pane layout used at or above [WIDE_LAYOUT_MIN_WIDTH_DP] (a tablet, or
 * a foldable's unfolded inner display): the left pane holds satellite
 * geometry (status, per-constellation summary, sky plot) and — like
 * [CompactHudLayout]'s header — doesn't scroll, since it's a glanceable
 * picture rather than a list of values to read through. The right pane holds
 * the signal-strength list and every numeric readout and scrolls on its own,
 * instead of the single narrow column v0.3.0 used — which left the bottom of
 * a wide screen empty.
 */
@Composable
private fun WideHudLayout(
    reading: GpsReading?,
    satellites: List<SatelliteInfo>,
    statusLabel: String,
    statusColor: Color,
    isSearching: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            StatusChip(label = statusLabel, color = statusColor, isPulsing = isSearching)
            Spacer(modifier = Modifier.height(16.dp))
            SatelliteSummary(satellites = satellites)
            Spacer(modifier = Modifier.height(20.dp))
            SatelliteSkyPlot(satellites = satellites)
            Spacer(modifier = Modifier.height(10.dp))
            SatelliteUsedLegend()
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            SatelliteSignalList(
                satellites = satellites,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            LatLonCards(reading)
            Spacer(modifier = Modifier.height(20.dp))
            SpeedSection(reading)
            Spacer(modifier = Modifier.height(20.dp))
            AltitudeAccuracyCards(reading)
            Spacer(modifier = Modifier.height(12.dp))
            LastUpdatedCard(reading)
        }
    }
}

/** Latitude/longitude readout cards — shared by both HUD layouts. */
@Composable
private fun LatLonCards(reading: GpsReading?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ReadoutCard(
            accentColor = NeonCyan,
            label = stringResource(R.string.label_latitude),
            value = reading?.let { "%.5f°".format(it.latitude) } ?: "--",
            modifier = Modifier.weight(1f),
        )
        ReadoutCard(
            accentColor = NeonCyan,
            label = stringResource(R.string.label_longitude),
            value = reading?.let { "%.5f°".format(it.longitude) } ?: "--",
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Speed readouts, km/h and m/s side by side — shared by both HUD layouts.
 * Previously paired with a semi-circular gauge; the gauge is gone as of
 * v0.5.2 — at typical walking/indoor-test speeds it sat almost empty and
 * added visual weight without adding legibility, so speed now gets the same
 * plain numeric-card treatment as every other readout instead of its own
 * graphic.
 */
@Composable
private fun SpeedSection(reading: GpsReading?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ReadoutCard(
            accentColor = NeonCyan,
            label = stringResource(R.string.label_speed_kmh),
            value = reading?.let { "%.1f".format(it.speedMetersPerSecond.metersPerSecondToKmh()) } ?: "--",
            modifier = Modifier.weight(1f),
        )
        ReadoutCard(
            accentColor = NeonCyan,
            label = stringResource(R.string.label_speed_ms),
            value = reading?.let { "%.1f".format(it.speedMetersPerSecond) } ?: "--",
            modifier = Modifier.weight(1f),
        )
    }
}

/** Altitude/accuracy readout cards — shared by both HUD layouts. */
@Composable
private fun AltitudeAccuracyCards(reading: GpsReading?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ReadoutCard(
            accentColor = NeonViolet,
            label = stringResource(R.string.label_altitude),
            value = reading?.let { "%.1f m".format(it.altitudeMeters) } ?: "--",
            modifier = Modifier.weight(1f),
        )
        ReadoutCard(
            accentColor = accuracyStatusColor(reading?.accuracyMeters),
            label = stringResource(R.string.label_accuracy),
            value = reading?.let { "±%.1f m".format(it.accuracyMeters) } ?: "--",
            modifier = Modifier.weight(1f),
        )
    }
}

/** Last-updated timestamp card — shared by both HUD layouts. */
@Composable
private fun LastUpdatedCard(reading: GpsReading?) {
    ReadoutCard(
        accentColor = TextSecondary,
        label = stringResource(R.string.label_updated_at),
        value = reading?.let { formatTimestamp(it.timestampMillis) } ?: "--",
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(epochMillis))
