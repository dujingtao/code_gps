package com.codegps.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Thin wrapper around [com.google.android.gms.location.FusedLocationProviderClient]
 * that exposes live location updates as a cold [Flow].
 *
 * Callers control the update lifecycle simply by collecting (or cancelling
 * collection of) the returned flow: subscribing registers a location
 * callback, and cancelling the collecting coroutine automatically
 * unregisters it via [awaitClose], so no manual start/stop bookkeeping is
 * needed at the call site.
 */
class LocationRepository(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Emits a new [GpsReading] every time the fused location provider
     * reports an update.
     *
     * Requires that ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION has
     * already been granted by the caller; the permission check itself is
     * done at the UI layer since only it knows how to prompt the user.
     */
    @SuppressLint("MissingPermission")
    fun observeLocationUpdates(): Flow<GpsReading> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(
                        GpsReading(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracyMeters = location.accuracy,
                            altitudeMeters = location.altitude,
                            speedMetersPerSecond = location.speed,
                            timestampMillis = location.time,
                        )
                    )
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // Runs when the collecting coroutine is cancelled (e.g. the screen
        // leaves composition or permission is revoked) — this is the single
        // place updates get torn down, so there is no separate stop() to
        // forget to call.
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    private companion object {
        const val UPDATE_INTERVAL_MS = 2000L
        const val MIN_UPDATE_INTERVAL_MS = 1000L
    }
}
