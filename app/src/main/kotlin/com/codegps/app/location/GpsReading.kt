package com.codegps.app.location

/**
 * A single, immutable snapshot of device location.
 *
 * Wrapping the platform [android.location.Location] object in our own type
 * keeps the UI layer decoupled from the Android location API and lets us
 * expose only the fields the app actually displays.
 */
data class GpsReading(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val altitudeMeters: Double,
    val speedMetersPerSecond: Float,
    val timestampMillis: Long,
)
