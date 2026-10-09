package com.betherecentral.features.building.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.betherecentral.features.building.domain.FloorScene
import com.betherecentral.features.building.domain.FloorMaterial
import com.betherecentral.features.building.domain.PlanFloor
import com.betherecentral.features.building.domain.PlanPoint
import com.betherecentral.features.building.domain.PlanRegionKind
import com.betherecentral.features.building.domain.FloorWallSegment
import kotlin.math.floor
import kotlin.math.min

/** Draws independent surface, wall and furniture assets inside the source-derived shell. */
internal fun DrawScope.drawModularFloor(
    floor: PlanFloor,
    scene: FloorScene,
    propsAtlas: ImageBitmap,
    project: (PlanPoint) -> Offset,
    scale: Float,
    assignedShell: Path,
) {
    if (floor.id != scene.floorId || scale <= 0f || propsAtlas.width <= 0 || propsAtlas.height <= 0) return
    clipPath(assignedShell) {
        scene.materials.forEach { patch ->
            val region = floor.regions.firstOrNull { it.id == patch.regionId } ?: return@forEach
            val polygon = projectedPath(region.polygon, project)
            clipPath(polygon) {
                drawPath(polygon, if (patch.material == FloorMaterial.WOOD) Color(0xFFC39A6C) else Color(0xFFD8CEB8))
                if (patch.material == FloorMaterial.WOOD) drawWood(region.polygon, project, scale)
                else drawTile(region.polygon, project, scale)
            }
        }

        // Reuse the slate/window kit on the furnished areas' perimeter. The same
        // modules face inward here, while courtyard modules face out of the opening.
        if (floor.id == "ground") scene.materials.forEach { patch ->
            val region = floor.regions.first { it.id == patch.regionId }
            var trim = projectedPath(region.polygon, project)
            floor.regions.filter { it.kind == PlanRegionKind.STAIRS }.forEach {
                trim = Path.combine(PathOperation.Difference, trim, projectedPath(it.polygon, project))
            }
            if (region.id == "ground-lobby") {
                val doorway = listOf(PlanPoint(1290.0, 1170.0), PlanPoint(1370.0, 1170.0),
                    PlanPoint(1370.0, 1210.0), PlanPoint(1290.0, 1210.0))
                trim = Path.combine(PathOperation.Difference, trim, projectedPath(doorway, project))
            }
            if (region.id != "ground-bike-room") {
                drawCourtyardWalls(listOf(region.polygon.map(project)), scale, trim, insideBoundary = true)
            }
        }

        // Upper-floor zones overlap. Only the external silhouette receives automatic
        // perimeter trim; interior walls are independently authored in each recipe.
        if (floor.id != "ground") {
            drawCourtyardWalls(floor.footprints.map { it.map(project) }, scale, assignedShell, insideBoundary = true)
        }

        // Stair landings remain reserved and visually distinct, including where a colored
        // region polygon continues underneath them.
        floor.regions.filter { it.kind == PlanRegionKind.STAIRS }.forEach { stair ->
            val stairPath = projectedPath(stair.polygon, project)
            clipPath(stairPath) {
                drawPath(stairPath, Color(0xFF343B3D))
                if (scene.sourceLinework == null) {
                    val bounds = bounds(stair.polygon)
                    val startY = floor(bounds.top / 30.0).toInt() * 30
                    var y = startY
                    while (y < bounds.bottom) {
                        val line = projectedPath(listOf(PlanPoint(bounds.left, y.toDouble()), PlanPoint(bounds.right, y.toDouble())), project)
                        drawPath(line, Color(0xFF596264), style = Stroke(width = 2.2f * scale))
                        y += 30
                    }
                    drawPath(stairPath, Color(0xFF202729), style = Stroke(width = 5f * scale))
                }
            }
        }

        scene.walls.forEach { wall -> drawWallSegment(wall, floor, project, scale) }

        scene.props.sortedWith(compareBy({ it.layer }, { it.center.y })).forEach { prop ->
            val src = atlasCell(propsAtlas, prop.asset.column, prop.asset.row) ?: return@forEach
            val points = prop.bounds.map(project)
            val left = points.minOf { it.x }; val right = points.maxOf { it.x }
            val top = points.minOf { it.y }; val bottom = points.maxOf { it.y }
            if (right <= left || bottom <= top) return@forEach
            val targetRatio = (right - left) / (bottom - top)
            val srcRatio = src.width.toFloat() / src.height.toFloat()
            val destW: Float; val destH: Float
            if (targetRatio > srcRatio) { destH = bottom - top; destW = destH * srcRatio }
            else { destW = right - left; destH = destW / srcRatio }
            val dest = Offset((left + right - destW) / 2f, (top + bottom - destH) / 2f)
            var allowed = projectedPath(floor.regions.first { it.id == prop.regionId }.polygon, project)
            floor.regions.filter { it.kind == PlanRegionKind.STAIRS }.forEach { stair ->
                allowed = Path.combine(PathOperation.Difference, allowed, projectedPath(stair.polygon, project))
            }
            clipPath(allowed) {
              drawImage(
                image = propsAtlas,
                srcOffset = IntOffset(src.left, src.top),
                srcSize = IntSize(src.width, src.height),
                dstOffset = IntOffset(dest.x.toInt(), dest.y.toInt()),
                dstSize = IntSize(destW.toInt().coerceAtLeast(1), destH.toInt().coerceAtLeast(1)),
                filterQuality = FilterQuality.None,
              )
            }
        }
    }

}

