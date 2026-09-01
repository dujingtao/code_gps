package com.codegps.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.codegps.app.R
import com.codegps.app.location.GpsReading
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

/**
 * Root screen: shows a permission request prompt until location access is
 * granted, then switches to a live view of the most recent [GpsReading].
 */
@Composable
fun LocationScreen(
    permissionStatus: LocationPermissionStatus,
    reading: GpsReading?,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (permissionStatus) {
                LocationPermissionStatus.UNKNOWN -> PermissionRationale(onRequestPermission)
                LocationPermissionStatus.DENIED -> Text(text = stringResource(R.string.permission_denied_message))
                LocationPermissionStatus.GRANTED -> ReadingCard(reading)
            }
        }
    }
}

@Composable
private fun PermissionRationale(onRequestPermission: () -> Unit) {
    Text(
        text = stringResource(R.string.permission_rationale_title),
        style = MaterialTheme.typography.titleLarge,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(R.string.permission_rationale_body))
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onRequestPermission) {
        Text(text = stringResource(R.string.permission_grant_button))
    }
}

@Composable
private fun ReadingCard(reading: GpsReading?) {
    if (reading == null) {
        Text(text = stringResource(R.string.location_waiting))
        return
    }

    Card(modifier = Modifier.fillMaxWidth(), elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            LabeledRow(stringResource(R.string.label_latitude), "%.6f°".format(reading.latitude))
            LabeledRow(stringResource(R.string.label_longitude), "%.6f°".format(reading.longitude))
            LabeledRow(stringResource(R.string.label_accuracy), "%.1f".format(reading.accuracyMeters))
            LabeledRow(stringResource(R.string.label_altitude), "%.1f".format(reading.altitudeMeters))
            LabeledRow(stringResource(R.string.label_speed), "%.2f".format(reading.speedMetersPerSecond))
            LabeledRow(stringResource(R.string.label_updated_at), formatTimestamp(reading.timestampMillis))
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String, contentPadding: PaddingValues = PaddingValues(vertical = 6.dp)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(epochMillis))
