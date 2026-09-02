package com.codegps.app.ui.components

/**
 * Typical strong-signal ceiling (dB-Hz) used to normalize a GNSS
 * carrier-to-noise density (C/N0) reading into a 0..1 fraction — not a hard
 * limit, real signals can occasionally read a little higher.
 */
private const val MAX_EXPECTED_CN0_DB_HZ = 45f

/**
 * Minimum fraction a valid signal reading normalizes to, so a very weak but
 * still-tracked satellite doesn't render as a zero-width bar / an
 * effectively invisible dot.
 */
private const val MIN_SIGNAL_FRACTION = 0.3f

/**
 * Normalizes a raw GNSS C/N0 reading (dB-Hz) to a 0..1 fraction of
 * [MAX_EXPECTED_CN0_DB_HZ], clamped to [MIN_SIGNAL_FRACTION]..1 so both very
 * weak and very strong signals stay visually legible. Shared by
 * [SatelliteSkyPlot] and [SatelliteSignalList] so both views agree on the
 * same scale.
 */
fun Float.toSignalFraction(): Float = (this / MAX_EXPECTED_CN0_DB_HZ).coerceIn(MIN_SIGNAL_FRACTION, 1f)
