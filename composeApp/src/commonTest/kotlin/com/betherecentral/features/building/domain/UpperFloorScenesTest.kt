package com.betherecentral.features.building.domain

import com.betherecentral.features.building.data.BeCentralPlans
import com.betherecentral.features.building.data.UpperFloorScenes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.abs
import kotlin.math.PI

class UpperFloorScenesTest {
    private val floorIds = listOf("first", "second", "third", "fourth")

    @Test
    fun everyUpperFloorHasStableModularDecorationWithinItsSourceRegions() {
        val allProps = mutableListOf<FloorProp>()
        val allMaterialIds = mutableListOf<String>()
        val allWallIds = mutableListOf<String>()
        val geometryFailures = linkedSetOf<String>()
        val overlapFailures = linkedSetOf<String>()

        floorIds.forEach { floorId ->
            val scene = assertNotNull(UpperFloorScenes.forFloor(floorId), "missing $floorId recipe")
            val floor = assertNotNull(BeCentralPlans.floor(floorId))
            assertEquals(floorId, scene.floorId)
            assertTrue(scene.props.isNotEmpty(), "$floorId has no authored props")
            assertTrue(scene.materials.isNotEmpty(), "$floorId has no authored material patches")
            assertTrue(scene.materials.all { patch ->
                floor.regions.any { it.id == patch.regionId && it.kind != PlanRegionKind.UNMAPPED && it.kind != PlanRegionKind.STAIRS }
            }, "$floorId material patch targets an unassigned source area")

            scene.props.forEach { prop ->
                assertTrue(prop.id.isNotBlank())
                assertTrue(prop.width.isFinite() && prop.height.isFinite() && prop.width > 0.0 && prop.height > 0.0, prop.id)
                assertTrue(prop.width >= 24.0 && prop.height >= 24.0, "${prop.id} is too small to read as furniture")
                assertTrue(prop.asset.column in 0..3 && prop.asset.row in 0..3, prop.id)
                val region = floor.regions.singleOrNull { it.id == prop.regionId }
                assertNotNull(region, "${prop.id} refers to a missing source region")
                assertTrue(region.kind != PlanRegionKind.UNMAPPED && region.kind != PlanRegionKind.STAIRS, prop.id)

                // Probe all edges and the full rectangle at three source-pixel intervals.
                val left = prop.center.x - prop.width / 2.0
                val right = prop.center.x + prop.width / 2.0
                val top = prop.center.y - prop.height / 2.0
                val bottom = prop.center.y + prop.height / 2.0
                val xSteps = kotlin.math.ceil(prop.width / 3.0).toInt()
                val ySteps = kotlin.math.ceil(prop.height / 3.0).toInt()
                for (xStep in 0..xSteps) for (yStep in 0..ySteps) {
                    val point = PlanPoint(
                        left + (right - left) * xStep / xSteps,
                        top + (bottom - top) * yStep / ySteps,
                    )
                    if (!PlanWorld.contains(region.polygon, point)) geometryFailures.add("${prop.id} leaves ${region.id}")
                    if (!PlanWorld.contains(floor, point)) geometryFailures.add("${prop.id} leaves the canonical footprint or enters a courtyard")
                    if (floor.regions.filter { it.kind == PlanRegionKind.STAIRS }.any { PlanWorld.contains(it.polygon, point) })
                        geometryFailures.add("${prop.id} overlaps a reserved stair")
                    if (floor.regions.filter { it.kind == PlanRegionKind.UNMAPPED }.any { PlanWorld.contains(it.polygon, point) })
                        geometryFailures.add("${prop.id} overlaps an unmapped source area")
                }
                // Keep the full rendered sprite comfortably inside the shell and courtyard edges.
                val insetHalfW = prop.width / 2.0 + 18.0
                val insetHalfH = prop.height / 2.0 + 18.0
                val insetStepsX = kotlin.math.ceil(insetHalfW * 2 / 3.0).toInt()
                val insetStepsY = kotlin.math.ceil(insetHalfH * 2 / 3.0).toInt()
                for (xStep in 0..insetStepsX) for (yStep in 0..insetStepsY) {
                    val point = PlanPoint(
                        prop.center.x - insetHalfW + insetHalfW * 2 * xStep / insetStepsX,
                        prop.center.y - insetHalfH + insetHalfH * 2 * yStep / insetStepsY,
                    )
                    if (!PlanWorld.contains(floor, point)) geometryFailures.add("${prop.id} is within 18px of the shell or courtyard boundary")
                }
            }
            val solids = scene.props.filter { it.asset != CampusPropAsset.RUG }
            solids.forEachIndexed { index, prop ->
                solids.drop(index + 1).forEach { other ->
                    val horizontalOverlap = minOf(prop.center.x + prop.width / 2.0, other.center.x + other.width / 2.0) -
                        maxOf(prop.center.x - prop.width / 2.0, other.center.x - other.width / 2.0)
                    val verticalOverlap = minOf(prop.center.y + prop.height / 2.0, other.center.y + other.height / 2.0) -
                        maxOf(prop.center.y - prop.height / 2.0, other.center.y - other.height / 2.0)
                    if (horizontalOverlap > 0.0 && verticalOverlap > 0.0)
                        overlapFailures.add("$floorId: ${prop.id} / ${other.id}")
                }
            }
            assertTrue(scene.walls.isNotEmpty(), "$floorId has no authored wall segments")
            scene.walls.forEach { wall ->
                val region = floor.regions.single { it.id == wall.regionId }
                val length = hypot(wall.end.x - wall.start.x, wall.end.y - wall.start.y)
                val steps = kotlin.math.ceil(length / 3.0).toInt()
                for (step in 0..steps) {
                    val t = step.toDouble() / steps
                    val center = PlanPoint(wall.start.x + (wall.end.x - wall.start.x) * t,
                        wall.start.y + (wall.end.y - wall.start.y) * t)
                    for (angleStep in 0..16) {
                        val angle = angleStep * PI / 8.0
                        val envelopePoint = PlanPoint(center.x + 12.0 * kotlin.math.cos(angle),
                            center.y + 12.0 * kotlin.math.sin(angle))
                        if (!PlanWorld.contains(region.polygon, envelopePoint)) geometryFailures.add("${wall.id} 12px envelope leaves ${region.id}")
                        if (!PlanWorld.contains(floor, envelopePoint)) geometryFailures.add("${wall.id} 12px envelope leaves the shell or enters a courtyard")
                        if (floor.regions.any { (it.kind == PlanRegionKind.STAIRS || it.kind == PlanRegionKind.UNMAPPED) && PlanWorld.contains(it.polygon, envelopePoint) })
                            geometryFailures.add("${wall.id} 12px envelope crosses reserved/unmapped area")
                    }
                    if (scene.props.any { prop -> prop.asset != CampusPropAsset.RUG && distanceToRect(center, prop) < 12.0 })
                        geometryFailures.add("${wall.id} is within 12px of a solid prop")
                }
                if (floor.places.flatMap { it.anchors }.any { anchor -> distanceToSegment(anchor, wall.start, wall.end) < 18.0 })
                    geometryFailures.add("${wall.id} is within 18px of a plan anchor")
            }
            allProps += scene.props
            allMaterialIds += scene.materials.map { it.id }
            allWallIds += scene.walls.map { it.id }
        }

        assertEquals(allProps.size, allProps.map { it.id }.toSet().size, "prop IDs must be unique across floors")
        assertTrue(geometryFailures.isEmpty(), geometryFailures.joinToString("\n"))
        assertTrue(overlapFailures.isEmpty(), overlapFailures.joinToString("\n"))
        assertEquals(allMaterialIds.size, allMaterialIds.toSet().size, "material IDs must be unique across floors")
        assertEquals(allWallIds.size, allWallIds.toSet().size, "wall IDs must be unique across floors")
        assertNull(UpperFloorScenes.forFloor("ground"), "the existing ground-floor recipe remains authoritative")
        assertNull(UpperFloorScenes.forFloor("unknown"))
    }

