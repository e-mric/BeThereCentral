package com.betherecentral.features.building.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.hypot

/** Decorative masonry around courtyard openings. All dimensions are source-plan pixels. */
internal fun DrawScope.drawCourtyardWalls(
    voids: List<List<Offset>>,
    scale: Float,
    buildingShell: Path,
) {
    if (scale <= 0f) return
    val shadow = Color(0xFF181C1E)
    val masonry = Color(0xFF33393A)
    val capShades = listOf(Color(0xFF596263), Color(0xFF50595A), Color(0xFF626A6B), Color(0xFF555E5F))
    val capEdge = Color(0xFF788182)
    val seam = Color(0xFF252B2D)
    val frame = Color(0xFF252D31)
    val glass = Color(0xFF9AC8D8)
    val glassLight = Color(0xFFC6E3E9)

    // A centered closed outline leaves its courtyard-facing half outside the shell. Clipping
    // retains only the building-side half and never paints into the empty courtyard.
    clipPath(buildingShell) {
        voids.filter { it.size >= 3 }.forEach { polygon ->
            val boundary = Path().apply {
                polygon.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
                close()
            }
            drawPath(boundary, shadow, style = Stroke(width = 34f * scale, join = StrokeJoin.Bevel))
            drawPath(boundary, masonry, style = Stroke(width = 28f * scale, join = StrokeJoin.Bevel))
            drawPath(boundary, Color(0xFF454D4E), style = Stroke(width = 18f * scale, join = StrokeJoin.Bevel))

            val winding = signedArea(polygon)
            for (index in polygon.indices) {
                val a = polygon[index]
                val b = polygon[(index + 1) % polygon.size]
                val dx = b.x - a.x
                val dy = b.y - a.y
                val length = hypot(dx, dy)
                if (length < 68f * scale) continue
                val tx = dx / length
                val ty = dy / length
                // In y-down screen coordinates, this points from the courtyard into the shell.
                val nx = if (winding >= 0f) ty else -ty
                val ny = if (winding >= 0f) -tx else tx
                val pitch = 60f * scale
                val module = 36f * scale
                val count = ((length - 16f * scale) / pitch).toInt().coerceAtLeast(0)
                for (n in 0 until count) {
                    val centerDistance = 8f * scale + n * pitch + pitch / 2f
                    if (centerDistance + module / 2f > length - 5f * scale) break
                    val center = Offset(a.x + tx * centerDistance, a.y + ty * centerDistance)
                    val start = center - Offset(tx * module / 2f, ty * module / 2f)
                    val end = center + Offset(tx * module / 2f, ty * module / 2f)
                    // A few source-indexed tones break the long cap into hand-laid slate blocks.
                    val capStart = centerDistance - pitch / 2f + 1f * scale
                    val capEnd = centerDistance + pitch / 2f - 1f * scale
                    val block = orientedQuad(a, tx, ty, nx, ny, capStart, capEnd, 0f, 13f * scale)
                    drawPath(block, capShades[(index + n) % capShades.size])
                    drawLine(capEdge,
                        Offset(a.x + tx * capStart + nx * 1.4f * scale, a.y + ty * capStart + ny * 1.4f * scale),
                        Offset(a.x + tx * capEnd + nx * 1.4f * scale, a.y + ty * capEnd + ny * 1.4f * scale),
                        1.2f * scale)
                    val blockJoint = Offset(a.x + tx * (capEnd + 1f * scale), a.y + ty * (capEnd + 1f * scale))
                    drawLine(seam, blockJoint, blockJoint + Offset(nx * 12f * scale, ny * 12f * scale), 1.4f * scale)

                    // Deep slate piers alternate with wide, inset window bays.
                    val pierWidth = 11f * scale
                    for (pierDistance in listOf(centerDistance - pitch / 2f + 1f * scale, centerDistance + pitch / 2f - 1f * scale)) {
                        val pier = orientedQuad(a, tx, ty, nx, ny, pierDistance - pierWidth / 2f, pierDistance + pierWidth / 2f, 0f, 16f * scale)
                        drawPath(pier, Color(0xFF272E30))
                        drawLine(Color(0xFF687273),
                            Offset(a.x + tx * (pierDistance - pierWidth / 2f) + nx * 1.2f * scale, a.y + ty * (pierDistance - pierWidth / 2f) + ny * 1.2f * scale),
                            Offset(a.x + tx * (pierDistance + pierWidth / 2f) + nx * 1.2f * scale, a.y + ty * (pierDistance + pierWidth / 2f) + ny * 1.2f * scale),
                            1f * scale)
                    }

                    val windowStart = start + Offset(nx * 4f * scale, ny * 4f * scale)
                    val windowEnd = end + Offset(nx * 4f * scale, ny * 4f * scale)
                    drawLine(frame, windowStart, windowEnd, 9f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Square)
                    drawLine(glass, windowStart + Offset(nx * 1f * scale, ny * 1f * scale), windowEnd + Offset(nx * 1f * scale, ny * 1f * scale), 5f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Square)
                    drawLine(glassLight, windowStart + Offset(nx * .7f * scale, ny * .7f * scale) - Offset(nx * 1.1f * scale, ny * 1.1f * scale), windowEnd + Offset(nx * .7f * scale, ny * .7f * scale) - Offset(nx * 1.1f * scale, ny * 1.1f * scale), 1f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Square)
                    // Slim mullions divide panes without turning the band into a bright rail.
                    for (fraction in listOf(.34f, .67f)) {
                        val mullion = windowStart + Offset(tx * module * fraction, ty * module * fraction)
                        drawLine(frame, mullion, mullion + Offset(nx * 4f * scale, ny * 4f * scale), 1.8f * scale)
                    }
                }
                // Short masonry joints between window bays, kept subtle at fit scale.
                val seamDistance = length * .5f
                val seamPoint = Offset(a.x + tx * seamDistance, a.y + ty * seamDistance)
                drawLine(seam, seamPoint - Offset(nx * 4f * scale, ny * 4f * scale), seamPoint + Offset(nx * 4f * scale, ny * 4f * scale), 1.5f * scale)
            }
        }
    }
}

private fun orientedQuad(
    origin: Offset,
    tx: Float,
    ty: Float,
    nx: Float,
    ny: Float,
    alongStart: Float,
    alongEnd: Float,
    depthStart: Float,
    depthEnd: Float,
) = Path().apply {
    val points = listOf(
        Offset(origin.x + tx * alongStart + nx * depthStart, origin.y + ty * alongStart + ny * depthStart),
        Offset(origin.x + tx * alongEnd + nx * depthStart, origin.y + ty * alongEnd + ny * depthStart),
        Offset(origin.x + tx * alongEnd + nx * depthEnd, origin.y + ty * alongEnd + ny * depthEnd),
        Offset(origin.x + tx * alongStart + nx * depthEnd, origin.y + ty * alongStart + ny * depthEnd),
    )
    points.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) }
    close()
}

private fun signedArea(points: List<Offset>): Float = points.indices.sumOf { i ->
    val a = points[i]
    val b = points[(i + 1) % points.size]
    (a.x * b.y - b.x * a.y).toDouble()
}.toFloat() / 2f
