package com.codegps.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.codegps.app.R
import com.codegps.app.location.GnssConstellation

// Per-constellation sky-plot colors. Kept separate from the core HUD accents
// because the sky plot can show 4+ simultaneous constellations that all need
// to stay visually distinguishable — GPS/Galileo reuse the app's primary
// neon accents, the rest get their own distinct hues rather than alpha
// variants of an existing color.
private val ConstellationGlonass = Color(0xFFFF7043)
private val ConstellationBeidou = Color(0xFFFFD54F)
private val ConstellationQzss = Color(0xFF69F0AE)
private val ConstellationSbas = Color(0xFFB0BEC5)
private val ConstellationIrnss = Color(0xFFFF4FD8)

/**
 * Maps a [GnssConstellation] to the color its dots use on the satellite sky
 * plot and its chip's dot in the satellite summary — both views always
 * agree on the same color per constellation.
 */
fun GnssConstellation.color(): Color = when (this) {
    GnssConstellation.GPS -> NeonCyan
    GnssConstellation.GLONASS -> ConstellationGlonass
    GnssConstellation.BEIDOU -> ConstellationBeidou
    GnssConstellation.GALILEO -> NeonViolet
    GnssConstellation.QZSS -> ConstellationQzss
    GnssConstellation.SBAS -> ConstellationSbas
    GnssConstellation.IRNSS -> ConstellationIrnss
    GnssConstellation.UNKNOWN -> TextSecondary
}

/**
 * Short display-name string resource for [this] constellation, shown in the
 * satellite summary chips (e.g. "北斗 4/6").
 */
fun GnssConstellation.labelRes(): Int = when (this) {
    GnssConstellation.GPS -> R.string.constellation_gps
    GnssConstellation.GLONASS -> R.string.constellation_glonass
    GnssConstellation.BEIDOU -> R.string.constellation_beidou
    GnssConstellation.GALILEO -> R.string.constellation_galileo
    GnssConstellation.QZSS -> R.string.constellation_qzss
    GnssConstellation.SBAS -> R.string.constellation_sbas
    GnssConstellation.IRNSS -> R.string.constellation_irnss
    GnssConstellation.UNKNOWN -> R.string.constellation_unknown
}
