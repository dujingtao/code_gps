package com.codegps.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.codegps.app.ui.theme.ReadoutLabelStyle
import com.codegps.app.ui.theme.TextSecondary

/**
 * A single instrument-style readout: a small color accent bar, a muted
 * uppercase label, and an odometer-animated monospace value. Used in a grid
 * below the main satellite sky plot for lat/lon/altitude/accuracy/last-updated.
 *
 * [accentColor] is a flat color swatch rather than an icon glyph — this
 * keeps the card visually consistent with the rest of the custom HUD design
 * without pulling in the `material-icons-extended` artifact (which is
 * several MB) just for a handful of glyphs.
 */
@Composable
fun ReadoutCard(
    accentColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    GlassSurface(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            AccentBar(color = accentColor)
            Text(text = label, style = ReadoutLabelStyle, color = TextSecondary)
            OdometerText(text = value)
        }
    }
}

@Composable
private fun AccentBar(color: Color) {
    Box(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .width(20.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color),
    )
}
