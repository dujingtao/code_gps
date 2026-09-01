package com.codegps.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codegps.app.R
import com.codegps.app.location.GpsReading
import com.codegps.app.ui.components.GlassSurface
import com.codegps.app.ui.components.RadarIndicator
import com.codegps.app.ui.components.ReadoutCard
import com.codegps.app.ui.components.SpeedGauge
import com.codegps.app.ui.components.StatusChip
import com.codegps.app.ui.theme.NeonCyan
import com.codegps.app.ui.theme.NeonViolet
import com.codegps.app.ui.theme.ReadoutLabelStyle
import com.codegps.app.ui.theme.ReadoutValueStyle
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
 * Root screen: a dark "HUD" style GPS readout. Shows a permission prompt
 * until location access is granted, then a radar-style position indicator
 * plus live instrument cards for altitude/accuracy/last-update and a speed
 * gauge, all continuously updated from [reading].
 */
@Composable
fun LocationScreen(
    permissionStatus: LocationPermissionStatus,
    reading: GpsReading?,
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
            LocationPermissionStatus.GRANTED -> HudContent(reading = reading)
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
private fun HudContent(reading: GpsReading?) {
    val isSearching = reading == null
    val statusLabel = stringResource(
        if (isSearching) R.string.status_searching else R.string.status_locked,
    )
    val statusColor = if (isSearching) NeonCyan else accuracyStatusColor(reading?.accuracyMeters)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StatusChip(label = statusLabel, color = statusColor, isPulsing = isSearching)

        Spacer(modifier = Modifier.height(20.dp))

        Box(contentAlignment = Alignment.Center) {
            RadarIndicator(accuracyMeters = reading?.accuracyMeters, isSearching = isSearching)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CoordinateReadout(
                    label = stringResource(R.string.label_latitude),
                    value = reading?.let { "%.5f°".format(it.latitude) } ?: "--",
                )
                Spacer(modifier = Modifier.height(6.dp))
                CoordinateReadout(
                    label = stringResource(R.string.label_longitude),
                    value = reading?.let { "%.5f°".format(it.longitude) } ?: "--",
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SpeedGauge(speedMetersPerSecond = reading?.speedMetersPerSecond ?: 0f)
        Spacer(modifier = Modifier.height(4.dp))
        AnimatedReadoutText(reading?.let { "%.1f m/s".format(it.speedMetersPerSecond) } ?: "-- m/s")
        Text(
            text = stringResource(R.string.label_speed),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        ReadoutCard(
            accentColor = TextSecondary,
            label = stringResource(R.string.label_updated_at),
            value = reading?.let { formatTimestamp(it.timestampMillis) } ?: "--",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** A small muted [label] beside an animated coordinate [value] (lat or lon). */
@Composable
private fun CoordinateReadout(label: String, value: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = label,
            style = ReadoutLabelStyle,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedReadoutText(value)
    }
}

/**
 * Wraps [text] in a vertical slide/fade transition, keyed on the string
 * itself, so every readout animates like an odometer digit instead of
 * snapping when a new GPS update arrives.
 */
@Composable
private fun AnimatedReadoutText(text: String) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            (slideInVertically(tween(220)) { it } + fadeIn(tween(220)))
                .togetherWith(slideOutVertically(tween(220)) { -it } + fadeOut(tween(220)))
        },
        label = "readout_text",
    ) { animated ->
        Text(text = animated, style = ReadoutValueStyle, color = TextPrimary)
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(epochMillis))