    private fun distanceToSegment(point: PlanPoint, start: PlanPoint, end: PlanPoint): Double {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared == 0.0) return hypot(point.x - start.x, point.y - start.y)
        val t = (((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSquared).coerceIn(0.0, 1.0)
        return hypot(point.x - (start.x + t * dx), point.y - (start.y + t * dy))
    }

    private fun distanceToRect(point: PlanPoint, prop: FloorProp): Double {
        val dx = max(abs(point.x - prop.center.x) - prop.width / 2.0, 0.0)
        val dy = max(abs(point.y - prop.center.y) - prop.height / 2.0, 0.0)
        return hypot(dx, dy)
    }

    @Test
    fun recipesPreserveAmbiguousAndUnmappedSourceConstraints() {
        val firstScene = assertNotNull(UpperFloorScenes.forFloor("first"))
        val first = assertNotNull(BeCentralPlans.floor("first"))
        assertEquals(PlanRegionKind.UNMAPPED, first.regions.single { it.id == "first-grey" }.kind)
        assertTrue(firstScene.props.none { it.regionId == "first-grey" })
        assertTrue(firstScene.materials.any { it.regionId == "first-red-base" && it.material == FloorMaterial.WOOD })

        val second = assertNotNull(BeCentralPlans.floor("second"))
        assertEquals(PlanRegionKind.UNMAPPED, second.regions.single { it.id == "second-unmapped" }.kind)
        assertTrue(UpperFloorScenes.forFloor("second")!!.props.none { it.regionId == "second-unmapped" })

        val fourth = assertNotNull(BeCentralPlans.floor("fourth"))
        assertEquals(PlanRegionKind.UNMAPPED, fourth.regions.single { it.id == "fourth-unmapped" }.kind)
        assertTrue(UpperFloorScenes.forFloor("fourth")!!.props.none { it.regionId == "fourth-unmapped" })
        assertTrue(fourth.places.single { it.number == "66" }.anchors.isEmpty())
    }
}
