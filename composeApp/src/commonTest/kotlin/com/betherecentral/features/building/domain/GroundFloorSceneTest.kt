package com.betherecentral.features.building.domain

import com.betherecentral.features.building.data.BeCentralPlans
import com.betherecentral.features.building.data.GroundFloorScene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GroundFloorSceneTest {
    private fun groundAnchor(id: String) = BeCentralPlans.floor("ground")!!.places.single { it.id == id }.anchors.single()
    @Test
    fun authoredPropsHaveStableIdsAndStayInsideAssignedKnownRegions() {
        val scene = GroundFloorScene.scene
        val floor = BeCentralPlans.floor("ground")!!
        assertEquals("ground", scene.floorId)
        assertEquals(scene.props.size, scene.props.map { it.id }.toSet().size)
        assertEquals(scene.materials.size, scene.materials.map { it.id }.toSet().size)
        assertEquals(scene.walls.size, scene.walls.map { it.id }.toSet().size)
        assertTrue(scene.materials.all { patch -> floor.regions.any { it.id == patch.regionId && it.kind != PlanRegionKind.UNMAPPED } })

        scene.props.forEach { prop ->
            assertTrue(prop.width.isFinite() && prop.width > 0.0 && prop.height.isFinite() && prop.height > 0.0, prop.id)
            assertTrue(prop.asset.column in 0..3 && prop.asset.row in 0..3, prop.id)
            val region = floor.regions.single { it.id == prop.regionId }
            assertNotEquals(PlanRegionKind.UNMAPPED, region.kind, prop.id)
            val bikeRoom = floor.regions.single { it.id == "ground-bike-room" }
            val probes = prop.bounds + prop.center + buildList {
                for (xStep in 0..(prop.width / 4).toInt()) {
                    for (yStep in 0..(prop.height / 4).toInt()) {
                        add(PlanPoint(prop.center.x - prop.width / 2 + xStep * 4,
                            prop.center.y - prop.height / 2 + yStep * 4))
                    }
                }
            }
            probes.forEach { point ->
                if (prop.regionId == "ground-lobby") assertTrue(!PlanWorld.contains(bikeRoom.polygon, point), "${prop.id} crosses the bike-room partition")
                assertTrue(PlanWorld.contains(region.polygon, point), "${prop.id} leaves ${region.id} at $point")
                assertTrue(PlanWorld.contains(floor, point), "${prop.id} leaves the canonical shell or enters a void at $point")
                assertTrue(floor.regions.filter { it.kind == PlanRegionKind.STAIRS }.none { PlanWorld.contains(it.polygon, point) }, "${prop.id} enters reserved stairs at $point")
            }
        }

        scene.walls.forEach { wall ->
            val region = floor.regions.single { it.id == wall.regionId }
            listOf(wall.start, wall.end).forEach { point ->
                assertTrue(PlanWorld.contains(region.polygon, point), "${wall.id} leaves its assigned region")
                assertTrue(PlanWorld.contains(floor, point), "${wall.id} leaves the canonical shell or enters a void")
                assertTrue(floor.regions.filter { it.kind == PlanRegionKind.STAIRS }.none { PlanWorld.contains(it.polygon, point) }, "${wall.id} enters reserved stairs")
            }
        }
    }

    @Test
    fun sourceLandmarksAndContinuousBikePartitionRemainAttached() {
        // Independently traced from the supplied white divider; a door gap would break this chain.
        val divider = GroundFloorScene.scene.walls.filter { it.id.startsWith("bike-partition-") }
        assertEquals(2, divider.size)
        assertEquals(listOf(PlanPoint(1199.0, 1058.0), PlanPoint(1177.0, 1138.0), PlanPoint(1177.0, 1185.0)),
            listOf(divider[0].start, divider[0].end, divider[1].end))
        assertEquals(divider[0].end, divider[1].start)
        assertEquals(setOf("ground-orange", "ground-lobby", "ground-bike-room", "ground-fari"), GroundFloorScene.scene.materials.map { it.regionId }.toSet())
        assertEquals(groundAnchor("front-desk"), GroundFloorScene.scene.props.single { it.id == "lobby-desk" }.center)
        assertEquals(groundAnchor("bike-parking"), GroundFloorScene.scene.props.single { it.id == "lobby-bike-parking" }.center)
    }
}