/** Draws source-observed contours above decorative art and perimeter trim. */
internal fun DrawScope.drawSourceLinework(
    scene: FloorScene,
    project: (PlanPoint) -> Offset,
    scale: Float,
    partitionColor: Color = Color(0xFF454C4E),
    stairColor: Color = Color(0xFF98A7AA),
    unmappedMask: Path? = null,
) {
    // This layer sits outside the coarse assigned-region mask, preserving source marks
    // that cross those approximate polygons and keeping their authored width.
    scene.sourceLinework?.let { linework ->
        drawSourceContours(linework.partitions, project, scale, partitionColor)
        drawSourceContours(linework.stairs, project, scale, stairColor)
        // Source marks on charcoal need the same contrast as the reserved stairs.
        // Recolour the existing geometry; do not expand its edges or close gaps.
        unmappedMask?.let { mask ->
            clipPath(mask) {
                drawSourceContours(linework.partitions, project, scale, stairColor)
            }
        }
    }
}

private fun DrawScope.drawSourceContours(
    contours: List<List<PlanPoint>>,
    project: (PlanPoint) -> Offset,
    scale: Float,
    color: Color,
) {
    val compound = Path().apply {
        fillType = PathFillType.EvenOdd
        contours.filter { it.size >= 3 }.forEach { contour ->
            contour.forEachIndexed { index, point ->
                val screen = project(point)
                if (index == 0) moveTo(screen.x, screen.y) else lineTo(screen.x, screen.y)
            }
            close()
        }
    }
    if (contours.any { it.size >= 3 }) {
        drawPath(compound, color)
        drawPath(compound, color, style = Stroke(width = scale.coerceAtLeast(.01f)))
    }
    contours.filter { it.size == 2 }.forEach { contour ->
        drawLine(color, project(contour[0]), project(contour[1]), strokeWidth = scale.coerceAtLeast(.01f))
    }
    contours.filter { it.size == 1 }.forEach { contour ->
        drawCircle(color, radius = scale.coerceAtLeast(.01f) / 2f, center = project(contour.single()))
    }
}

private fun DrawScope.drawWallSegment(
    wall: FloorWallSegment,
    floor: PlanFloor,
    project: (PlanPoint) -> Offset,
    scale: Float,
) {
    val region = floor.regions.firstOrNull { it.id == wall.regionId } ?: return
    val regionPath = projectedPath(region.polygon, project)
    clipPath(regionPath) {
        val start = project(wall.start); val end = project(wall.end)
        drawLine(Color(0xFF171D20), start + Offset(0f, 4f * scale), end + Offset(0f, 4f * scale), 16f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Square)
        drawLine(Color(0xFF30383B), start, end, 13f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Square)
        drawLine(Color(0xFF626D70), start - Offset(0f, 4f * scale), end - Offset(0f, 4f * scale), 3f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Square)
        if (wall.windows) {
            drawLine(Color(0xFF20292D), start, end, 8f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Square)
            drawLine(Color(0xFF7FAABD), start + Offset(0f, 1f * scale), end + Offset(0f, 1f * scale), 4f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Square)
            drawLine(Color(0xFFC0DFE6), start - Offset(0f, 1f * scale), end - Offset(0f, 1f * scale), 1.2f * scale, cap = androidx.compose.ui.graphics.StrokeCap.Square)
        }
    }
}

private data class SourceRect(val left: Int, val top: Int, val width: Int, val height: Int)

/** Tight-ish transparent-safe crops per atlas cell; dimensions adapt to the 1254px atlas. */
private fun atlasCell(image: ImageBitmap, column: Int, row: Int): SourceRect? {
    if (column !in 0..3 || row !in 0..3) return null
    // Tight source-pixel boxes for the assets used by this recipe (atlas is 1254 × 1254).
    // They avoid the very different transparent margins around each cell's sprite.
    val box = when (row to column) {
        0 to 0 -> listOf(40f, 88f, 296f, 307f)   // single desk
        0 to 1 -> listOf(318f, 52f, 620f, 310f)  // paired workstations
        0 to 2 -> listOf(652f, 65f, 904f, 307f)   // broad shared table
        0 to 3 -> listOf(966f, 52f, 1218f, 305f)  // round table
        1 to 1 -> listOf(356f, 328f, 590f, 620f)  // bookcase
        1 to 2 -> listOf(670f, 340f, 902f, 619f)  // potted plant
        1 to 3 -> listOf(956f, 380f, 1226f, 610f) // café counter
        1 to 0 -> listOf(20f, 378f, 310f, 616f)  // lounge corner
        2 to 0 -> listOf(46f, 672f, 295f, 911f) // bike parking
        2 to 1 -> listOf(375f, 632f, 582f, 920f) // phone booth
        2 to 2 -> listOf(680f, 680f, 882f, 930f) // printer
        2 to 3 -> listOf(1038f, 642f, 1198f, 924f) // water cooler
        3 to 0 -> listOf(38f, 960f, 290f, 1215f) // whiteboard
        3 to 1 -> listOf(329f, 970f, 615f, 1205f) // rug
        3 to 2 -> listOf(640f, 982f, 920f, 1186f) // reception desk
        3 to 3 -> listOf(950f, 1010f, 1224f, 1172f) // sofa
        else -> {
            val cellW = image.width / 4f; val cellH = image.height / 4f
            val inset = 9f / 313.5f
            listOf(column * cellW + cellW * inset, row * cellH + cellH * inset,
                (column + 1) * cellW - cellW * inset, (row + 1) * cellH - cellH * inset)
        }
    }
    val left = (box[0] / 1254f * image.width).toInt()
    val top = (box[1] / 1254f * image.height).toInt()
    val right = min(image.width, (box[2] / 1254f * image.width).toInt())
    val bottom = min(image.height, (box[3] / 1254f * image.height).toInt())
    return SourceRect(left, top, right - left, bottom - top)
}

