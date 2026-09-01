package com.codegps.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.codegps.app.location.GnssConstellation
import com.codegps.app.location.SatelliteInfo
import com.codegps.app.ui.theme.ReadoutLabelStyle
import com.codegps.app.ui.theme.TextPrimary
import com.codegps.app.ui.theme.color
import com.codegps.app.ui.theme.labelRes

/**
 * A horizontally scrollable row of per-constellation chips — a color dot
 * matching [SatelliteSkyPlot], the constellation's short label, and a
 * "used in fix / visible" count (e.g. "北斗 4/6") — for every constellation
 * that currently has at least one visible satellite. Renders nothing when
 * [satellites] is empty (before a GNSS fix, or with location services off),
 * so the layout degrades gracefully instead of showing an empty row.
 */
@Composable
fun SatelliteSummary(
    satellites: List<SatelliteInfo>,
    modifier: Modifier = Modifier,
) {
    val grouped = satellites.groupBy { it.constellation }.toSortedMap()
    if (grouped.isEmpty()) return

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(grouped.entries.toList()) { (constellation, group) ->
            ConstellationChip(
                constellation = constellation,
                usedCount = group.count { it.usedInFix },
                visibleCount = group.size,
            )
        }
    }
}

@Composable
private fun ConstellationChip(constellation: GnssConstellation, usedCount: Int, visibleCount: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(constellation.color()),
        )
        Box(modifier = Modifier.width(6.dp))
        Text(
            text = "${stringResource(constellation.labelRes())} $usedCount/$visibleCount",
            style = ReadoutLabelStyle,
            color = TextPrimary,
        )
    }
}
