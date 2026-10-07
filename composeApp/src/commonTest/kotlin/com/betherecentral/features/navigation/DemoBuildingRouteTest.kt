package com.betherecentral.features.navigation

import com.betherecentral.features.building.data.DemoBuilding
import com.betherecentral.features.building.domain.ConceptFloorRegistration
import com.betherecentral.features.building.domain.Point2D
import com.betherecentral.features.navigation.domain.NavigationEdge
import com.betherecentral.features.navigation.domain.RouteMode
import com.betherecentral.features.navigation.domain.RoutePlanner
import com.betherecentral.features.navigation.domain.RoutePreference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DemoBuildingRouteTest {
    @Test
    fun conceptIllustrationRegistrationMatchesItsCropAndWorldExtent() {
        assertEquals(24, ConceptFloorRegistration.sourceLeftPx)
        assertEquals(48, ConceptFloorRegistration.sourceTopPx)
        assertEquals(1488, ConceptFloorRegistration.sourceWidthPx)
        assertEquals(872, ConceptFloorRegistration.sourceHeightPx)
        assertEquals(Point2D(0.0, 43.6), ConceptFloorRegistration.imageToWorld(Point2D(24.0, 48.0)))
        assertEquals(Point2D(74.4, 0.0), ConceptFloorRegistration.imageToWorld(Point2D(1512.0, 920.0)))
        assertTrue(DemoBuilding.floors.all { it.widthMeters == 74.4 && it.heightMeters == 43.6 })
    }

    @Test
    fun sampleMapHasFourFloorsAndTwelveStableNamedRoomsPerFloor() {
        assertEquals(listOf("floor-1", "floor-2", "floor-3", "floor-4"), DemoBuilding.floors.map { it.id })
        assertEquals(48, DemoBuilding.rooms.size)
        assertEquals(48, DemoBuilding.rooms.map { it.id }.toSet().size)
        for (floor in DemoBuilding.floors) {
            val rooms = DemoBuilding.rooms.filter { it.floorId == floor.id }
            assertEquals(setOf("orbit", "meeting-01", "meeting-02", "moss", "cafe", "lounge", "quiet", "phone-booth-1", "phone-booth-2", "spark", "reception", "north"), rooms.map { it.id.substringAfter("room-l${floor.id.removePrefix("floor-")}-") }.toSet())
        }
        assertEquals(setOf("cp-l1-lobby", "cp-l2-lobby", "cp-l3-lobby", "cp-l4-lobby", "cp-l1-west"), DemoBuilding.checkpoints.map { it.id }.toSet())
    }

    @Test
    fun allRoomFootprintsFitAndEveryDoorTouchesItsRoomBoundary() {
        for (room in DemoBuilding.rooms) {
            val bounds = DemoBuilding.floorBounds.getValue(room.floorId)
            val left = room.center.xMeters - room.widthMeters / 2
            val right = room.center.xMeters + room.widthMeters / 2
            val bottom = room.center.yMeters - room.heightMeters / 2
            val top = room.center.yMeters + room.heightMeters / 2
            assertTrue(left >= bounds.minX && right <= bounds.maxX && bottom >= bounds.minY && top <= bounds.maxY, room.id)
            val epsilon = 1e-9
            val onVerticalEdge = (kotlin.math.abs(room.entrance.xMeters - left) < epsilon || kotlin.math.abs(room.entrance.xMeters - right) < epsilon) && room.entrance.yMeters in bottom..top
            val onHorizontalEdge = (kotlin.math.abs(room.entrance.yMeters - bottom) < epsilon || kotlin.math.abs(room.entrance.yMeters - top) < epsilon) && room.entrance.xMeters in left..right
            assertTrue(onVerticalEdge || onHorizontalEdge, "${room.id} entrance ${room.entrance} must touch its footprint")
        }
    }

    @Test
    fun routesFollowTheCafeAndLoungeDoorBends() {
        val cafe = DemoBuilding.rooms.single { it.id == "room-l1-cafe" }
        val lounge = DemoBuilding.rooms.single { it.id == "room-l1-lounge" }
        for ((room, bend) in listOf(cafe to Point2D(755.0, 595.0), lounge to Point2D(755.0, 595.0))) {
            val route = assertNotNull(DemoBuilding.findRoute("cp-l1-lobby", room.id, RoutePreference.STEP_FREE))
            val routePoints = route.legs.flatMap { it.points }
            assertTrue(routePoints.contains(room.entrance), "route should reach door of ${room.id}")
            assertTrue(routePoints.contains(ConceptFloorRegistration.imageToWorld(bend)), "route should use the depicted aisle bend for ${room.id}")
        }
    }

    @Test
    fun routeSpansFloorsAndUsesStairsByDefaultButLiftWhenPreferred() {
        val destination = "room-l4-orbit"
        val default = assertNotNull(DemoBuilding.findRoute("cp-l1-lobby", destination, RoutePreference.DEFAULT))
        val lift = assertNotNull(DemoBuilding.findRoute("cp-l1-lobby", destination, RoutePreference.LIFT_ORIENTED))
        assertTrue(default.legs.any { it.mode == RouteMode.STAIRS })
        assertTrue(default.distanceMeters > 0.0)
        assertTrue(lift.legs.any { it.mode == RouteMode.LIFT })
        assertTrue(lift.legs.none { it.mode == RouteMode.STAIRS })
        assertEquals(RoutePreference.LIFT_ORIENTED, lift.preference)
    }

    @Test
    fun everyCheckpointCanReachEveryRoomStepFreeAndThroughTheLift() {
        for (checkpoint in DemoBuilding.checkpoints) for (room in DemoBuilding.rooms) {
            val route = assertNotNull(DemoBuilding.findRoute(checkpoint.id, room.id, RoutePreference.STEP_FREE), "${checkpoint.id} to ${room.id}")
            assertTrue(route.stepFree)
            assertTrue(route.legs.none { it.mode == RouteMode.STAIRS })
            assertTrue(route.legs.all { leg -> leg.points.all { p -> p.xMeters in 0.0..74.4 && p.yMeters in 0.0..43.6 } })
        }
    }

    @Test
    fun everyGraphNodeUsesOneConsistentCoordinateAndRouteLegsJoinWithoutJumps() {
        val endpointByNode = mutableMapOf<String, Point2D>()
        for (edge in DemoBuilding.navigationEdges) {
            val first = edge.points.first()
            val last = edge.points.last()
            val previousFrom = endpointByNode[edge.fromNodeId]
            val previousTo = endpointByNode[edge.toNodeId]
            assertEquals(previousFrom ?: first, first, "${edge.fromNodeId} has inconsistent outgoing coordinate")
            assertEquals(previousTo ?: last, last, "${edge.toNodeId} has inconsistent incoming coordinate")
            endpointByNode[edge.fromNodeId] = first
            endpointByNode[edge.toNodeId] = last
        }
        for (checkpoint in DemoBuilding.checkpoints) for (room in DemoBuilding.rooms) {
            val route = assertNotNull(DemoBuilding.findRoute(checkpoint.id, room.id, RoutePreference.STEP_FREE))
            for ((current, next) in route.legs.zipWithNext()) {
                assertEquals(current.points.last(), next.points.first(), "route jumps between ${current.toNodeId} and ${next.fromNodeId}")
            }
        }
    }

    @Test
    fun unknownNodesReturnNullAndStepFreePlannerRejectsInaccessibleEdges() {
        assertNull(DemoBuilding.findRoute("unknown", "room-l3-moss", false))
        assertNull(DemoBuilding.findRoute("cp-l1-west", "unknown", false))
        val edge = NavigationEdge("start", "destination", "floor-1", RouteMode.WALK, 2.0, 2.0, listOf(Point2D(0.0, 0.0), Point2D(2.0, 0.0)), isStepFree = false)
        assertNotNull(RoutePlanner.findRoute(listOf(edge), "start", "destination", "cp", "room"))
        assertNull(RoutePlanner.findRoute(listOf(edge), "start", "destination", "cp", "room", RoutePreference.STEP_FREE))
    }

    @Test
    fun navigationEdgesRejectNegativeOrNonFiniteCostsAndDistances() {
        fun edge(distance: Double = 1.0, cost: Double = 1.0) = NavigationEdge("a", "b", "floor-1", RouteMode.WALK, distance, cost, emptyList())
        assertFailsWith<IllegalArgumentException> { edge(cost = -1.0) }
        assertFailsWith<IllegalArgumentException> { edge(cost = Double.NaN) }
        assertFailsWith<IllegalArgumentException> { edge(distance = Double.POSITIVE_INFINITY) }
    }
}
