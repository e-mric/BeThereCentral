package com.betherecentral.features.building.domain

import com.betherecentral.features.building.data.BeCentralPlans
import com.betherecentral.features.building.data.CampusFloorScenes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlanLineworkTest {
    private fun rectangle(left: Double, top: Double, right: Double, bottom: Double) = listOf(
        PlanPoint(left, top), PlanPoint(right, top), PlanPoint(right, bottom), PlanPoint(left, bottom),
    )
    private fun prop(x: Double, y: Double, width: Double = 2.0, height: Double = 2.0) =
        FloorProp("test", "room", PlanPoint(x, y), width, height, CampusPropAsset.DESK)

    @Test
    fun furnitureCannotCrossThinLinesEvenWhenNoVertexFallsInsideIt() {
        val lines = PlanLinework(listOf(listOf(PlanPoint(0.0, 5.0), PlanPoint(20.0, 5.0))), emptyList())
        assertTrue(lines.intersects(prop(10.0, 5.0), clearance = 0.0))
        assertFalse(lines.intersects(prop(10.0, 9.0), clearance = 0.0))
        assertTrue(lines.intersects(prop(10.0, 9.0), clearance = 3.0))
    }

    @Test
    fun courtyardHolesAndSourceDoorGapsRemainAvailable() {
        val ring = PlanLinework(listOf(rectangle(0.0, 0.0, 20.0, 20.0), rectangle(4.0, 4.0, 16.0, 16.0)), emptyList())
        assertFalse(ring.intersects(prop(10.0, 10.0), clearance = 0.0))
        assertTrue(ring.intersects(prop(2.0, 10.0), clearance = 0.0))
        val gap = PlanLinework(listOf(rectangle(0.0, 0.0, 5.0, 1.0), rectangle(10.0, 0.0, 20.0, 1.0)), emptyList())
        assertFalse(gap.intersects(prop(7.5, 0.5), clearance = 0.0))
    }

    @Test
    fun isolatedPixelsAndStairTreadsAlsoReserveTheirSpace() {
        val lines = PlanLinework(listOf(listOf(PlanPoint(5.0, 5.0))), listOf(rectangle(10.0, 0.0, 11.0, 20.0)))
        assertTrue(lines.intersects(prop(5.0, 5.0), clearance = 0.0))
        assertTrue(lines.intersects(prop(10.5, 10.0), clearance = 0.0))
    }

    @Test
    fun sourceLandmarksRetainWestWingPartitionsAndTheGreyConnector() {
        // Original 2048×1448 schematic coordinates, independently inspected in
        // the source images: Garden 49/51, Ecas, Microstart, grey fourth connector.
        val garden = CampusFloorScenes.forFloor("third").sourceLinework!!
        listOf(PlanPoint(380.0, 961.0), PlanPoint(630.0, 795.0), PlanPoint(345.0, 1190.0)).forEach {
            assertTrue(garden.intersects(prop(it.x, it.y), clearance = 1.0), "Missing third-floor source line at $it")
        }
        val fourth = CampusFloorScenes.forFloor("fourth").sourceLinework!!
        assertTrue(fourth.intersects(prop(1230.0, 1095.0), clearance = 1.0), "Missing grey connector divider")
        // These former badge rims must not become partitions or block decoration.
        assertFalse(garden.intersects(prop(1368.0, 794.0), clearance = 0.0), "Badge 37 became a wall")
        assertFalse(fourth.intersects(prop(1196.0, 988.0), clearance = 0.0), "Badge 60 became a wall")
    }

    @Test
    fun everyActiveFloorUsesSourcePartitionsWithoutConflictingDecoration() {
        BeCentralPlans.floors.forEach { floor ->
            val scene = CampusFloorScenes.forFloor(floor.id)
            val lines = assertNotNull(scene.sourceLinework, floor.id)
            assertTrue(lines.partitions.isNotEmpty(), "${floor.id}: missing partitions")
            assertTrue(lines.stairs.isNotEmpty(), "${floor.id}: missing stairs")
            assertTrue(scene.walls.isEmpty(), "${floor.id}: fictional dividers must not contradict the source")
            scene.props.forEach { assertFalse(lines.intersects(it), "${floor.id}: ${it.id} obscures source linework") }
        }
    }

    @Test
    fun compactGroundAndThirdFloorFurnishingsRemainVisible() {
        val ground = CampusFloorScenes.forFloor("ground")
        val bikeRack = ground.props.single { it.id == "lobby-bike-parking" }
        assertEquals("ground-bike-room", bikeRack.regionId)
        assertTrue(ground.props.map { it.id }.containsAll(setOf(
            "lobby-desk", "lobby-plant", "orange-worktable", "orange-plant", "fari-table", "fari-plant",
        )))

        val third = CampusFloorScenes.forFloor("third")
        assertTrue(third.props.map { it.id }.containsAll(setOf(
            "f3-garden-sofa-a", "f3-garden-shelf-a", "f3-garden-round-a", "f3-garden-plant-b",
        )))
        assertTrue(third.props.count { it.regionId == "third-garden" } >= 6)
    }
}
