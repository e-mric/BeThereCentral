package com.betherecentral.features.guide.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

// Opaque figure bounds within the original transparent 2×2 generated atlas.
private val standingFrame = IntOffset(237, 108) to IntSize(223, 454)
private val walkingFrames = listOf(
    IntOffset(775, 108) to IntSize(227, 448),
    IntOffset(239, 700) to IntSize(239, 445),
)

/** Draws the generated founder with nearest-neighbor pixels and feet on the route. */
fun DrawScope.drawGuideSprite(atlas: ImageBitmap, feet: Offset, walkingFrame: Int, facingLeft: Boolean) {
    val (sourceOffset, sourceSize) = if (walkingFrame < 0) standingFrame else walkingFrames[walkingFrame and 1]
    val height = (34f * density).roundToInt()
    val width = (height * sourceSize.width.toFloat() / sourceSize.height).roundToInt()
    withTransform({ if (facingLeft) scale(scaleX = -1f, scaleY = 1f, pivot = feet) }) {
        drawImage(
            image = atlas,
            srcOffset = sourceOffset,
            srcSize = sourceSize,
            dstOffset = IntOffset((feet.x - width / 2f).roundToInt(), (feet.y - height).roundToInt()),
            dstSize = IntSize(width, height),
            filterQuality = FilterQuality.None,
        )
    }
}
