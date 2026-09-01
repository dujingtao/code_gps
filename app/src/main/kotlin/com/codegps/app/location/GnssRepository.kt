package com.codegps.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Wraps [LocationManager.registerGnssStatusCallback] to expose the live set
 * of visible GNSS satellites as a cold [Flow].
 *
 * Mirrors [LocationRepository.observeLocationUpdates]'s lifecycle exactly:
 * subscribing registers a status callback, and cancelling the collecting
 * coroutine automatically unregisters it via [awaitClose], so no manual
 * start/stop bookkeeping is needed at the call site.
 */
class GnssRepository(context: Context) {

    private val locationManager = context.getSystemService<LocationManager>()
    private val callbackHandler = Handler(Looper.getMainLooper())

    /**
     * Emits the full list of currently visible satellites every time the
     * platform reports an updated [GnssStatus] snapshot.
     *
     * Requires that ACCESS_FINE_LOCATION has already been granted by the
     * caller; as with [LocationRepository], the permission check itself is
     * done at the UI layer since only it knows how to prompt the user. If
     * the device unexpectedly has no [LocationManager] service, this emits
     * an empty list and completes rather than throwing.
     */
    @SuppressLint("MissingPermission")
    fun observeSatellites(): Flow<List<SatelliteInfo>> = callbackFlow {
        val manager = locationManager
        if (manager == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                trySend(status.toSatelliteInfoList())
            }
        }

        manager.registerGnssStatusCallback(callback, callbackHandler)

        // Runs when the collecting coroutine is cancelled (e.g. the screen
        // leaves composition or permission is revoked) — the single place
        // updates get torn down, matching LocationRepository's pattern.
        awaitClose { manager.unregisterGnssStatusCallback(callback) }
    }

    private fun GnssStatus.toSatelliteInfoList(): List<SatelliteInfo> =
        (0 until satelliteCount).map { index ->
            SatelliteInfo(
                constellation = GnssConstellation.fromConstellationType(getConstellationType(index)),
                svid = getSvid(index),
                cn0DbHz = getCn0DbHz(index),
                elevationDegrees = getElevationDegrees(index),
                azimuthDegrees = getAzimuthDegrees(index),
                usedInFix = usedInFix(index),
            )
        }
}