private fun DrawScope.drawWood(points: List<PlanPoint>, project: (PlanPoint) -> Offset, scale: Float) {
    val b = bounds(points)
    val plankHeight = 8.0
    val plankWidth = 48.0
    val tones = listOf(0xFFC89F72, 0xFFC0986D, 0xFFCEA779, 0xFFC59C6E, 0xFFBD9366)
    var row = floor(b.top / plankHeight).toInt()
    while (row * plankHeight < b.bottom) {
        val y = row * plankHeight
        val stagger = (row % 3) * 16.0
        var col = floor((b.left - stagger) / plankWidth).toInt()
        while (col * plankWidth + stagger < b.right) {
            val x = col * plankWidth + stagger
            val corners = listOf(PlanPoint(x, y), PlanPoint(x + plankWidth, y),
                PlanPoint(x + plankWidth, y + plankHeight), PlanPoint(x, y + plankHeight))
            drawPath(projectedPath(corners, project), Color(tones[(row * 7 + col * 3).mod(tones.size)]))
            drawLine(Color(0xFF9B764F), project(PlanPoint(x, y)), project(PlanPoint(x + plankWidth, y)), .6f * scale)
            drawLine(Color(0xFFAC865C), project(PlanPoint(x, y)), project(PlanPoint(x, y + plankHeight)), .6f * scale)
            drawLine(Color(0xFFE0B888), project(PlanPoint(x + 1, y + 1)), project(PlanPoint(x + plankWidth - 1, y + 1)), .5f * scale)
            val grainOffset = ((row + col).mod(4) + 2).toDouble()
            drawLine(Color(0x40936A45), project(PlanPoint(x + 5, y + grainOffset)),
                project(PlanPoint(x + 28 + (col.mod(3) * 4), y + grainOffset)), .45f * scale)
            col++
        }
        row++
    }
}

private fun DrawScope.drawTile(points: List<PlanPoint>, project: (PlanPoint) -> Offset, scale: Float) {
    val b = bounds(points)
    var tileRow = floor(b.top / 16.0).toInt()
    while (tileRow * 16.0 < b.bottom) {
        var tileColumn = floor(b.left / 16.0).toInt()
        while (tileColumn * 16.0 < b.right) {
            if ((tileRow + tileColumn) % 2 == 0) {
                val left = tileColumn * 16.0; val top = tileRow * 16.0
                drawPath(projectedPath(listOf(PlanPoint(left, top), PlanPoint(left + 16, top), PlanPoint(left + 16, top + 16), PlanPoint(left, top + 16)), project), Color(0x0D998E79))
            }
            tileColumn++
        }
        tileRow++
    }
    var x = floor(b.left / 16.0).toInt() * 16.0
    while (x <= b.right) {
        drawPath(projectedPath(listOf(PlanPoint(x, b.top), PlanPoint(x, b.bottom)), project), Color(0xFFB9AF99), style = Stroke(width = .7f * scale))
        x += 16.0
    }
    var y = floor(b.top / 16.0).toInt() * 16.0
    while (y <= b.bottom) {
        drawPath(projectedPath(listOf(PlanPoint(b.left, y), PlanPoint(b.right, y)), project), Color(0xFFB9AF99), style = Stroke(width = .7f * scale))
        y += 16.0
    }
    drawPath(projectedPath(points, project), Color(0xFFC0BAA7), style = Stroke(width = 1.6f * scale))
}

private data class Bounds(val left: Double, val top: Double, val right: Double, val bottom: Double)
private fun bounds(points: List<PlanPoint>) = Bounds(points.minOf { it.x }, points.minOf { it.y }, points.maxOf { it.x }, points.maxOf { it.y })

private fun projectedPath(points: List<PlanPoint>, project: (PlanPoint) -> Offset) = Path().apply {
    points.forEachIndexed { index, point ->
        val p = project(point)
        if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
    }
    if (points.size > 2) close()
}
