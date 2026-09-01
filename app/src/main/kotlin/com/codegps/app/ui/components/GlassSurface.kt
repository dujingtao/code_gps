package com.codegps.app.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.codegps.app.ui.theme.GlassBorder
import com.codegps.app.ui.theme.GlassFill
import com.codegps.app.ui.theme.SpaceBlackBottom
import com.codegps.app.ui.theme.SpaceBlackMid
import com.codegps.app.ui.theme.SpaceBlackTop

/**
 * A translucent "glass" panel used for every card/prompt in the HUD UI.
 *
 * True backdrop blur (blurring whatever is actually rendered behind this
 * composable) would require capturing a snapshot of sibling content, which
 * Compose does not expose directly. Since the app's background is always the
 * same static vertical gradient (see [LocationScreen][com.codegps.app.ui.LocationScreen]),
 * this fakes a convincing frosted-glass look by drawing and blurring a
 * replica of that gradient behind a translucent fill — visually
 * indistinguishable from a real backdrop blur for a smooth gradient.
 *
 * [Modifier.blur] only has an effect on API 31+; below that this silently
 * degrades to a flat translucent panel with no blur, which still reads fine
 * as "glass", just without the frosted softness.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit,
) {
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(22.dp)
    } else {
        Modifier
    }

    Box(modifier = modifier.clip(shape)) {
        // Backdrop replica: same gradient as the screen background, blurred.
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(blurModifier)
                .background(Brush.verticalGradient(listOf(SpaceBlackTop, SpaceBlackMid, SpaceBlackBottom))),
        )
        // Translucent fill + border on top of the blurred backdrop.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(GlassFill)
                .border(1.dp, GlassBorder, shape),
        )
        // Foreground content stays fully sharp — never wrapped in blur.
        content()
    }
}
