package com.codegps.app.location

/**
 * A single, immutable snapshot of one GNSS satellite's status, as reported
 * by [android.location.GnssStatus] for one entry in a status update.
 */
data class SatelliteInfo(
    val constellation: GnssConstellation,
    val svid: Int,
    val cn0DbHz: Float,
    val elevationDegrees: Float,
    val azimuthDegrees: Float,
    val usedInFix: Boolean,
)
