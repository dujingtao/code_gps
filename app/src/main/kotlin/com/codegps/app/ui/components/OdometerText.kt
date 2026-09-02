package com.codegps.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.codegps.app.ui.theme.ReadoutValueStyle
import com.codegps.app.ui.theme.TextPrimary

/**
 * Renders [text] as a row of independently animated characters — the classic
 * odometer / flip-digit effect: when [text] changes, only the character
 * positions whose value actually changed roll to their new glyph, while
 * unrelated characters stay visually still. This replaces animating the
 * whole string as one sliding block, which reads as the entire readout
 * "jumping" on every GPS update.
 *
 * Character positions are keyed by their distance from the **end** of the
 * string, not by left-to-right index. Formatted GPS values can grow a digit
 * at the front between updates (e.g. "9.8" -> "10.2" m/s, or
 * "9.90420°" -> "10.90420°"); indexing from the left would then shift every
 * later character's index by one, so the wrong digits would appear to flip.
 * Indexing from the end keeps each digit's identity stable relative to the
 * decimal point — exactly how a mechanical odometer's wheels work — so only
 * the digit(s) that actually changed value animate. The one edge case this
 * doesn't animate specially is a brand-new leading position appearing (the
 * string growing a character): that position has no prior on-screen state to
 * transition from, so it simply appears already showing its value.
 *
 * Each changed character crossfades **in place** (no vertical slide) — an
 * earlier version slid characters up/down, but with several readouts (and,
 * in the satellite signal list, several rows) all updating within the same
 * second, many independently-timed slides on screen at once read as chaotic
 * rather than legible. A plain fade keeps each digit anchored to its slot so
 * only the "what changed" signal remains.
 */
@Composable
fun OdometerText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = ReadoutValueStyle,
    color: Color = TextPrimary,
) {
    val reversed = text.reversed()

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Iterating distance-from-end downTo 0 reconstructs the original
        // left-to-right character order while keying each slot by its
        // distance from the end (see the class doc for why that's the axis
        // that must stay stable).
        for (indexFromEnd in reversed.indices.reversed()) {
            val char = reversed[indexFromEnd]
            key(indexFromEnd) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        fadeIn(tween(180)).togetherWith(fadeOut(tween(180)))
                    },
                    label = "odometer_char_$indexFromEnd",
                ) { animatedChar ->
                    Text(text = animatedChar.toString(), style = style, color = color)
                }
            }
        }
    }
}
