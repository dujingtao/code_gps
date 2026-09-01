package com.codegps.app.location

import android.location.GnssStatus

/**
 * The satellite navigation system a satellite belongs to, decoupled from the
 * raw [GnssStatus.CONSTELLATION_*] int constants so the UI layer never has
 * to know about the platform API directly (mirrors how [GpsReading] wraps
 * [android.location.Location]).
 */
enum class GnssConstellation {
    GPS,
    GLONASS,
    BEIDOU,
    GALILEO,
    QZSS,
    SBAS,
    IRNSS,
    UNKNOWN;

    companion object {
        /**
         * [GnssStatus.CONSTELLATION_IRNSS] was only added in API 28; this
         * app's minSdk is 26, so its value is hardcoded here rather than
         * referencing the constant directly. It will simply never be
         * produced by [GnssStatus.getConstellationType] on API < 28 devices.
         */
        private const val CONSTELLATION_IRNSS = 7

        /** Maps a raw [GnssStatus.getConstellationType] value to a [GnssConstellation]. */
        fun fromConstellationType(type: Int): GnssConstellation = when (type) {
            GnssStatus.CONSTELLATION_GPS -> GPS
            GnssStatus.CONSTELLATION_GLONASS -> GLONASS
            GnssStatus.CONSTELLATION_BEIDOU -> BEIDOU
            GnssStatus.CONSTELLATION_GALILEO -> GALILEO
            GnssStatus.CONSTELLATION_QZSS -> QZSS
            GnssStatus.CONSTELLATION_SBAS -> SBAS
            CONSTELLATION_IRNSS -> IRNSS
            else -> UNKNOWN
        }
    }
}
